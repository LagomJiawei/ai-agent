package com.zjw.agent.model;

/**
 * 代理执行状态的枚举类
 *
 * @author ZhangJw
 * @date 2026年05月11日 8:40
 */
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}