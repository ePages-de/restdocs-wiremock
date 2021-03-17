#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

openssl aes-256-cbc -K $encrypted_c2f58914b0fd_key -iv $encrypted_c2f58914b0fd_iv \
	-in secret-keys.gpg.enc -out "${SIGNING_KEYRING_FILE}" -d

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
