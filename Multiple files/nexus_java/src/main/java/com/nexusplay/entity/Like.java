package com.nexusplay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;














@Entity
@Table(name = "post_likes", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "post_id"}), @UniqueConstraint(columnNames = {"user_id", "content_id"}), @UniqueConstraint(columnNames = {"user_id", "stream_id"})})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private ForumPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private Stream stream;

    @NotBlank
    @Size(max = 10)
    private String type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Like() {}

    public Like(User user, ForumPost post, String type) {
        this.user = user;
        this.post = post;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Like(User user, Content content, String type) {
        this.user = user;
        this.content = content;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Like(User user, Stream stream, String type) {
        this.user = user;
        this.stream = stream;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ForumPost getPost() { return post; }
    public void setPost(ForumPost post) { this.post = post; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public Stream getStream() { return stream; }
    public void setStream(Stream stream) { this.stream = stream; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
