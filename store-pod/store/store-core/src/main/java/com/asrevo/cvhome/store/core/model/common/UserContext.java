package com.asrevo.cvhome.store.core.model.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class UserContext implements AutoCloseable {

    private static ThreadLocal<UserContext> instance = new ThreadLocal<>();
    private String ipAddress;

    private UserContext() {}

    public static UserContext create() {
        UserContext context = new UserContext();
        instance.set(context);
        return context;
    }

    public static UserContext getCurrentInstance() {
        return instance.get();
    }

    @Override
    public void close() throws Exception {
        instance.remove();
    }
}
