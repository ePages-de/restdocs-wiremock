#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

./gradlew  \
  clean \
  build \
  --info \
  --exclude-task signMavenJavaPublication \
  --exclude-task signArchives
