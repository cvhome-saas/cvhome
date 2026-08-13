package com.asrevo.cvhome.content.entity.banner;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BANNER_ARTWORK")
@Getter
@Setter
public class BannerArtwork {
    @EmbeddedId
    private BannerArtworkId id = new BannerArtworkId();
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("contentId")
    @JoinColumn(name = "CONTENT_ID")
    private ContentBanner banner;
    @Column(name = "DESKTOP_MEDIA_ID")
    private Long desktopMediaId;
    @Column(name = "MOBILE_MEDIA_ID")
    private Long mobileMediaId;
    @Column(name = "ALT_TEXT", length = 500)
    private String altText;

    public LanguageCode getLanguageCode() {
        return new LanguageCode(id.getLanguageCode());
    }

    public void setLanguageCode(LanguageCode languageCode) {
        id.setLanguageCode(languageCode.code());
    }
}
