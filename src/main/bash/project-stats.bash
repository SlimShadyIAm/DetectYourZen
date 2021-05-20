#!/bin/bash

[[ "$#" -ne 1 ]] && echo "Requires 1 arguments: data directory" && exit 1

datadir="$1"

summarize () {
  stat="$1"
  grep "$stat" "$datadir"/*/stats.csv | sed -e 's/.*'"$stat"',//g' | awk '{s+=$1} END {print s}'
}

echo "file count: $(summarize 'git_file_count')"
echo "line count: $(summarize 'parse_lines')"

