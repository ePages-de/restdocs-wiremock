#!/bin/bash

set -e # Exit with nonzero exit code if anything fails

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

function check_variable_set() {
  _VARIABLE_NAME=$1
	_VARIABLE_VALUE=${!_VARIABLE_NAME}
	if [[ -z ${_VARIABLE_VALUE} ]] ; then
		echo "Missing env variable ${_VARIABLE_NAME}"
		exit 1
	fi
}
check_variable_set FILE_ENCRYPTION_PASSWORD
check_variable_set SIGNING_KEY_ID
check_variable_set SIGNING_PASSWORD
check_variable_set SONATYPE_USERNAME
check_variable_set SONATYPE_PASSWORD

# # Decrypt signing key
gpg --quiet --batch --yes --decrypt --passphrase="${FILE_ENCRYPTION_PASSWORD}" \
	--output secret-keys.gpg secret-keys.gpg.enc

# Publish
./gradlew publishToSonatype \
	--info \
	-Dorg.gradle.project.sonatypeUsername="${SONATYPE_USERNAME}" \
	-Dorg.gradle.project.sonatypePassword="${SONATYPE_PASSWORD}" \
	-Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
	-Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}" \
	-Dorg.gradle.project.signing.secretKeyRingFile="${SCRIPT_DIR}/secret-keys.gpg"
