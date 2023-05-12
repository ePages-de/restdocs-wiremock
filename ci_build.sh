#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

./gradlew  \
  build \
  --info \
  --exclude-task signMavenJavaPublication \
  --exclude-task signArchives \
