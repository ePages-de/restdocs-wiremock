#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

./gradlew  \
  -Dsonar.projectKey="ePages-de_restdocs-wiremock" \
  -Dsonar.organization="epages-de" \
  -Dsonar.host.url="https://sonarcloud.io" \
  -Dsonar.login="$SONAR_TOKEN" \
  clean  \
  build \
  sonarqube \
  --info \
  --exclude-task signMavenJavaPublication \
  --exclude-task signArchives \
