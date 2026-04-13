package com.nexusplay.dto;

import java.util.Map;

public class AdminDashboardDTO {
    private Map<String, Object> statistics;
    private Map<String, Object> recentActivity;
    private Map<String, Object> systemHealth;
    private Map<String, Object> alerts;

    // Constructors
    public AdminDashboardDTO() {}

    public AdminDashboardDTO(Map<String, Object> statistics, 
                           Map<String, Object> recentActivity,
                           Map<String, Object> systemHealth,
                           Map<String, Object> alerts) {
        this.statistics = statistics;
        this.recentActivity = recentActivity;
        this.systemHealth = systemHealth;
        this.alerts = alerts;
    }

    // Getters and Setters
    public Map<String, Object> getStatistics() { return statistics; }
    public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }

    public Map<String, Object> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(Map<String, Object> recentActivity) { this.recentActivity = recentActivity; }

    public Map<String, Object> getSystemHealth() { return systemHealth; }
    public void setSystemHealth(Map<String, Object> systemHealth) { this.systemHealth = systemHealth; }

    public Map<String, Object> getAlerts() { return alerts; }
    public void setAlerts(Map<String, Object> alerts) { this.alerts = alerts; }
}
