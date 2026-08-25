package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorefrontLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String slug;

    private String title;

    private String href;

    private String type;

}
