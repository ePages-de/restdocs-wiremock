package com.epages.restdocs;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.List;

import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

class ResponseTemplateProcessor {

    private final List<ResponseFieldTemplateDescriptor> templateDescriptors;
    private UriTemplate uriTemplate;
    private String responseBody;

    ResponseTemplateProcessor(List<ResponseFieldTemplateDescriptor> templateDescriptors, UriTemplate uriTemplate, String responseBody) {
        this.templateDescriptors = templateDescriptors;
        this.uriTemplate = uriTemplate;
        this.responseBody = responseBody;
    }

    String replaceTemplateFields() {
        if (templateDescriptors.isEmpty()) {
            return responseBody;
        }

        DocumentContext documentContext = JsonPath.parse(responseBody);

        for (ResponseFieldTemplateDescriptor descriptor: templateDescriptors) {
            String expression = null;
            if (uriTemplate != null && !uriTemplate.getVariableNames().isEmpty()) {
                expression = preProcessUriTemplateVariableNameExpression(descriptor);
            } else if (descriptor.getUriTemplateVariableName() != null) {
                throw new IllegalArgumentException("Descriptor for field '" + descriptor.getPath() + "' specifies a 'replacedWithUriTemplateVariableValue' but no URI Template could be found in. " +
                        "Make sure to construct your request with the methods in org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders that use URI templates");
            }
            if (expression == null) {
                if (descriptor.getWireMockTemplateExpression() == null) {
                    throw new IllegalArgumentException("Descriptor for field '" + descriptor.getPath() + "' contains no replacedWithWireMockTemplateExpression");
                }
                expression = "{{" + descriptor.getWireMockTemplateExpression() + "}}";
            }
            documentContext.set(descriptor.getPath(), expression);
        }
        return documentContext.jsonString();
    }

    private String preProcessUriTemplateVariableNameExpression(ResponseFieldTemplateDescriptor descriptor) {

        if (descriptor.getUriTemplateVariableName() != null) {
            return "{{request.requestLine.pathSegments.[" + getIndexOfUriVariableInPath(descriptor.getUriTemplateVariableName()) + "]}}";
        }
        return null;
    }

    private int getIndexOfUriVariableInPath(String variableName) {
        List<String> pathSegments = UriComponentsBuilder.fromUriString(uriTemplate.toString()).build().getPathSegments();
        for (int i = 0; i < pathSegments.size(); i++) {
            if (pathSegments.get(i).contains(variableName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find variableName '" + variableName + "' in URL Template - present variables are '" + uriTemplate.getVariableNames() + "'");
    }
}
