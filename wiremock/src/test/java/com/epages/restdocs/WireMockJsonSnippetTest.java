package com.epages.restdocs;

import static com.epages.restdocs.WireMockDocumentation.wiremockJson;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.OperationResponseFactory;
import org.springframework.restdocs.operation.RequestConverter;
import org.springframework.restdocs.operation.ResponseConverter;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.restdocs.test.ExpectedSnippet;
import org.springframework.restdocs.test.OperationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class WireMockJsonSnippetTest {

	private static final TemplateFormat FORMAT = WireMockJsonSnippet.TEMPLATE_FORMAT;

	@Rule
	public ExpectedSnippet expectedSnippet = new ExpectedSnippet(FORMAT);

	@SuppressWarnings("unchecked")
	private final RequestConverter<Object> requestConverter = Mockito.mock(RequestConverter.class);

	@SuppressWarnings("unchecked")
	private final ResponseConverter<Object> responseConverter = Mockito.mock(ResponseConverter.class);

	private final Object request = new Object();

	private final Object response = new Object();

	private final OperationRequest operationRequest = new OperationRequestFactory()
			.create(URI.create("http://localhost:8080"), null, null, new HttpHeaders(), null, null);

	private final OperationResponse operationResponse = new OperationResponseFactory().create(null, null, null);

	private final Snippet snippet = Mockito.mock(Snippet.class);

	@Test
	public void basicHandling() throws IOException {
		given(this.requestConverter.convert(this.request)).willReturn(this.operationRequest);
		given(this.responseConverter.convert(this.response)).willReturn(this.operationResponse);
		HashMap<String, Object> configuration = new HashMap<>();
		new RestDocumentationGenerator<>("id", this.requestConverter, this.responseConverter, this.snippet)
				.handle(this.request, this.response, configuration);
		verifySnippetInvocation(this.snippet, configuration);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simpleRequest() throws IOException {
		this.expectedSnippet.expectWireMockJson("simple-request").withContents(
				(Matcher<String>) SameJSONAs
						.sameJSONAs(new ObjectMapper().writeValueAsString(expectedJsonForSimpleRequest())));
		wiremockJson().document(operationBuilder("simple-request").request("http://localhost/").method("GET").build());
	}

	private ImmutableMap<String, ImmutableMap<String, ? extends Object>> expectedJsonForSimpleRequest() {
		return of( //
				"request", //
				of("method", "GET", "urlPath", "/"), //
				"response", //
				of("headers", emptyMap(), "body", "", "status", 200));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getRequestWithParams() throws IOException {
		this.expectedSnippet.expectWireMockJson("get-request").withContents(
				(Matcher<String>) SameJSONAs.sameJSONAs(new ObjectMapper().writeValueAsString(expectedJsonForGetRequestWithParams())));
		wiremockJson().document(operationBuilder("get-request").request("http://localhost/foo?a=b&c=").method("GET")
				.header("Accept", "application/json").build());
	}

	private ImmutableMap<String, ImmutableMap<String, ? extends Object>> expectedJsonForGetRequestWithParams() {
		return of( //
				"request", //
				of("method", "GET", "urlPath", "/foo", "queryParameters", //
						of("a", of("equalTo", "b")), "headers", of("Accept", of("equalTo", "application/json"))), //
				"response", //
				of("headers", emptyMap(), "body", "", "status", 200));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void postRequest() throws IOException {
		this.expectedSnippet.expectWireMockJson("post-request").withContents((Matcher<String>) SameJSONAs
				.sameJSONAs(new ObjectMapper().writeValueAsString(expectedJsonForPostRequest())));
		OperationBuilder operationBuilder = operationBuilder("post-request");
		operationBuilder.response().content("response-content").header("Content-Type", "text/plain").build();
		wiremockJson().document(operationBuilder.request("http://localhost/").method("POST")
				.header("Content-Type", "text/uri-list").content("http://some.uri").build());
	}

	private Map<String, ? extends Object> expectedJsonForPostRequest() {
		return of( //
				"request", //
				of("method", "POST", "urlPath", "/", "headers", of("Content-Type", of("equalTo", "text/uri-list"))), //
				"response", //
				of("headers",
						of("Content-Length", ImmutableList.of("16"), "Content-Type", ImmutableList.of("text/plain")),
						"body", "response-content", "status", 200));
	}

	public OperationBuilder operationBuilder(String name) {
		return new OperationBuilder(name, this.expectedSnippet.getOutputDirectory(), FORMAT);
	}

	private void verifySnippetInvocation(Snippet snippet, Map<String, Object> attributes) throws IOException {
		verifySnippetInvocation(snippet, attributes, 1);
	}

	private void verifySnippetInvocation(Snippet snippet, Map<String, Object> attributes, int times)
			throws IOException {
		ArgumentCaptor<Operation> operation = ArgumentCaptor.forClass(Operation.class);
		verify(snippet, Mockito.times(times)).document(operation.capture());
		assertThat(this.operationRequest, is(equalTo(operation.getValue().getRequest())));
		assertThat(this.operationResponse, is(equalTo(operation.getValue().getResponse())));
		assertThat(attributes, is(equalTo(operation.getValue().getAttributes())));
	}
}
