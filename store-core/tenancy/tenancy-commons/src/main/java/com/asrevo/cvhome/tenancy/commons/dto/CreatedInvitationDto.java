package com.asrevo.cvhome.tenancy.commons.dto;

/**
 * A freshly created invitation, and the only place its token is ever readable.
 *
 * <p>
 * <strong>There is no mail sender in this platform</strong>, so nothing here can email the invitee. The token is
 * returned to the caller so the console can show a link to copy and send by whatever means the operator already
 * uses. Only its hash is stored, so this response cannot be reconstructed afterwards — losing it means issuing a
 * new invitation, which is the same property a password reset link has and for the same reason.
 * </p>
 */
public record CreatedInvitationDto(InvitationDto invitation, String token) {
}
