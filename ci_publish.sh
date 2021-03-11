#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

# if [[ "$TRAVIS_PULL_REQUEST" = "true" ]]; then
# 	exit 0
# fi

# if [[ "$TRAVIS_TAG" =~ ^[0-9.]+ ]] ; then
	./gradlew publishToSonatype \
		--exclude-task :restdocs-server:publishToSonatype \
		--info \
	 	-Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
		-Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}"
# fi
