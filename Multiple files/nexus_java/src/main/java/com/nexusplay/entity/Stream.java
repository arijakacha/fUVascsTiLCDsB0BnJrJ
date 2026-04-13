package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "stream")
public class Stream {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String url;

    @Column(name = "is_live", nullable = false)
    private Boolean isLive = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reunion_date")
    private LocalDateTime reunionDate;

    // Constructors
    public Stream() {}

    public Stream(Player player, String title, String url) {
        this.player = player;
        this.title = title;
        this.url = url;
        this.createdAt = LocalDateTime.now();
    }

    public Stream(Player player, String title, String url, Boolean isLive) {
        this.player = player;
        this.title = title;
        this.url = url;
        this.isLive = isLive;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Boolean getIsLive() { return isLive; }
    public void setIsLive(Boolean isLive) { this.isLive = isLive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReunionDate() { return reunionDate; }
    public void setReunionDate(LocalDateTime reunionDate) { this.reunionDate = reunionDate; }
}
