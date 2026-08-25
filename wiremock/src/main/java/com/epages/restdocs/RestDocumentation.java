package com.epages.restdocs;

import org.springframework.restdocs.RestDocumentationExtension;

/**
 * Convenience helper for running Spring REST Docs in JUnit.
 * 
 */
public abstract class RestDocumentation {

	/**
	 * @return JUnit Jupiter {@link RestDocumentationExtension} for Spring REST Docs, preconfigured for
	 *         writing to build/generated-snippets.
	 */
	public static RestDocumentationExtension usingGradleDir() {
		return new RestDocumentationExtension("build/generated-snippets");
	}

	/**
	 * @return JUnit Jupiter {@link RestDocumentationExtension} for Spring REST Docs, preconfigured for
	 *         writing to target/generated-snippets.
	 */
	public static RestDocumentationExtension usingMavenDir() {
		return new RestDocumentationExtension("target/generated-snippets");
	}

}
