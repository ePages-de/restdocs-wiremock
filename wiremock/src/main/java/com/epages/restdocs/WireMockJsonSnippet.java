package com.epages.restdocs;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolver;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

final class WireMockJsonSnippet implements Snippet {

	private static final String SNIPPET_NAME = "wiremock-stub";

	private static final TemplateFormat TEMPLATE_FORMAT = new TemplateFormat() {

		@Override
		public String getId() {
			return "json";
		}

		@Override
		public String getFileExtension() {
			return "json";
		}
	};

	WireMockJsonSnippet() {
	}

	@Override
	public void document(Operation operation) throws IOException {
		RestDocumentationContext context = (RestDocumentationContext) operation.getAttributes()
				.get(RestDocumentationContext.class.getName());
		WriterResolver writerResolver = new StandardWriterResolver(
				new RestDocumentationContextPlaceholderResolver(context), "UTF-8", TEMPLATE_FORMAT);
		try (Writer writer = writerResolver.resolve(operation.getName(), SNIPPET_NAME, context)) {
			writer.append(toJsonString(operation));
		}
	}

	private String toJsonString(Operation operation) throws JsonProcessingException {
		return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(createModel(operation));
	}

	protected Map<Object, Object> createModel(Operation operation) {
		OperationResponse response = operation.getResponse();

		Maps.Builder<Object, Object> requestBuilder = Maps.builder()
				.put("method", operation.getRequest().getMethod())
				.put("urlPath", operation.getRequest().getUri().getRawPath());

		Maps.Builder<Object, Object> responseBuilder = Maps.builder()
				.put("status", response.getStatus().value()).put("headers", response.getHeaders())
				.put("body", responseBody(response));

		Map<Object, Object> queryParams = queryParams(operation);
		if (!queryParams.isEmpty()) {
			requestBuilder.put("queryParameters", queryParams);
		}

		Map<Object, Object> headers = requestHeaders(operation.getRequest());
		if (!headers.isEmpty()) {
			requestBuilder.put("headers", headers);
		}

		return Maps.builder().put("request", requestBuilder.build()).put("response", responseBuilder.build())
				.build();
	}

	private Map<Object, Object> queryParams(Operation operation) {
		Maps.Builder<Object, Object> queryParams = Maps.builder();

		for (Map.Entry<String, List<String>> e : operation.getRequest().getParameters().entrySet()) {
			queryParams.put(e.getKey(), Maps.of("equalTo", e.getValue().get(0)));
		}

		return queryParams.build();
	}

	private String responseBody(OperationResponse response) {
		return response.getContentAsString();
	}

	private Map<Object, Object> requestHeaders(OperationRequest request) {
		Maps.Builder<Object, Object> requestHeaders = Maps.builder();
		for (Map.Entry<String, List<String>> e : request.getHeaders().entrySet()) {
			if ("content-type".equalsIgnoreCase(e.getKey()) || "accept".equalsIgnoreCase(e.getKey())) {
				requestHeaders.put(e.getKey(), Maps.of("equalTo", e.getValue().get(0)));
			}
		}
		return requestHeaders.build();
	}

	static class Maps {
		static <K, V> Map<K, V> of(K k1, V v1) {
			HashMap<K, V> map = new HashMap<>();
			map.put(k1, v1);
			return map;
		}

		static <K, V> Builder<K, V> builder() {
			return new Builder<>();
		}

		static class Builder<K, V> {
			private final Map<K, V> map;

			public Builder() {
				map = new HashMap<>();
			}

			Builder<K, V> put(K k, V v) {
				map.put(k, v);
				return this;
			}

			Map<K, V> build() {
				return map;
			}
		}
	}

}