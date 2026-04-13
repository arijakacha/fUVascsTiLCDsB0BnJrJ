package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "forum_post")
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 500)
    @Column(length = 500)
    private String image;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Temporarily commented out @OneToMany relationships to prevent circular dependency issues during table creation
    // @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    // private List<GuideComment> guideComments;

    // @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    // private List<Like> likes;

    // @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    // private List<Reponse> reponses;

    // @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    // private List<Report> reports;

    // Constructors
    public ForumPost() {}

    public ForumPost(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public ForumPost(String title, String image, String content, User user) {
        this.title = title;
        this.image = image;
        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Temporarily commented out getters/setters for @OneToMany relationships
    // public List<GuideComment> getGuideComments() { return guideComments; }
    // public void setGuideComments(List<GuideComment> guideComments) { this.guideComments = guideComments; }

    // public List<Like> getLikes() { return likes; }
    // public void setLikes(List<Like> likes) { this.likes = likes; }

    // public List<Reponse> getReponses() { return reponses; }
    // public void setReponses(List<Reponse> reponses) { this.reponses = reponses; }

    // public List<Report> getReports() { return reports; }
    // public void setReports(List<Report> reports) { this.reports = reports; }
}
