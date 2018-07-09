package com.epages.restdocs;

import static com.epages.restdocs.WireMockDocumentation.templatedResponseField;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.springframework.web.util.UriTemplate;

public class ResponseTemplateProcessorTest {

    private String jsonBody = "{\n" +
            "  \"id\": \"the-id\",\n" +
            "  \"name\": \"some\"\n" +
            "}";

    @Test
    public void should_replace_with_uri_variable_expression() {
        ResponseFieldTemplateDescriptor templateDescriptor = templatedResponseField("id").replacedWithUriTemplateVariableValue("someId");
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                singletonList(templateDescriptor),
                new UriTemplate("http://localhost/api/things/{someId}"),
                jsonBody);

        String result = templateProcessor.replaceTemplateFields();

        assertThat(result, sameJSONAs("{\n" +
                "  \"id\": \"{{request.requestLine.pathSegments.[2]}}\",\n" +
                "  \"name\": \"some\"\n" +
                "}"));
    }

    @Test
    public void should_handle_multiple_descriptors() {
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                Arrays.asList(
                        templatedResponseField("id").replacedWithWireMockTemplateExpression("randomValue length=33 type='ALPHANUMERIC'"),
                        templatedResponseField("name").replacedWithWireMockTemplateExpression("randomValue type='UUID'")
                ),
                null,
                jsonBody);

        String result = templateProcessor.replaceTemplateFields();

        assertThat(result, sameJSONAs("{\n" +
                "  \"id\":\"{{randomValue length=33 type='ALPHANUMERIC'}}\",\n" +
                "  \"name\":\"{{randomValue type='UUID'}}\"\n" +
                "}"));
    }

    @Test
    public void should_return_response_on_empty_descriptors() {
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                Collections.<ResponseFieldTemplateDescriptor>emptyList(),
                null,
                jsonBody);

        String result = templateProcessor.replaceTemplateFields();

        assertThat(result, is(jsonBody));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_when_variable_name_not_found() {
        ResponseFieldTemplateDescriptor templateDescriptor = templatedResponseField("id").replacedWithUriTemplateVariableValue("someId");
        ResponseTemplateProcessor templateProcessor = new ResponseTemplateProcessor(
                singletonList(templateDescriptor),
                new UriTemplate("http://localhost/api/things/{someOtherId}"),
                jsonBody);

        templateProcessor.replaceTemplateFields();
    }
}
