package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "rank_history")
public class RankHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @NotNull
    private Integer rank;

    @Column(name = "elo_rating", nullable = false)
    private Integer eloRating = 1200;

    @Size(max = 255)
    private String region;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Size(max = 50)
    private String season;

    // Constructors
    public RankHistory() {}

    public RankHistory(Player player, Game game, Integer rank, Integer eloRating) {
        this.player = player;
        this.game = game;
        this.rank = rank;
        this.eloRating = eloRating;
        this.recordedAt = LocalDateTime.now();
    }

    public RankHistory(Player player, Game game, Integer rank, Integer eloRating, String region, String season) {
        this.player = player;
        this.game = game;
        this.rank = rank;
        this.eloRating = eloRating;
        this.region = region;
        this.season = season;
        this.recordedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public Integer getEloRating() { return eloRating; }
    public void setEloRating(Integer eloRating) { this.eloRating = eloRating; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
}
