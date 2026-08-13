package com.asrevo.cvhome.content.entity.banner;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerTargetKind;
import com.asrevo.cvhome.content.model.banner.LoginTarget;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_BANNER")
@Getter
@Setter
public class ContentBanner {
    @Id
    @Column(name = "CONTENT_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CONTENT_ID")
    private Content content;
    @Enumerated(EnumType.STRING)
    @Column(name = "PLACEMENT", nullable = false, length = 30)
    private BannerPlacement placement;
    @Column(name = "POSITION", nullable = false)
    private int position;
    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_KIND", nullable = false, length = 30)
    private BannerTargetKind targetKind;
    @Column(name = "TARGET_VALUE", length = 1000)
    private String targetValue;
    @Column(name = "BACKGROUND_COLOR", length = 20)
    private String backgroundColor;
    @Column(name = "FOREGROUND_COLOR", length = 20)
    private String foregroundColor;
    @Enumerated(EnumType.STRING)
    @Column(name = "LOGGED_IN_TARGET", nullable = false, length = 20)
    private LoginTarget loginTarget;
    @ElementCollection
    @CollectionTable(name = "BANNER_COUNTRY", joinColumns = @JoinColumn(name = "CONTENT_ID"))
    @Column(name = "COUNTRY_CODE", nullable = false, length = 2)
    private Set<String> countryCodes = new HashSet<>();
    @OneToMany(mappedBy = "banner", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Set<BannerArtwork> artworks = new HashSet<>();

    public void addArtwork(BannerArtwork artwork) {
        artwork.setBanner(this);
        artworks.add(artwork);
    }
}
