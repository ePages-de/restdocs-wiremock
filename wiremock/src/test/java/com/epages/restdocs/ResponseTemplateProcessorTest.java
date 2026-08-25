package com.epages.restdocs;

import static com.epages.restdocs.WireMockDocumentation.templatedResponseField;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.web.util.UriTemplate;

class ResponseTemplateProcessorTest {

    private String jsonBody = "{\n" +
            "  \"id\": \"the-id\",\n" +
            "  \"name\": \"some\"\n" +
            "}";

    @Test
    void should_replace_with_uri_variable_expression() throws JSONException {
        ResponseFieldTemplateDescriptor templateDescriptor = templatedResponseField("id").replacedWithUriTemplateVariableValue("someId");
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                singletonList(templateDescriptor),
                new UriTemplate("http://localhost/api/things/{someId}"),
                jsonBody);

        String result = templateProcessor.replaceTemplateFields();

        String expected = "{\n" +
            "  \"id\": \"{{request.requestLine.pathSegments.[2]}}\",\n" +
            "  \"name\": \"some\"\n" +
            "}";
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    void should_handle_multiple_descriptors() throws JSONException {
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                Arrays.asList(
                        templatedResponseField("id").replacedWithWireMockTemplateExpression("randomValue length=33 type='ALPHANUMERIC'"),
                        templatedResponseField("name").replacedWithWireMockTemplateExpression("randomValue type='UUID'")
                ),
                null,
                jsonBody);

        String result = templateProcessor.replaceTemplateFields();

        String expected = "{\n" +
            "  \"id\":\"{{randomValue length=33 type='ALPHANUMERIC'}}\",\n" +
            "  \"name\":\"{{randomValue type='UUID'}}\"\n" +
            "}";
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    void should_return_response_on_empty_descriptors() throws JSONException {
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                Collections.emptyList(),
                null,
                jsonBody);

        String result = templateProcessor.replaceTemplateFields();

        JSONAssert.assertEquals(jsonBody, result, true);
    }

    @Test
    void should_throw_when_variable_name_not_found() {
        ResponseFieldTemplateDescriptor templateDescriptor = templatedResponseField("id").replacedWithUriTemplateVariableValue("someId");
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                singletonList(templateDescriptor),
                new UriTemplate("http://localhost/api/things/{someOtherId}"),
                jsonBody);

        assertThrows(IllegalArgumentException.class, templateProcessor::replaceTemplateFields);
    }
}
