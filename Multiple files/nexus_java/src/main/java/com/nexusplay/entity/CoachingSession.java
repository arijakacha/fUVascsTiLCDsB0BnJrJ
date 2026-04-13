package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "coaching_session")
public class CoachingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @NotBlank
    @Size(max = 20)
    private String status;

    @Column(columnDefinition = "LONGTEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "meeting_url")
    @Size(max = 255)
    private String meetingUrl;

    @Column(name = "meeting_room")
    @Size(max = 255)
    private String meetingRoom;

    @Column(name = "meeting_expires_at")
    private LocalDateTime meetingExpiresAt;

    // Constructors
    public CoachingSession() {}

    public CoachingSession(Coach coach, LocalDateTime scheduledAt, String status) {
        this.coach = coach;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public CoachingSession(Player player, Coach coach, LocalDateTime scheduledAt, 
                          String status, String notes) {
        this.player = player;
        this.coach = coach;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getMeetingRoom() { return meetingRoom; }
    public void setMeetingRoom(String meetingRoom) { this.meetingRoom = meetingRoom; }

    public LocalDateTime getMeetingExpiresAt() { return meetingExpiresAt; }
    public void setMeetingExpiresAt(LocalDateTime meetingExpiresAt) { this.meetingExpiresAt = meetingExpiresAt; }
}
