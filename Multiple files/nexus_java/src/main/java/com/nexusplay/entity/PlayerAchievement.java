package com.nexusplay.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_achievement")
public class PlayerAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    private Integer progress = 0;

    @Column(name = "is_unlocked", nullable = false)
    private Boolean isUnlocked = false;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    // Constructors
    public PlayerAchievement() {}

    public PlayerAchievement(Player player, Achievement achievement) {
        this.player = player;
        this.achievement = achievement;
    }

    public PlayerAchievement(Player player, Achievement achievement, Integer progress, Boolean isUnlocked) {
        this.player = player;
        this.achievement = achievement;
        this.progress = progress;
        this.isUnlocked = isUnlocked;
        if (isUnlocked) {
            this.unlockedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Achievement getAchievement() { return achievement; }
    public void setAchievement(Achievement achievement) { this.achievement = achievement; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Boolean getIsUnlocked() { return isUnlocked; }
    public void setIsUnlocked(Boolean isUnlocked) { 
        this.isUnlocked = isUnlocked; 
        if (isUnlocked && this.unlockedAt == null) {
            this.unlockedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
