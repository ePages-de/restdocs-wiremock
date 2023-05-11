#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

./gradlew clean build \
  --exclude-task signMavenJavaPublication \
  --exclude-task signArchives
  sonarqube --info
