#!/usr/bin/env bash
set -euo pipefail

base_ref="${1:-${BASE_SHA:-}}"
head_ref="${2:-${HEAD_SHA:-HEAD}}"

if [[ -z "$base_ref" ]]; then
    echo "BASE_SHA or first argument is required" >&2
    exit 1
fi

if ! git cat-file -e "${head_ref}^{commit}" 2>/dev/null; then
    echo "Head ref '$head_ref' was not found locally; falling back to HEAD" >&2
    head_ref="HEAD"
fi

if ! git cat-file -e "${base_ref}^{commit}" 2>/dev/null; then
    echo "Base ref '$base_ref' was not found locally" >&2
    exit 1
fi

changed_files=()
while IFS= read -r file; do
    [[ -n "$file" ]] && changed_files+=("$file")
done < <(git diff --name-only --diff-filter=ACMRD "$base_ref" "$head_ref")

if [[ "${#changed_files[@]}" -eq 0 ]]; then
    echo "No changed files detected; no unit test tasks to run." >&2
    exit 0
fi

projects=()
project_dirs=()
while IFS= read -r line; do
    if [[ "$line" =~ include\(\"(:[^\"]+)\"\) ]]; then
        project="${BASH_REMATCH[1]}"
        project_dir="${project#:}"
        projects+=("$project")
        project_dirs+=("${project_dir//:/$'/'}")
    fi
done < settings.gradle.kts

run_all=false
affected_projects=()

add_project() {
    local candidate="$1"
    local existing
    if [[ "${#affected_projects[@]}" -gt 0 ]]; then
        for existing in "${affected_projects[@]}"; do
            if [[ "$existing" == "$candidate" ]]; then
                return
            fi
        done
    fi
    affected_projects+=("$candidate")
}

add_projects_using_plugin() {
    local plugin_id="$1"
    local index
    local build_file

    for index in "${!project_dirs[@]}"; do
        build_file="${project_dirs[$index]}/build.gradle.kts"
        if [[ -f "$build_file" ]] && grep -Fq "id(\"$plugin_id\")" "$build_file"; then
            add_project "${projects[$index]}"
        fi
    done
}

add_projects_for_convention_class() {
    local class_name="$1"
    local plugin_id

    plugin_id="$(awk -v class_name="$class_name" '
        /id = "/ {
            plugin_id = $0
            sub(/^.*id = "/, "", plugin_id)
            sub(/".*$/, "", plugin_id)
        }
        index($0, "implementationClass = \"") && index($0, class_name "\"") {
            print plugin_id
            exit
        }
    ' build-logic/build.gradle.kts)"

    if [[ -n "$plugin_id" ]]; then
        add_projects_using_plugin "$plugin_id"
    else
        run_all=true
    fi
}

add_projects_using_catalog_alias() {
    local alias="$1"
    local accessor="${alias//-/.}"
    local index
    local build_file
    local convention_file
    local convention_class

    accessor="${accessor//_/.}"

    for index in "${!project_dirs[@]}"; do
        build_file="${project_dirs[$index]}/build.gradle.kts"
        if [[ -f "$build_file" ]] && grep -Eq "libs\\.${accessor//./\\.}([^[:alnum:]_]|$)" "$build_file"; then
            add_project "${projects[$index]}"
        fi
    done

    while IFS= read -r convention_file; do
        convention_class="$(basename "$convention_file" .kt)"
        add_projects_for_convention_class "$convention_class"
    done < <(
        grep -lE \
            "find(Library|Plugin|Version)\\(\"${accessor//./\\.}\"\\)" \
            build-logic/src/main/kotlin/*ConventionPlugin.kt 2>/dev/null || true
    )
}

add_projects_for_catalog_changes() {
    local catalog_path="gradle/libs.versions.toml"
    local changed_keys=()
    local affected_aliases=()
    local line
    local key
    local alias
    local existing

    while IFS= read -r line; do
        if [[ "$line" =~ ^[+-]([[:alnum:]_.-]+)[[:space:]]*= ]]; then
            key="${BASH_REMATCH[1]}"
            changed_keys+=("$key")
            affected_aliases+=("$key")
        fi
    done < <(git diff --unified=0 "$base_ref" "$head_ref" -- "$catalog_path")

    for key in "${changed_keys[@]}"; do
        while IFS= read -r alias; do
            [[ -n "$alias" ]] && affected_aliases+=("$alias")
        done < <(
            {
                git show "${base_ref}:${catalog_path}" 2>/dev/null || true
                git show "${head_ref}:${catalog_path}" 2>/dev/null || true
            } | awk -v version_key="$key" '
                $0 ~ "version.ref[[:space:]]*=[[:space:]]*\\\"" version_key "\\\"" {
                    alias = $0
                    sub(/[[:space:]]*=.*$/, "", alias)
                    print alias
                }
            ' | sort -u
        )
    done

    for alias in "${affected_aliases[@]}"; do
        for existing in "${processed_aliases[@]:-}"; do
            [[ "$existing" == "$alias" ]] && continue 2
        done
        processed_aliases+=("$alias")
        add_projects_using_catalog_alias "$alias"
    done
}

processed_aliases=()

for file in "${changed_files[@]}"; do
    case "$file" in
        settings.gradle.kts|build.gradle.kts|gradle.properties|gradlew|gradlew.bat)
            run_all=true
            ;;
        build-logic/src/main/kotlin/*ConventionPlugin.kt)
            add_projects_for_convention_class "$(basename "$file" .kt)"
            ;;
        build-logic/*)
            run_all=true
            ;;
        gradle/libs.versions.toml)
            add_projects_for_catalog_changes
            ;;
        gradle/*)
            run_all=true
            ;;
    esac

    for index in "${!project_dirs[@]}"; do
        module_dir="${project_dirs[$index]}"
        if [[ "$file" == "$module_dir" || "$file" == "$module_dir/"* ]]; then
            add_project "${projects[$index]}"
        fi
    done
done

if [[ "$run_all" == true ]]; then
    affected_projects=("${projects[@]}")
fi

if [[ "${#affected_projects[@]}" -eq 0 ]]; then
    echo "No Gradle module diffs detected; no unit test tasks to run." >&2
    exit 0
fi

tasks=()
for project in "${affected_projects[@]}"; do
    tasks+=("${project}:test")
done

echo "Changed files:" >&2
printf '  %s\n' "${changed_files[@]}" >&2
echo "Affected unit test tasks: ${tasks[*]}" >&2
printf '%s\n' "${tasks[*]}"
