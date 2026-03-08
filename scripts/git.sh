#!/bin/bash

# Check whether there are pending changes.
if [ -z "$(git status --porcelain)" ]; then
    echo "No changes to commit. Exiting..."
    exit 0
fi

TAG_MESSAGE=$1
TICKET_ID=${2:-"users"}

# Add all changes to the staging area and create a commit.
echo "Staging changes and creating commit..."
git add .
if [ -z "$TAG_MESSAGE" ]; then
	git commit --allow-empty-message -m '' --no-verify
    TAG_MESSAGE="deploy to test environment"
else
	git commit -m "$TAG_MESSAGE" --no-verify
fi

# Push changes to the remote repository.
echo "Pushing changes to the remote repository..."
git push

git fetch -p

# Get the latest tag number and prepare to increment it if needed.
LAST_TAG=$(git tag -l "snapshot-$TICKET_ID-*" | sort -Vr | head -n 1)
if [[ $LAST_TAG =~ ([0-9]+)$ ]]; then
    TAG_NUMBER=$((${BASH_REMATCH[1]} + 1))
else
    TAG_NUMBER=1
fi

# Increment the tag number automatically if the tag already exists.
TAG_EXISTS=1
while [ $TAG_EXISTS -ne 0 ]; do
    TAG="snapshot-$TICKET_ID-$(printf "%02d" $TAG_NUMBER)"
    if git rev-parse "$TAG" >/dev/null 2>&1; then
        TAG_NUMBER=$((TAG_NUMBER + 1))
    else
        TAG_EXISTS=0
    fi
done


echo "Checking out the latest commit..."
git checkout HEAD

echo "Creating tag $TAG..."
git tag -a "$TAG" -m "$TAG_MESSAGE"

echo "Pushing tag to the remote repository..."
git push origin "$TAG"
