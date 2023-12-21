package org.revo.streamer.actuate;

import lombok.Getter;
import org.revo.streamer.codec.rtsp.RtspSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class SessionsAggregatorInfo {
    private final int count;
    private final List<SessionInfo> sessionInfos;

    public SessionsAggregatorInfo(Map<String, RtspSession> sessions) {
        this.count = sessions.size();
        this.sessionInfos = sessions.values().stream().map(SessionInfo::new).collect(Collectors.toList());
    }
}
