package com.epages.restdocs;

public class ResponseFieldTemplateDescriptor {

	/**
	 * Create a descriptor defining the replacement of a response field with a response template.
	 * See http://wiremock.org/docs/response-templating/ for details.
	 *
	 * @param path jsonPath expression of the field to be replaced
	 */
	public ResponseFieldTemplateDescriptor(String path) {
		this.path = path;
	}

	private final String path;
	private String wireMockTemplateExpression;
	private String uriTemplateVariableName;


	public String getPath() {
		return path;
	}

	public String getWireMockTemplateExpression() {
		return wireMockTemplateExpression;
	}

	public String getUriTemplateVariableName() {
		return uriTemplateVariableName;
	}

	/**
	 * Use a WireMock response template expression to replace the value at <code>path</code>.
	 * Expressions can be used as described in the <a href="http://wiremock.org/docs/response-templating/">WireMock Response Templating documentation</a>
	 * Note that <code>replacedWithUriTemplateVariableValue</code> and <code>replacedWithWireMockTemplateExpression</code> cannot be used together.
	 * @param expression the Wiremock response template expression (state without curly braces - e.g <code>request.requestLine.pathSegments.[1]</code>)
	 */
	public ResponseFieldTemplateDescriptor replacedWithWireMockTemplateExpression(String expression) {
			this.wireMockTemplateExpression = expression;
			return this;
	}

	/**
	 * The name of the URI template variable to use as a replacement.
	 * This requires the use of URI templates using the methods in <code>org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders</code> that take a <code>urlTemplate</code> parameter.
	 *
	 * Note that <code>replacedWithUriTemplateVariableValue</code> and <code>replacedWithWireMockTemplateExpression</code> cannot be used together.
	 * @param uriTemplateVariableName the name of the variable in the URI template of the request
	 */
	public ResponseFieldTemplateDescriptor replacedWithUriTemplateVariableValue(String uriTemplateVariableName) {
		this.uriTemplateVariableName = uriTemplateVariableName;
		return this;
	}
}
