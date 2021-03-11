#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

./gradlew clean build jacocoTestReport coveralls \
  -Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
  -Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}"
