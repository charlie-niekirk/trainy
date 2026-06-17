#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
base_ref="${1:-${BASE_SHA:-}}"
head_ref="${2:-${HEAD_SHA:-HEAD}}"

unit_tasks="$($script_dir/affected-unit-test-tasks.sh "$base_ref" "$head_ref")"

ui_tasks=()
for unit_task in $unit_tasks; do
    project="${unit_task%:test}"
    project_dir="${project#:}"
    project_dir="${project_dir//:/$'/'}"

    if [[ "$project" == :feature:* ]] &&
        [[ -d "$project_dir/src/androidTest" ]] &&
        find "$project_dir/src/androidTest" -type f -print -quit | grep -q .; then
        ui_tasks+=("${project}:connectedDebugAndroidTest")
    fi
done

if [[ "${#ui_tasks[@]}" -eq 0 ]]; then
    echo "No affected feature modules contain UI tests." >&2
    exit 0
fi

echo "Affected UI test tasks: ${ui_tasks[*]}" >&2
printf '%s\n' "${ui_tasks[*]}"
