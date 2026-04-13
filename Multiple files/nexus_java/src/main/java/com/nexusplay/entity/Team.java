package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "team", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"name", "game_id"}))
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @NotBlank
    @Size(max = 150)
    private String name;

    private String logo;

    @Size(max = 80)
    private String country;

    @Column(name = "foundation_year")
    private Short foundationYear;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> players;

    @OneToMany(mappedBy = "teamA", cascade = CascadeType.ALL)
    private List<GameMatch> matchesAsTeamA;

    @OneToMany(mappedBy = "teamB", cascade = CascadeType.ALL)
    private List<GameMatch> matchesAsTeamB;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Statistic> statistics;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<TeamInvitation> teamInvitations;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<MatchPlayer> matchPlayers;

    // Constructors
    public Team() {}

    public Team(String name, Game game) {
        this.name = name;
        this.game = game;
        this.createdAt = LocalDateTime.now();
    }

    public Team(String name, Game game, String description) {
        this.name = name;
        this.game = game;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Short getFoundationYear() { return foundationYear; }
    public void setFoundationYear(Short foundationYear) { this.foundationYear = foundationYear; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public List<GameMatch> getMatchesAsTeamA() { return matchesAsTeamA; }
    public void setMatchesAsTeamA(List<GameMatch> matchesAsTeamA) { this.matchesAsTeamA = matchesAsTeamA; }

    public List<GameMatch> getMatchesAsTeamB() { return matchesAsTeamB; }
    public void setMatchesAsTeamB(List<GameMatch> matchesAsTeamB) { this.matchesAsTeamB = matchesAsTeamB; }

    public List<Statistic> getStatistics() { return statistics; }
    public void setStatistics(List<Statistic> statistics) { this.statistics = statistics; }

    public List<TeamInvitation> getTeamInvitations() { return teamInvitations; }
    public void setTeamInvitations(List<TeamInvitation> teamInvitations) { this.teamInvitations = teamInvitations; }

    public List<MatchPlayer> getMatchPlayers() { return matchPlayers; }
    public void setMatchPlayers(List<MatchPlayer> matchPlayers) { this.matchPlayers = matchPlayers; }
}
