package com.revise.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "revision_topics")
@Data
@NoArgsConstructor
public class RevisionTopic {

    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    // Links this topic directly to the owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    // Using TEXT so you have plenty of room for detailed descriptions
    @Column(columnDefinition = "TEXT")
    private String description;

    // Automatically creates a linked table (revision_topics_links) for our array of URLs
    @ElementCollection
    @CollectionTable(name = "topic_links", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "link")
    private List<String> links = new ArrayList<>();

    @Column(nullable = false)
    private int stage = 1;

    @Column(name = "last_revision_date")
    private LocalDateTime lastRevisionDate;

    @Column(name = "next_revision_date", nullable = false)
    private LocalDateTime nextRevisionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
