package com.asrevo.cvhome.store.core.entity.catalog.marketplace;


import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import lombok.Getter;
import lombok.Setter;


/*@Entity
@Table(name="CATEGORY_DESCRIPTION",uniqueConstraints={
		@UniqueConstraint(columnNames={
			"CATEGORY_ID",
			"LANGUAGE_ID"
		})
	}
)*/
@Getter
@Setter
public class CatalogDescription extends Description {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /*	@ManyToOne(targetEntity = Catalog.class)
    @JoinColumn(name = "CATALOG_ID", nullable = false)*/
    private Catalog catalog;


    public CatalogDescription() {
    }

    public CatalogDescription(String name, Language language) {
        this.setName(name);
        this.setLanguage(language);
        super.setId(0L);
    }


}
