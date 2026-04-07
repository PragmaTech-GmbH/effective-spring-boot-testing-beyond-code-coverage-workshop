#!/bin/bash

# Script to render all lab slides (lab-1 through lab-8) into individual PDFs
# Requires Marp CLI: npm install -g @marp-team/marp-cli

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! command -v marp &> /dev/null; then
    echo "Error: Marp CLI not found. Please install it first:"
    echo "  npm install -g @marp-team/marp-cli"
    exit 1
fi

cd "$SCRIPT_DIR"

echo "Rendering individual lab PDFs..."

for lab in 1 2 3 4 5 6 7 8; do
    labFile="lab-${lab}.md"
    if [ ! -f "$labFile" ]; then
        echo "Warning: $labFile not found, skipping."
        continue
    fi
    echo "  Rendering $labFile..."
    marp --pdf "$labFile" --theme pragmatech.css --allow-local-files
done

echo "Done! PDFs saved alongside each lab-N.md file."
