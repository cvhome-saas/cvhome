package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** The requested parent would make the role inherit from itself. */
public class RoleInheritanceCycleException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RoleInheritanceCycleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static RoleInheritanceCycleException of(String name, String parent) {
        return new ErrorBuilder<>(UaaErrors.ROLE_INHERITANCE_CYCLE, RoleInheritanceCycleException::new)
                .detail("%s cannot inherit from %s: that would form a cycle.", name, parent)
                .param("roleName", name)
                .param("parentName", parent)
                .build();
    }

}
