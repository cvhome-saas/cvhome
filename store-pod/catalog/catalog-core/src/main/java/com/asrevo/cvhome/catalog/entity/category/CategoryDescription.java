package com.asrevo.cvhome.catalog.entity.category;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CATEGORY_DESCRIPTION",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "CATEGORY_ID", "LANGUAGE_CODE" }) })
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
		valueColumnName = "SEQ_COUNT", pkColumnValue = "CATEGORY_DESCRIPTION_SEQ_NEXT_VAL",
		allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
		initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class CategoryDescription extends Description {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@ManyToOne(targetEntity = Category.class)
	@JoinColumn(name = "CATEGORY_ID", nullable = false)
	private Category category;

	@Column(name = "SEF_URL", length = 120)
	private String seUrl;

	@Column(name = "CATEGORY_HIGHLIGHT")
	private String categoryHighlight;

	@Column(name = "META_TITLE", length = 120)
	private String metatagTitle;

	@Column(name = "META_KEYWORDS")
	private String metatagKeywords;

	@Column(name = "META_DESCRIPTION")
	private String metatagDescription;

	public CategoryDescription() {
	}

}
