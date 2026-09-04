package com.asrevo.cvhome.sso.dto;

/**
 * The tiles above the client list.
 *
 * @param secretsExpiringSoon confidential and machine clients whose secret expires within thirty days
 */
public record ClientStats(long total, long enabled, long disabled, long machine, long confidential, long publicClients,
                          long secretsExpiringSoon) {
}
