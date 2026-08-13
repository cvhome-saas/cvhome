package com.asrevo.cvhome.content.entity.page;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.asrevo.cvhome.content.entity.content.Content;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_PAGE")
@Getter
@Setter
public class ContentPage {
    @Id
    @Column(name = "CONTENT_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CONTENT_ID")
    private Content content;
    @Column(name = "TEMPLATE", nullable = false, length = 100)
    private String template;
    @Column(name = "SHOW_IN_SITEMAP", nullable = false)
    private boolean showInSitemap;
    @Column(name = "PARENT_PAGE_ID")
    private Long parentPageId;
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageBlock> blocks = new ArrayList<>();

    public void addBlock(PageBlock block) {
        block.setPage(this);
        blocks.add(block);
    }
}
