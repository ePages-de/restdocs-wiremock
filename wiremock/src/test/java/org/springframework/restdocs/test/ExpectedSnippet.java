/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.restdocs.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.restdocs.snippet.TemplatedSnippet;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.restdocs.test.SnippetMatchers.SnippetMatcher;

/**
 * The {@code ExpectedSnippet} JUnit Jupiter extension is used to verify that a
 * {@link TemplatedSnippet} has generated the expected snippet.
 *
 * @author Andy Wilkinson
 * @author Andreas Evers
 */
public class ExpectedSnippet implements BeforeEachCallback, AfterEachCallback {

	private final TemplateFormat templateFormat;

	private final SnippetMatcher snippet;

	private String expectedName;

	private String expectedType;

	private File outputDirectory;

	public ExpectedSnippet(TemplateFormat templateFormat) {
		this.templateFormat = templateFormat;
		this.snippet = SnippetMatchers.snippet(templateFormat);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		this.outputDirectory = new File(
				"build/" + context.getRequiredTestClass().getSimpleName());
	}

	@Override
	public void afterEach(ExtensionContext context) {
		verifySnippet();
	}

	private void verifySnippet() {
		if (this.outputDirectory != null && this.expectedName != null) {
			File snippetDir = new File(this.outputDirectory, this.expectedName);
			File snippetFile = new File(snippetDir,
					this.expectedType + "." + this.templateFormat.getFileExtension());
			assertThat(snippetFile, is(this.snippet));
		}
	}

	public ExpectedSnippet expectWireMockJson(String name) {
		expect(name, "wiremock-stub");
		return this;
	}

	private ExpectedSnippet expect(String name, String type) {
		this.expectedName = name;
		this.expectedType = type;
		return this;
	}

	public void withContents(Matcher<String> matcher) {
		this.snippet.withContents(matcher);
	}

	public File getOutputDirectory() {
		return this.outputDirectory;
	}

}
