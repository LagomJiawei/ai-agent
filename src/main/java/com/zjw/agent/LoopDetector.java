package com.zjw.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能体循环检测器 - 检测和防止Agent陷入无限循环
 *
 * 检测策略：
 * 1. 重复消息检测 - 检测Assistant消息的完全重复
 * 2. 工具调用循环检测 - 检测相同工具调用序列的重复
 * 3. 无进展检测 - 检测连续多次没有实质性进展
 * 
 * @author ZhangJw
 * @date 2026年05月12日 8:05
 */
@Slf4j
@Data
public class LoopDetector {

    /**
     * 重复消息阈值--连续出现相同消息的次数
     */
    private int duplicateThreshold = 2;

    /**
     * 工具调用循环阈值--相同工具调用模式重复次数
     */
    private int toolCallLoopThreshold = 3;

    /**
     * 无进展阈值--连续无进展的步骤数
     */
    private int noProgressThreshold = 4;

    /**
     * 执行历史记录--记录每一步的详细信息
     */
    private List<ExecutionRecord> executionHistory = new ArrayList<>();

    /**
     * 执行记录--记录单次执行的详细信息
     */
    @Data
    public static class ExecutionRecord {
        private int stepNumber;
        private String assistantMessage;
        private List<ToolCallRecord> toolCallRecords;
        private long timestamp;
        private boolean hasProgress;

        public ExecutionRecord(int stepNumber, String assistantMessage, List<ToolCallRecord> toolCallRecords) {
            this.stepNumber = stepNumber;
            this.assistantMessage = assistantMessage;
            this.toolCallRecords = toolCallRecords != null ? toolCallRecords : new ArrayList<>();
            this.timestamp = System.currentTimeMillis();
            this.hasProgress = true; // 默认有进展，可根据实际情况调整
        }

        /**
         * 获取工具调用签名（用于比较工具调用模式）
         */
        public String getToolCallSignature() {
            if (CollectionUtils.isEmpty(toolCallRecords)) {
                return "NO_TOOLS";
            }
            return toolCallRecords.stream()
                    .map(tc -> tc.getToolName() + ":" + tc.getArgumentsHash())
                    .sorted()
                    .collect(Collectors.joining("|"));
        }
    }

    /**
     * 工具调用记录
     */
    @Data
    public static class ToolCallRecord {
        private String toolName;
        private String arguments;
        private String argumentsHash;

        public ToolCallRecord(String toolName, String arguments) {
            this.toolName = toolName;
            this.arguments = arguments;
            this.argumentsHash = Integer.toHexString(arguments != null ? arguments.hashCode() : 0);
        }
    }

    /**
     * 循环检测结果
     */
    @Data
    public static class LoopDetectionResult {
        private boolean isStuck;
        private LoopType loopType;
        private String description;
        private List<String> suggestions;

        public enum LoopType {
            NONE,               // 无循环
            DUPLICATE_MESSAGE,  // 重复消息
            TOOL_CALL_LOOP,     // 工具调用循环
            NO_PROGRESS         // 无进展
        }

        public static LoopDetectionResult notStuck() {
            LoopDetectionResult result = new LoopDetectionResult();
            result.isStuck = false;
            result.loopType = LoopType.NONE;
            result.description = "未检测到循环";
            result.suggestions = new ArrayList<>();
            return result;
        }

        public static LoopDetectionResult stuck(LoopType type, String description, List<String> suggestions) {
            LoopDetectionResult result = new LoopDetectionResult();
            result.isStuck = true;
            result.loopType = type;
            result.description = description;
            result.suggestions = suggestions;
            return result;
        }
    }

    /**
     * 记录执行步骤
     *
     * @param stepNumber 步骤编号
     * @param assistantMessage AI助手消息
     * @param toolCallRecords 工具调用记录列表
     */
    public void recordExecution(int stepNumber, String assistantMessage, List<ToolCallRecord> toolCallRecords) {
        ExecutionRecord record = new ExecutionRecord(stepNumber, assistantMessage, toolCallRecords);
        executionHistory.add(record);
        log.debug("📝 Recorded execution step {}: toolCallRecords={}, message_length={}",
                stepNumber,
                toolCallRecords != null ? toolCallRecords.size() : 0,
                assistantMessage != null ? assistantMessage.length() : 0);
    }

    /**
     * 检查是否陷入循环
     *
     * @return 循环检测结果
     */
    public LoopDetectionResult checkForLoop() {
        if (executionHistory.size() < 2) {
            return LoopDetectionResult.notStuck();
        }

        // 1. 检查重复消息
        LoopDetectionResult duplicateResult = this.checkDuplicateMessages();
        if (duplicateResult.isStuck()) {
            return duplicateResult;
        }

        // 2. 检查工具调用循环
        LoopDetectionResult toolCallResult = this.checkToolCallLoops();
        if (toolCallResult.isStuck()) {
            return toolCallResult;
        }

        // 3. 检查无进展状态
        LoopDetectionResult progressResult = this.checkNoProgress();
        if (progressResult.isStuck()) {
            return progressResult;
        }

        return LoopDetectionResult.notStuck();
    }

    /**
     * 检查重复消息
     */
    private LoopDetectionResult checkDuplicateMessages() {
        if (executionHistory.size() < duplicateThreshold + 1) {
            return LoopDetectionResult.notStuck();
        }

        ExecutionRecord lastRecord = executionHistory.getLast();
        if (lastRecord.getAssistantMessage() == null || lastRecord.getAssistantMessage().trim().isEmpty()) {
            return LoopDetectionResult.notStuck();
        }

        int duplicateCount = 0;
        for (int i = executionHistory.size() - 2; i >= 0; i--) {
            ExecutionRecord record = executionHistory.get(i);
            if (lastRecord.getAssistantMessage().equals(record.getAssistantMessage())) {
                duplicateCount++;
                if (duplicateCount >= duplicateThreshold) {
                    String message = String.format("检测到重复消息（重复%d次）", duplicateCount);
                    List<String> suggestions = Arrays.asList(
                            "尝试使用不同的工具或方法",
                            "重新审视问题的解决方案",
                            "考虑分解任务为更小的步骤"
                    );
                    log.warn("⚠️ {}", message);
                    return LoopDetectionResult.stuck(
                            LoopDetectionResult.LoopType.DUPLICATE_MESSAGE,
                            message,
                            suggestions
                    );
                }
            } else {
                break; // 只检查连续的重复
            }
        }

        return LoopDetectionResult.notStuck();
    }

    /**
     * 检查工具调用循环
     */
    private LoopDetectionResult checkToolCallLoops() {
        if (executionHistory.size() < toolCallLoopThreshold) {
            return LoopDetectionResult.notStuck();
        }

        // 检查最近的工具调用模式是否重复
        List<String> recentSignatures = new ArrayList<>();
        int checkCount = Math.min(toolCallLoopThreshold * 2, executionHistory.size());

        for (int i = executionHistory.size() - checkCount; i < executionHistory.size(); i++) {
            recentSignatures.add(executionHistory.get(i).getToolCallSignature());
        }

        // 检查是否有重复的模式
        Map<String, Integer> signatureCount = new HashMap<>();
        for (String sig : recentSignatures) {
            signatureCount.put(sig, signatureCount.getOrDefault(sig, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : signatureCount.entrySet()) {
            if (entry.getValue() >= toolCallLoopThreshold && !"NO_TOOLS".equals(entry.getKey())) {
                String message = String.format("检测到工具调用循环（模式'%s'重复%d次）",
                        entry.getKey(), entry.getValue());
                List<String> suggestions = Arrays.asList(
                        "检查工具参数是否正确",
                        "尝试使用其他可用工具",
                        "验证工具返回结果是否符合预期"
                );
                log.warn("⚠️ {}", message);
                return LoopDetectionResult.stuck(
                        LoopDetectionResult.LoopType.TOOL_CALL_LOOP,
                        message,
                        suggestions
                );
            }
        }

        return LoopDetectionResult.notStuck();
    }

    /**
     * 检查无进展状态
     */
    private LoopDetectionResult checkNoProgress() {
        if (executionHistory.size() < noProgressThreshold) {
            return LoopDetectionResult.notStuck();
        }

        // 检查最近几步是否都没有进展
        int noProgressCount = 0;
        for (int i = executionHistory.size() - noProgressThreshold; i < executionHistory.size(); i++) {
            if (!executionHistory.get(i).isHasProgress()) {
                noProgressCount++;
            }
        }

        if (noProgressCount >= noProgressThreshold) {
            String message = String.format("检测到无进展状态（连续%d步无进展）", noProgressCount);
            List<String> suggestions = Arrays.asList(
                    "重新评估当前策略",
                    "考虑改变解决问题的方法",
                    "检查是否有可用的替代方案"
            );
            log.warn("⚠️ {}", message);
            return LoopDetectionResult.stuck(
                    LoopDetectionResult.LoopType.NO_PROGRESS,
                    message,
                    suggestions
            );
        }

        return LoopDetectionResult.notStuck();
    }

    /**
     * 生成循环诊断报告
     *
     * @return 诊断报告文本
     */
    public String generateDiagnosticReport() {
        if (executionHistory.isEmpty()) {
            return "无执行历史记录";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== 执行历史诊断报告 ===\n");
        report.append(String.format("总执行步骤: %d\n", executionHistory.size()));
        report.append(String.format("执行时长: %d ms\n",
                executionHistory.getLast().getTimestamp() -
                        executionHistory.getFirst().getTimestamp()));
        report.append("\n最近5步执行情况:\n");

        int startIndex = Math.max(0, executionHistory.size() - 5);
        for (int i = startIndex; i < executionHistory.size(); i++) {
            ExecutionRecord record = executionHistory.get(i);
            report.append(String.format("  步骤 %d: 工具调用=%d, 消息长度=%d\n",
                    record.getStepNumber(),
                    record.getToolCallRecords() != null ? record.getToolCallRecords().size() : 0,
                    record.getAssistantMessage() != null ? record.getAssistantMessage().length() : 0));
        }

        return report.toString();
    }

    /**
     * 重置检测器状态
     */
    public void reset() {
        executionHistory.clear();
        log.info("🔄 Loop detector reset");
    }
}