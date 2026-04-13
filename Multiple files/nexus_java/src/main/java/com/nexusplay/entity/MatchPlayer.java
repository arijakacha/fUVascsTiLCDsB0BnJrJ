package com.nexusplay.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "match_player")
public class MatchPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_match_id", nullable = false)
    private GameMatch gameMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer kills = 0;

    private Integer deaths = 0;

    private Integer assists = 0;

    @Column(name = "position_x", precision = 10, scale = 2)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 10, scale = 2)
    private BigDecimal positionY;

    @Column(name = "is_winner", nullable = false)
    private Boolean isWinner = false;

    @Column(name = "elo_change")
    private Integer eloChange;

    // Constructors
    public MatchPlayer() {}

    public MatchPlayer(GameMatch gameMatch, Player player, Integer kills, Integer deaths, Integer assists) {
        this.gameMatch = gameMatch;
        this.player = player;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public GameMatch getGameMatch() { return gameMatch; }
    public void setGameMatch(GameMatch gameMatch) { this.gameMatch = gameMatch; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Integer getKills() { return kills; }
    public void setKills(Integer kills) { this.kills = kills; }

    public Integer getDeaths() { return deaths; }
    public void setDeaths(Integer deaths) { this.deaths = deaths; }

    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }

    public BigDecimal getPositionX() { return positionX; }
    public void setPositionX(BigDecimal positionX) { this.positionX = positionX; }

    public BigDecimal getPositionY() { return positionY; }
    public void setPositionY(BigDecimal positionY) { this.positionY = positionY; }

    public Boolean getIsWinner() { return isWinner; }
    public void setIsWinner(Boolean isWinner) { this.isWinner = isWinner; }

    public Integer getEloChange() { return eloChange; }
    public void setEloChange(Integer eloChange) { this.eloChange = eloChange; }
}
