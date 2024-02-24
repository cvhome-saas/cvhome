package com.asrevo.cvhome.commons.command;

import java.util.List;

public interface Command {
    default List<String> destinations() {
        return List.of();
    }
}
