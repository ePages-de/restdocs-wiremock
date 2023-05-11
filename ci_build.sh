#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

./gradlew  \
  -Dorg.gradle.project.sonar.projectKey="ePages-de_restdocs-wiremock" \
  -Dorg.gradle.project.sonar.organization="epages-de" \
  -Dorg.gradle.project.sonar.host.url="https://sonarcloud.io" \
  clean  \
  build \
  sonarqube \
  --info \
  --exclude-task signMavenJavaPublication \
  --exclude-task signArchives \
