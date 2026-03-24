package com.asrevo.cvhome.controlplane.org.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Getter
@Setter
@Table(schema = "org", name = "pod")
public class PodEntity extends BaseEntity<PodEntity, PodId> {

	@Column("name")
	private String name;

	@Column("endpoint")
	private String endpoint;

	@Column("endpoint_type")
	private EndpointType endpointType;

	@Column("org_id")
	private ManagerOrgId orgId;

	public static PodEntity newEntity(Pod pod) {
		PodEntity entity = new PodEntity();
		if (Objects.nonNull(pod.id())) {
			entity.setId(pod.id());
		}
		else {
			entity.setId(entity.generateId());
		}
		entity.setName(pod.name());
		entity.setEndpoint(pod.endpoint().endpoint());
		entity.setEndpointType(pod.endpoint().type());
		entity.setOrgId(pod.orgId());
		return entity;
	}

	@Override
	protected PodId generateId() {
		return PodId.newId();
	}

	public Pod toPod() {
		return new Pod(this.getId(), this.getName(), new PodEndpoint(this.getEndpoint(), this.getEndpointType()),
				this.getOrgId(), null);
	}

}
