package com.geek.threaddoctor.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PromptRenderer {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");
    private final ObjectMapper objectMapper;

    public PromptRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PromptRenderResult render(PromptRenderRequest request) {
        Matcher matcher = PLACEHOLDER.matcher(request.templateContent());
        StringBuffer rendered = new StringBuffer();
        Set<String> unresolved = new LinkedHashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            Object value = resolve(request.variables(), name);
            if (value == null) {
                unresolved.add(name);
                matcher.appendReplacement(rendered, Matcher.quoteReplacement(matcher.group(0)));
            } else {
                matcher.appendReplacement(rendered, Matcher.quoteReplacement(toText(request.templateType(), value)));
            }
        }
        matcher.appendTail(rendered);
        List<String> unresolvedList = unresolved.stream().sorted(Comparator.naturalOrder()).toList();
        if (request.strict() && !unresolvedList.isEmpty()) {
            throw new MissingPromptVariableException(request.templateType(), unresolvedList);
        }
        return new PromptRenderResult(request.templateType(), rendered.toString(), unresolvedList, Instant.now());
    }

    private Object resolve(Map<String, Object> variables, String name) {
        if (variables == null || variables.isEmpty()) {
            return null;
        }
        Object current = variables;
        for (String part : name.split("\\.")) {
            current = readPart(current, part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object readPart(Object current, String part) {
        if (current instanceof Map<?, ?> map) {
            return map.get(part);
        }
        if (current instanceof Iterable<?> && part.matches("\\d+")) {
            int index = Integer.parseInt(part);
            int currentIndex = 0;
            for (Object item : (Iterable<?>) current) {
                if (currentIndex++ == index) {
                    return item;
                }
            }
            return null;
        }
        if (current.getClass().isRecord()) {
            return readRecord(current, part);
        }
        return readBean(current, part);
    }

    private Object readRecord(Object current, String part) {
        for (RecordComponent component : current.getClass().getRecordComponents()) {
            if (component.getName().equals(part)) {
                try {
                    return component.getAccessor().invoke(current);
                } catch (ReflectiveOperationException ex) {
                    throw new PromptRenderException(null, "Unable to read record prompt variable: " + part, ex);
                }
            }
        }
        return null;
    }

    private Object readBean(Object current, String part) {
        List<String> names = new ArrayList<>();
        names.add(part);
        names.add("get" + Character.toUpperCase(part.charAt(0)) + part.substring(1));
        names.add("is" + Character.toUpperCase(part.charAt(0)) + part.substring(1));
        for (String methodName : names) {
            try {
                return current.getClass().getMethod(methodName).invoke(current);
            } catch (NoSuchMethodException ignored) {
                // Try the next JavaBean accessor shape.
            } catch (ReflectiveOperationException ex) {
                throw new PromptRenderException(null, "Unable to read bean prompt variable: " + part, ex);
            }
        }
        return null;
    }

    private String toText(PromptTemplateType type, Object value) {
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new PromptRenderException(type, "Unable to render prompt variable as JSON", ex);
        }
    }
}
