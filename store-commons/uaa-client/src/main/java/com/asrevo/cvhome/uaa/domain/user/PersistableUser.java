package com.asrevo.cvhome.uaa.domain.user;

import com.asrevo.cvhome.uaa.domain.group.PersistableGroup;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableUser extends UserEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String password;

	private String repeatPassword;

	private String store;

	private String userName;

	private boolean active;

	private List<PersistableGroup> groups = new ArrayList<>();

}
