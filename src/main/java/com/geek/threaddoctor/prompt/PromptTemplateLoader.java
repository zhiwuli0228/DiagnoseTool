/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Component
public class PromptTemplateLoader {
    private final PromptProperties properties;
    private final Map<PromptTemplateType, PromptTemplate> cache = new ConcurrentHashMap<>();

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public PromptTemplateLoader(PromptProperties properties) {
        this.properties = properties;
    }

    /**
     * 加载提示词模板。
     *
     * @param type 类型
     * @return 业务处理结果
     */
    public PromptTemplate load(PromptTemplateType type) {
        if (properties.cacheEnabled()) {
            return cache.computeIfAbsent(type, this::loadUncached);
        }
        return loadUncached(type);
    }

    private PromptTemplate loadUncached(PromptTemplateType type) {
        Path external = externalPath(type);
        if (external != null && Files.exists(external)) {
            return loadExternal(type, external);
        }
        return loadClasspath(type);
    }

    private Path externalPath(PromptTemplateType type) {
        if (properties.templateDir() == null || properties.templateDir().isBlank()) {
            return null;
        }
        return Path.of(properties.templateDir()).resolve(type.defaultPath()).normalize();
    }

    private PromptTemplate loadExternal(PromptTemplateType type, Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new PromptTemplate(type, path.toString(), content, type.contentType(), PromptTemplateSource.EXTERNAL_FILE, Instant.now());
        } catch (IOException ex) {
            throw new PromptTemplateLoadException(type, path.toString(), ex);
        }
    }

    private PromptTemplate loadClasspath(PromptTemplateType type) {
        ClassPathResource resource = new ClassPathResource(type.defaultPath());
        if (!resource.exists()) {
            throw new PromptTemplateNotFoundException(type, type.defaultPath());
        }
        try (var stream = resource.getInputStream()) {
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return new PromptTemplate(type, type.defaultPath(), content, type.contentType(), PromptTemplateSource.CLASSPATH, Instant.now());
        } catch (IOException ex) {
            throw new PromptTemplateLoadException(type, type.defaultPath(), ex);
        }
    }
}
