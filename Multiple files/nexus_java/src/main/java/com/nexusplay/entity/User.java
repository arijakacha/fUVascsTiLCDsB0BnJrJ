package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 100)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "user_type", nullable = false, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.REGISTERED;

    @Column(name = "has_player", nullable = false)
    private Boolean hasPlayer = false;

    @Column(name = "riot_summoner_name")
    private String riotSummonerName;

    @Column(name = "riot_region")
    private String riotRegion;

    @Column(name = "riot_puuid")
    private String riotPuuid;

    @Column(name = "riot_summoner_id")
    private String riotSummonerId;

    @Column(name = "riot_last_sync_at")
    private LocalDateTime riotLastSyncAt;

    @Column(name = "recent_matches", columnDefinition = "LONGTEXT")
    private String recentMatches;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Player> players;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Coach> coaches;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Content> contents;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    private List<Report> reports;

    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL)
    private List<Report> reportedIn;

    public enum UserStatus {
        ACTIVE, BANNED
    }

    public enum UserType {
        REGISTERED, ADMIN, COACH, ORGANIZATION, VISITOR
    }

    // Constructors
    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public Boolean getHasPlayer() { return hasPlayer; }
    public void setHasPlayer(Boolean hasPlayer) { this.hasPlayer = hasPlayer; }

    public String getRiotSummonerName() { return riotSummonerName; }
    public void setRiotSummonerName(String riotSummonerName) { this.riotSummonerName = riotSummonerName; }

    public String getRiotRegion() { return riotRegion; }
    public void setRiotRegion(String riotRegion) { this.riotRegion = riotRegion; }

    public String getRiotPuuid() { return riotPuuid; }
    public void setRiotPuuid(String riotPuuid) { this.riotPuuid = riotPuuid; }

    public String getRiotSummonerId() { return riotSummonerId; }
    public void setRiotSummonerId(String riotSummonerId) { this.riotSummonerId = riotSummonerId; }

    public LocalDateTime getRiotLastSyncAt() { return riotLastSyncAt; }
    public void setRiotLastSyncAt(LocalDateTime riotLastSyncAt) { this.riotLastSyncAt = riotLastSyncAt; }

    public String getRecentMatches() { return recentMatches; }
    public void setRecentMatches(String recentMatches) { this.recentMatches = recentMatches; }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public List<Coach> getCoaches() { return coaches; }
    public void setCoaches(List<Coach> coaches) { this.coaches = coaches; }

    public List<Content> getContents() { return contents; }
    public void setContents(List<Content> contents) { this.contents = contents; }

    public List<Report> getReports() { return reports; }
    public void setReports(List<Report> reports) { this.reports = reports; }

    public List<Report> getReportedIn() { return reportedIn; }
    public void setReportedIn(List<Report> reportedIn) { this.reportedIn = reportedIn; }
}
