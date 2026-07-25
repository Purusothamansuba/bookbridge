#!/bin/bash

OUTPUT="all_java_files.txt"

# Clear output file
> "$OUTPUT"

# Find all .java files recursively
find . -name "*.java" | sort | while read -r file
do
    echo "========================================" >> "$OUTPUT"
    echo "File: $file" >> "$OUTPUT"
    echo "========================================" >> "$OUTPUT"
    cat "$file" >> "$OUTPUT"
    echo -e "\n\n" >> "$OUTPUT"
done

echo "Created $OUTPUT"

