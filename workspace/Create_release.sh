#!/bin/bash
  
repo_full_name=$(git config --get remote.origin.url | sed 's/.*:\/\/github.com\///;s/.git$//')

token="f4f37c34c276080e0b3832792c1ceb0d2ac9f3c5"
tag=$1
# Get the full message associated with this tag
message="$(git for-each-ref refs/tags/$tag --format='%(contents)')"
name=$(echo "$message" | head -n1)
description=$(echo "$message" | sed -z 's/\n/\\n/g') # Escape line breaks to prevent json parsing problems


generate_post_data()
{
          cat <<EOF
{
  "tag_name": "$tag",
  "target_commitish": "develop",
  "name": "$name",
  "body": "$description",
  "draft": false,
  "prerelease": false
}
EOF
}

echo "Create release $version for repo: $repo_full_name branch: $branch"
curl --data "$(generate_post_data)" "https://api.github.com/repos/$repo_full_name/releases?access_token=$token"
