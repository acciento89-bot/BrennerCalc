#!/bin/sh
set -eu
test_dir=$(mktemp -d /tmp/brenner-tests.XXXXXX)
combined="$test_dir/main.swift"
binary="$test_dir/core-tests"
trap 'rm -f "$combined" "$binary"; rmdir "$test_dir"' EXIT
cat BrennerCalc/CalculatorEngine.swift > "$combined"
if [ -f BrennerCalc/ServiceVisits.swift ]; then cat BrennerCalc/ServiceVisits.swift >> "$combined"; fi
cat Tests/main.swift >> "$combined"
swiftc "$combined" -o "$binary"
"$binary"
