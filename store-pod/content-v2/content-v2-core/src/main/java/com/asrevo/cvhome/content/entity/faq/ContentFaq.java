package com.asrevo.cvhome.content.entity.faq;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.asrevo.cvhome.content.entity.content.Content;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_FAQ")
@Getter
@Setter
public class ContentFaq {
    @Id
    @Column(name = "CONTENT_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CONTENT_ID")
    private Content content;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    private FaqGroup group;
    @Column(name = "POSITION", nullable = false)
    private int position;
    @OneToMany(mappedBy = "faq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaqReference> references = new ArrayList<>();

    public void addReference(FaqReference reference) {
        reference.setFaq(this);
        references.add(reference);
    }
}
