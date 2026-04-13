package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coach")
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 100)
    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(columnDefinition = "LONGTEXT")
    private String bio;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "price_per_session", precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL)
    private List<CoachingSession> coachingSessions;

    // Constructors
    public Coach() {}

    public Coach(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public Coach(User user, String experienceLevel, String bio, 
                 BigDecimal rating, BigDecimal pricePerSession) {
        this.user = user;
        this.experienceLevel = experienceLevel;
        this.bio = bio;
        this.rating = rating;
        this.pricePerSession = pricePerSession;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public BigDecimal getPricePerSession() { return pricePerSession; }
    public void setPricePerSession(BigDecimal pricePerSession) { this.pricePerSession = pricePerSession; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<CoachingSession> getCoachingSessions() { return coachingSessions; }
    public void setCoachingSessions(List<CoachingSession> coachingSessions) { this.coachingSessions = coachingSessions; }
}
