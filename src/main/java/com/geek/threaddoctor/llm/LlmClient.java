package com.geek.threaddoctor.llm;

public interface LlmClient {
    LlmResponse complete(LlmRequest request);
}
