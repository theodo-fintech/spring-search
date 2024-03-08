#!/bin/bash

# Check if new_version argument is provided
if [ $# -ne 1 ]; then
    echo "Usage: $0 <new_version>"
    exit 1
fi

# Assign the new_version argument to a variable
new_version="$1"
commit_message="Bump project version to $new_version"
branch_name="bump/v$new_version"

# Bump project version
mvn versions:set -DgenerateBackupPoms=false -DnewVersion="$new_version"

# Commit & push changes
git checkout -b "$branch_name"
git add pom.xml
git commit -m "$commit_message"
git push origin "$branch_name"

# Create pull request
gh pr create --title "spring-search-$new_version" --body "Automated PR for project version bump" --base master --head "$branch_name"
