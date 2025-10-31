package com.asrevo.cvhome.store.core.entity.common.audit;

import com.asrevo.cvhome.store.core.utils.CloneUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;

@Embeddable
public class AuditSection implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATED")
	private Date dateCreated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFIED")
	private Date dateModified;

	@Getter
	@Column(name = "UPDT_ID", length = 60)
	private String modifiedBy;

	public AuditSection() {
	}

	public Date getDateCreated() {
		return CloneUtils.clone(dateCreated);
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = CloneUtils.clone(dateCreated);
	}

	public Date getDateModified() {
		return CloneUtils.clone(dateModified);
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = CloneUtils.clone(dateModified);
	}

	public void setModifiedBy(String modifiedBy) {
		if (!modifiedBy.isBlank()) { // TODO
			if (modifiedBy.length() > 20) {
				modifiedBy = modifiedBy.substring(0, 20);
			}
		}
		this.modifiedBy = modifiedBy;
	}

}
