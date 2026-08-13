package com.asrevo.cvhome.content.entity.banner;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class BannerArtworkId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "CONTENT_ID")
    private Long contentId;
    @Column(name = "LANGUAGE_CODE", length = 6)
    private String languageCode;
}
