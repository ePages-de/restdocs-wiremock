package com.epages.restdocs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationExtension;

class RestDocumentationTest {

	@Test
	void should_create_restdocumentation() {
		RestDocumentationExtension documentation1 = RestDocumentation.usingGradleDir();
		assertNotNull(documentation1);
		RestDocumentationExtension documentation2 = RestDocumentation.usingMavenDir();
		assertNotNull(documentation2);
	}
}
