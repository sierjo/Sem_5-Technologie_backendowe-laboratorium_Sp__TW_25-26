package com.project.jobportal.dto;

//DTO для уведомления
public class NotificationDTO {
    private String content;
    private String type;
    private Integer targetId; // ID вакансии или пользователя

    // Getters and Setters, Constructor
    public NotificationDTO(String content, String type, Integer targetId) {
        this.content = content;
        this.type = type;
        this.targetId = targetId;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }
}