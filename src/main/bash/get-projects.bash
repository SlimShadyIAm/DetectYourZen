#!/bin/bash

[[ "$#" -ne 1 ]] && echo "Requires 1 arguments: output file" && exit 1

check_for_command () {
  command -v "$1" >/dev/null 2>&1 || {
    echo >&2 "Must have $1 installed, aborting."; exit 1;
  }
}

check_for_command jq
check_for_command wget

outfile="$1"
url="https://api.github.com/search/repositories?q="
pages=10
per_page=100
needed=1000
query="\
language:Python\
+fork:false\
+archived:false\
+is:public\
+size:>=1000\
+NOT book in:description,readme\
+NOT cookbook in:name\
+NOT awesome in:name\
+NOT tutorial in:name\
+NOT manual in:name\
"

amount=$((pages*per_page))
rm -f "$outfile.tmp"
# This loop is necessary because the API keeps returning different results on
# any call, so that there are many duplicates on the first 10 pages. Keep
# searching until we found at least $needed projects.
while true; do
  for (( i=0; i<$pages; i++ )); do
    searchUrl="$url""$query"'&sort=stars&order=desc&per_page='"$per_page"'&page='"$((i+1))"
    echo "getting $searchUrl"
    wget -q "$searchUrl" -O - \
      | jq '.items[].git_url' \
      | sed -e 's/^"//g;s/"$//g' \
      | head -n "$amount" \
      >> "$outfile.tmp"
    unique=$(sort "$outfile.tmp" | uniq | wc -l)
    echo "found $unique unique projects"
    if [[ "$unique" -ge "$needed" ]]; then break; fi
    # a maximum of 10 requests are allowed per minute
    sleep "6.1"
  done
  if [[ "$unique" -ge "$needed" ]]; then break; fi
done

sort "$outfile.tmp" | uniq | head -n "$needed" > "$outfile"
rm "$outfile.tmp"

