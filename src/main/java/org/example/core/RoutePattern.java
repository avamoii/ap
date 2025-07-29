// 3. Route Pattern Matcher - Handles dynamic parameters like /:id
package org.example.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutePattern {
    private final String originalPattern;
    private final Pattern compiledPattern;
    private final List<String> paramNames;

    public RoutePattern(String pattern) {
        this.originalPattern = pattern;
        this.paramNames = extractParamNames(pattern);
        this.compiledPattern = compilePattern(pattern);
    }

    public boolean matches(String path) {
        return compiledPattern.matcher(path).matches();
    }

    public Map<String, String> extractParams(String path) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = compiledPattern.matcher(path);

        if (matcher.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                params.put(paramNames.get(i), matcher.group(i + 1));
            }
        }

        return params;
    }

    private List<String> extractParamNames(String pattern) {
        List<String> names = new ArrayList<>();
        Pattern paramPattern = Pattern.compile(":([^/]+)");
        Matcher matcher = paramPattern.matcher(pattern);

        while (matcher.find()) {
            names.add(matcher.group(1));
        }

        return names;
    }

    private Pattern compilePattern(String pattern) {
        String regex = pattern.replaceAll(":([^/]+)", "([^/]+)");
        return Pattern.compile("^" + regex + "$");
    }

    @Override
    public String toString() {
        return originalPattern;
    }
}