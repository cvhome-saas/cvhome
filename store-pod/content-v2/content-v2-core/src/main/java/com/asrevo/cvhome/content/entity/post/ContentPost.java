package com.asrevo.cvhome.content.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.asrevo.cvhome.content.entity.content.Content;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_POST")
@Getter
@Setter
public class ContentPost {
    @Id
    @Column(name = "CONTENT_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CONTENT_ID")
    private Content content;
    @Column(name = "HERO_MEDIA_ID")
    private Long heroMediaId;
    @Column(name = "AUTHOR_SNAPSHOT", nullable = false, length = 255)
    private String authorSnapshot;
    @Column(name = "READING_MINUTES", nullable = false)
    private int readingMinutes;
    @Column(name = "FEATURED", nullable = false)
    private boolean featured;
    @Column(name = "EXCERPT", nullable = false, columnDefinition = "text")
    private String excerpt;
}
