/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

import org.springframework.stereotype.Service;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class DiagnosisProgressService {
    private final DiagnosisProgressRepository repository;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     */
    public DiagnosisProgressService(DiagnosisProgressRepository repository) {
        this.repository = repository;
    }

    /**
     * 重置诊断进度。
     *
     * @param sessionId 会话标识
     * @return 重置后的诊断进度
     */
    public DiagnosisProgress reset(String sessionId) {
        DiagnosisProgress progress = DiagnosisProgress.notStarted(sessionId)
                .advance(DiagnosisProgressStep.STARTED, 10, "Diagnosis started.");
        return repository.save(progress);
    }

    /**
     * 推进诊断进度。
     *
     * @param sessionId 会话标识
     * @param step 业务参数
     * @param percent 业务参数
     * @param message 消息内容
     * @return 推进后的诊断进度
     */
    public DiagnosisProgress advance(String sessionId, DiagnosisProgressStep step, int percent, String message) {
        DiagnosisProgress current = current(sessionId);
        return repository.save(current.advance(step, percent, message));
    }

    /**
     * 完成当前操作。
     *
     * @param sessionId 会话标识
     * @return 操作结果
     */
    public DiagnosisProgress complete(String sessionId) {
        return repository.save(current(sessionId).complete("Diagnosis completed."));
    }

    /**
     * 标记诊断失败。
     *
     * @param sessionId 会话标识
     * @param error 错误信息
     * @return 失败状态的诊断进度
     */
    public DiagnosisProgress fail(String sessionId, Exception error) {
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        return repository.save(current(sessionId).fail("Diagnosis failed.", message));
    }

    /**
     * 获取当前诊断进度。
     *
     * @param sessionId 会话标识
     * @return 当前诊断进度
     */
    public DiagnosisProgress current(String sessionId) {
        return repository.findBySessionId(sessionId).orElseGet(() -> DiagnosisProgress.notStarted(sessionId));
    }
}
