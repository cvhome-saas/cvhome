package com.asrevo.cvhome.content.model.content;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public abstract class Content implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	@NotEmpty
	private String name;

	private String contentType;

	public Content() {
	}

	public Content(String name) {
		this.name = name;
	}

	public Content(String name, String contentType) {
		this.name = name;
		this.contentType = contentType;
	}

}
