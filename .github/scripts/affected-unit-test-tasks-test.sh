#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
fixture="$(mktemp -d)"
trap 'rm -rf "$fixture"' EXIT

cp "$script_dir/affected-unit-test-tasks.sh" "$fixture/affected-unit-test-tasks.sh"
cd "$fixture"

git init --quiet
git config user.email "affected-tests@example.com"
git config user.name "Affected tests"

mkdir -p app/src/main core/data/src/main feature/search/src/main
mkdir -p build-logic/src/main/kotlin gradle

cat > settings.gradle.kts <<'EOF'
include(":app")
include(":core:data")
include(":feature:search")
EOF

cat > app/build.gradle.kts <<'EOF'
plugins {
    id("trainy.android.application")
}

dependencies {
    implementation(libs.example.runtime)
}
EOF

cat > core/data/build.gradle.kts <<'EOF'
plugins {
    id("trainy.metro")
}
EOF

cat > feature/search/build.gradle.kts <<'EOF'
plugins {
    id("trainy.android.library")
}
EOF

cat > build-logic/build.gradle.kts <<'EOF'
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "trainy.android.application"
            implementationClass = "example.AndroidApplicationConventionPlugin"
        }
        register("metro") {
            id = "trainy.metro"
            implementationClass = "example.MetroConventionPlugin"
        }
    }
}
EOF

cat > build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt <<'EOF'
class AndroidApplicationConventionPlugin
EOF

cat > build-logic/src/main/kotlin/MetroConventionPlugin.kt <<'EOF'
class MetroConventionPlugin {
    val runtime = libs.findLibrary("metro.runtime")
}
EOF

cat > gradle/libs.versions.toml <<'EOF'
[versions]
shared = "1.0"

[libraries]
example-runtime = { module = "example:runtime", version.ref = "shared" }
metro-runtime = { module = "example:metro-runtime", version.ref = "shared" }
EOF

touch app/src/main/App.kt core/data/src/main/Data.kt feature/search/src/main/Search.kt
git add .
git commit --quiet -m "Initial fixture"

assert_tasks() {
    local description="$1"
    local base="$2"
    local head="$3"
    local expected="$4"
    local actual

    actual="$(./affected-unit-test-tasks.sh "$base" "$head" 2>/dev/null)"
    if [[ "$actual" != "$expected" ]]; then
        printf '%s\nExpected: %s\nActual:   %s\n' "$description" "$expected" "$actual" >&2
        exit 1
    fi
}

base="$(git rev-parse HEAD)"
printf '\nchanged\n' >> core/data/src/main/Data.kt
git add core/data/src/main/Data.kt
git commit --quiet -m "Change nested module"
head="$(git rev-parse HEAD)"
assert_tasks "Nested module change" "$base" "$head" ":core:data:test"

base="$head"
printf '\n// changed\n' >> build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt
git add build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt
git commit --quiet -m "Change application convention"
head="$(git rev-parse HEAD)"
assert_tasks "Convention plugin change" "$base" "$head" ":app:test"

base="$head"
sed -i.bak 's/shared = "1.0"/shared = "2.0"/' gradle/libs.versions.toml
rm gradle/libs.versions.toml.bak
git add gradle/libs.versions.toml
git commit --quiet -m "Change shared catalog version"
head="$(git rev-parse HEAD)"
assert_tasks "Version catalog change" "$base" "$head" ":app:test :core:data:test"

base="$head"
printf '\n// changed\n' >> settings.gradle.kts
git add settings.gradle.kts
git commit --quiet -m "Change global settings"
head="$(git rev-parse HEAD)"
assert_tasks \
    "Global settings change" \
    "$base" \
    "$head" \
    ":app:test :core:data:test :feature:search:test"

echo "All affected unit test task checks passed."
