#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

if [[ "$TRAVIS_TAG" =~ ^[0-9.]+ ]] ; then
	./gradlew publishToSonatype --info
fi
