#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

if [[ "$TRAVIS_TAG" =~ ^[0-9.]+ ]] ; then
	./gradlew publishToSonatype \
		--exclude-task :restdocs-server:publishToSonatype \
		--info \
		-Dorg.gradle.project.sonatypeUsername="${SONATYPE_USERNAME}" \
		-Dorg.gradle.project.sonatypePassword="${SONATYPE_PASSWORD}" \
	 	-Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
		-Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}" \
		-Dorg.gradle.project.signing.secretKeyRingFile="${SIGNING_KEYRING_FILE}"
fi
