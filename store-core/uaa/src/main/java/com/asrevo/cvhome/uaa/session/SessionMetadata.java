package com.asrevo.cvhome.uaa.session;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * What uaa remembers about a session beyond what Spring Session stores: where it came from and how it started.
 * Written once at sign-in, read by the session lists.
 */
public final class SessionMetadata {

    public static final String IP = "uaa.ip";

    public static final String USER_AGENT = "uaa.ua";

    public static final String VIA = "uaa.via";

    public static final String CREATED_AT = "uaa.createdAt";

    private static final int MAX_USER_AGENT = 512;

    private SessionMetadata() {
    }

    public static void stamp(HttpSession session, HttpServletRequest request, String via) {
        session.setAttribute(IP, request.getRemoteAddr());
        String agent = request.getHeader("User-Agent");
        session.setAttribute(USER_AGENT, agent == null ? null
                : agent.substring(0, Math.min(agent.length(), MAX_USER_AGENT)));
        session.setAttribute(VIA, via);
        session.setAttribute(CREATED_AT, Instant.now().toEpochMilli());
    }

}
