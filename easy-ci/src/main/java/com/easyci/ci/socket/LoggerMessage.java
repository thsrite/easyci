package com.easyci.ci.socket;

import lombok.*;

/**
 * 日志消息实体
 */
@Data
@ToString
@AllArgsConstructor
public class LoggerMessage {
    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;
    private String exception;
    private String cause;
}
