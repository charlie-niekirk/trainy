#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
fixture="$(mktemp -d)"
trap 'rm -rf "$fixture"' EXIT

cp "$script_dir/affected-unit-test-tasks.sh" "$fixture/affected-unit-test-tasks.sh"
cp "$script_dir/affected-ui-test-tasks.sh" "$fixture/affected-ui-test-tasks.sh"
chmod +x "$fixture/affected-unit-test-tasks.sh" "$fixture/affected-ui-test-tasks.sh"
cd "$fixture"

git init --quiet
git config user.email "affected-tests@example.com"
git config user.name "Affected tests"

mkdir -p app/src/androidTest feature/search/src/androidTest feature/settings/src/main
mkdir -p build-logic/src/main/kotlin gradle

cat > settings.gradle.kts <<'EOF'
include(":app")
include(":feature:search")
include(":feature:settings")
EOF

for module in app feature/search feature/settings; do
    mkdir -p "$module/src/main"
    cat > "$module/build.gradle.kts" <<'EOF'
plugins {
    id("trainy.android.library")
}
EOF
done

cat > build-logic/build.gradle.kts <<'EOF'
gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "trainy.android.library"
            implementationClass = "example.AndroidLibraryConventionPlugin"
        }
    }
}
EOF

touch build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt
touch gradle/libs.versions.toml
touch app/src/androidTest/AppTest.kt app/src/main/App.kt
touch feature/search/src/androidTest/SearchTest.kt feature/search/src/main/Search.kt
touch feature/settings/src/main/Settings.kt
git add .
git commit --quiet -m "Initial fixture"

assert_tasks() {
    local description="$1"
    local base="$2"
    local head="$3"
    local expected="$4"
    local actual

    actual="$(./affected-ui-test-tasks.sh "$base" "$head" 2>/dev/null)"
    if [[ "$actual" != "$expected" ]]; then
        printf '%s\nExpected: %s\nActual:   %s\n' "$description" "$expected" "$actual" >&2
        exit 1
    fi
}

commit_change() {
    local file="$1"
    printf '\nchanged\n' >> "$file"
    git add "$file"
    git commit --quiet -m "Change $file"
}

base="$(git rev-parse HEAD)"
commit_change feature/search/src/main/Search.kt
head="$(git rev-parse HEAD)"
assert_tasks \
    "Feature with UI tests" \
    "$base" \
    "$head" \
    ":feature:search:connectedDebugAndroidTest"

base="$head"
commit_change feature/settings/src/main/Settings.kt
head="$(git rev-parse HEAD)"
assert_tasks "Feature without UI tests" "$base" "$head" ""

base="$head"
commit_change app/src/main/App.kt
head="$(git rev-parse HEAD)"
assert_tasks "Non-feature module" "$base" "$head" ""

base="$head"
commit_change settings.gradle.kts
head="$(git rev-parse HEAD)"
assert_tasks \
    "Global Gradle change" \
    "$base" \
    "$head" \
    ":feature:search:connectedDebugAndroidTest"

echo "All affected UI test task checks passed."
