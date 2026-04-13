package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "player", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"nickname", "game_id"}))
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @NotBlank
    @Size(max = 100)
    private String nickname;

    @Size(max = 150)
    @Column(name = "real_name")
    private String realName;

    @Size(max = 80)
    private String role;

    @Size(max = 80)
    private String nationality;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Integer score = 0;

    @Column(name = "is_pro", nullable = false)
    private Boolean isPro = false;

    @Column(name = "phone_number")
    @Size(max = 20)
    private String phoneNumber;

    @Column(name = "sms_consent", nullable = false)
    private Boolean smsConsent = false;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<MatchPlayer> matchPlayers;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<PlayerAchievement> playerAchievements;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<RankHistory> rankHistories;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Statistic> statistics;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Stream> streams;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<TeamInvitation> teamInvitations;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<CoachingSession> coachingSessionsAsPlayer;

    // Constructors
    public Player() {}

    public Player(String nickname, Game game) {
        this.nickname = nickname;
        this.game = game;
        this.createdAt = LocalDateTime.now();
    }

    public Player(String nickname, String realName, Game game) {
        this.nickname = nickname;
        this.realName = realName;
        this.game = game;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Boolean getIsPro() { return isPro; }
    public void setIsPro(Boolean isPro) { this.isPro = isPro; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Boolean getSmsConsent() { return smsConsent; }
    public void setSmsConsent(Boolean smsConsent) { this.smsConsent = smsConsent; }

    public List<MatchPlayer> getMatchPlayers() { return matchPlayers; }
    public void setMatchPlayers(List<MatchPlayer> matchPlayers) { this.matchPlayers = matchPlayers; }

    public List<PlayerAchievement> getPlayerAchievements() { return playerAchievements; }
    public void setPlayerAchievements(List<PlayerAchievement> playerAchievements) { this.playerAchievements = playerAchievements; }

    public List<RankHistory> getRankHistories() { return rankHistories; }
    public void setRankHistories(List<RankHistory> rankHistories) { this.rankHistories = rankHistories; }

    public List<Statistic> getStatistics() { return statistics; }
    public void setStatistics(List<Statistic> statistics) { this.statistics = statistics; }

    public List<Stream> getStreams() { return streams; }
    public void setStreams(List<Stream> streams) { this.streams = streams; }

    public List<TeamInvitation> getTeamInvitations() { return teamInvitations; }
    public void setTeamInvitations(List<TeamInvitation> teamInvitations) { this.teamInvitations = teamInvitations; }

    public List<CoachingSession> getCoachingSessionsAsPlayer() { return coachingSessionsAsPlayer; }
    public void setCoachingSessionsAsPlayer(List<CoachingSession> coachingSessionsAsPlayer) { this.coachingSessionsAsPlayer = coachingSessionsAsPlayer; }
}
