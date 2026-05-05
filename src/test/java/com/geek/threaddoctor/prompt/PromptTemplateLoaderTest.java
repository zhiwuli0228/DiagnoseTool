package com.geek.threaddoctor.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PromptTemplateLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsClasspathTemplateByDefault() {
        PromptTemplateLoader loader = new PromptTemplateLoader(new PromptProperties(null, true, true, "zh-CN"));

        PromptTemplate template = loader.load(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT);

        assertThat(template.loadedFrom()).isEqualTo(PromptTemplateSource.CLASSPATH);
        assertThat(template.content()).contains("Thread Doctor");
    }

    @Test
    void externalTemplateOverridesClasspathTemplate() throws Exception {
        Path template = tempDir.resolve(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT.defaultPath());
        Files.createDirectories(template.getParent());
        Files.writeString(template, "external {{defaultOutputLanguage}}");
        PromptTemplateLoader loader = new PromptTemplateLoader(new PromptProperties(tempDir.toString(), true, true, "zh-CN"));

        PromptTemplate loaded = loader.load(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT);

        assertThat(loaded.loadedFrom()).isEqualTo(PromptTemplateSource.EXTERNAL_FILE);
        assertThat(loaded.content()).isEqualTo("external {{defaultOutputLanguage}}");
    }

    @Test
    void cacheCanBeDisabledForLocalDebugging() throws Exception {
        Path template = tempDir.resolve(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT.defaultPath());
        Files.createDirectories(template.getParent());
        Files.writeString(template, "v1");
        PromptTemplateLoader cached = new PromptTemplateLoader(new PromptProperties(tempDir.toString(), true, true, "zh-CN"));
        PromptTemplateLoader uncached = new PromptTemplateLoader(new PromptProperties(tempDir.toString(), false, true, "zh-CN"));

        assertThat(cached.load(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT).content()).isEqualTo("v1");
        assertThat(uncached.load(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT).content()).isEqualTo("v1");
        Files.writeString(template, "v2");

        assertThat(cached.load(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT).content()).isEqualTo("v1");
        assertThat(uncached.load(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT).content()).isEqualTo("v2");
    }
}
