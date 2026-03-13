package com.asrevo.cvhome.uaa.domain.group;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GroupEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String name;

	private String type;

}
