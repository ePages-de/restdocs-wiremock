package com.epages.restdocs;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

class JsonStringMatcher extends TypeSafeMatcher<String> {

    private final String expected;

    JsonStringMatcher(String expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(String actual) {
        try {
            JSONCompareResult result = JSONCompare.compareJSON(expected, actual, JSONCompareMode.STRICT);
            return !result.failed();
        } catch(JSONException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }

    public static Matcher<String> sameJSONAs(String expected) {
        return new JsonStringMatcher(expected);
    }
}
