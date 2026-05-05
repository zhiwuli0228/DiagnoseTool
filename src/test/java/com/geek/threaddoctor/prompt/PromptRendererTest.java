package com.geek.threaddoctor.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptRendererTest {
    private final PromptRenderer renderer = new PromptRenderer(new ObjectMapper());

    @Test
    void rendersSimpleNestedAndJsonVariables() {
        PromptRenderResult result = renderer.render(new PromptRenderRequest(
                PromptTemplateType.DIAGNOSIS_USER_PROMPT,
                "goal={{goal}} id={{incident.sessionId}} json={{evidencePackJson}}",
                Map.of(
                        "goal", "diagnose",
                        "incident", Map.of("sessionId", "INC-1"),
                        "evidencePackJson", Map.of("count", 2)),
                true));

        assertThat(result.renderedContent())
                .contains("goal=diagnose")
                .contains("id=INC-1")
                .contains("\"count\" : 2");
        assertThat(result.unresolvedVariables()).isEmpty();
    }

    @Test
    void strictRenderingFailsOnMissingVariables() {
        assertThatThrownBy(() -> renderer.render(new PromptRenderRequest(
                PromptTemplateType.DIAGNOSIS_USER_PROMPT,
                "missing={{notFound}}",
                Map.of(),
                true)))
                .isInstanceOf(MissingPromptVariableException.class)
                .hasMessageContaining("notFound");
    }

    @Test
    void relaxedRenderingReportsMissingVariables() {
        PromptRenderResult result = renderer.render(new PromptRenderRequest(
                PromptTemplateType.DIAGNOSIS_USER_PROMPT,
                "missing={{notFound}}",
                Map.of(),
                false));

        assertThat(result.renderedContent()).contains("{{notFound}}");
        assertThat(result.unresolvedVariables()).containsExactly("notFound");
    }
}
