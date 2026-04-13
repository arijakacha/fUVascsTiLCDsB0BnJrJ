package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 150)
    @Column(unique = true)
    private String name;

    private String logo;

    private String description;

    @Column(name = "release_year")
    private Short releaseYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    // private List<Player> players;

    // @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    // private List<Team> teams;

    // @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    // private List<Achievement> achievements;

    // @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    // private List<GameMatch> matches;

    // @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    // private List<Statistic> statistics;

    // Constructors
    public Game() {}

    public Game(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Game(String name, String description, Short releaseYear) {
        this.name = name;
        this.description = description;
        this.releaseYear = releaseYear;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Short getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Short releaseYear) { this.releaseYear = releaseYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // public List<Player> getPlayers() { return players; }
    // public void setPlayers(List<Player> players) { this.players = players; }

    // public List<Team> getTeams() { return teams; }
    // public void setTeams(List<Team> teams) { this.teams = teams; }

    // public List<Achievement> getAchievements() { return achievements; }
    // public void setAchievements(List<Achievement> achievements) { this.achievements = achievements; }

    // public List<GameMatch> getMatches() { return matches; }
    // public void setMatches(List<GameMatch> matches) { this.matches = matches; }

    // public List<Statistic> getStatistics() { return statistics; }
    // public void setStatistics(List<Statistic> statistics) { this.statistics = statistics; }
}
