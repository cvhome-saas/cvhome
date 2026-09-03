package com.asrevo.cvhome.uaa.dto;

/** The accept page's answer: who may now sign in, and where. */
public record AcceptedLink(String username, String loginUrl) {
}
