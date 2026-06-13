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
done < <(git diff --name-only --diff-filter=ACMR "$base_ref" "$head_ref")

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
        project_dirs+=("${project_dir//:/\/}")
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

for file in "${changed_files[@]}"; do
    case "$file" in
        settings.gradle.kts|build.gradle.kts|gradle.properties|gradlew|gradlew.bat|build-logic/*|gradle/*)
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
