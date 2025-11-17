package com.asrevo.cvhome.controlplane.manager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.manager.commons.event.store.OrgCreatedEvent;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(schema = "manager", name = "manager_org")
public class ManagerOrgEntity extends BaseEntity<ManagerOrgEntity, ManagerOrgId> {

	@Column("created_date")
	private Instant createdDate;

	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	private Email email;

	public static ManagerOrgEntity createOrgFromUser(Email email) {
		ManagerOrgEntity entity = new ManagerOrgEntity();
		entity.id = entity.generateId();
		entity.setCreatedDate(Instant.now());
		entity.setEmail(email);
		entity.registerEvent(OrgCreatedEvent.from(entity.getId()));
		return entity;
	}

	@Override
	protected ManagerOrgId generateId() {
		return ManagerOrgId.newId();
	}

}
