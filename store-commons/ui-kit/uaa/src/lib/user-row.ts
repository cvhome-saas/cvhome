/**
 * A uaa account as a table renders it, and the mapper from the DTO.
 *
 * Extracted from console-ui's `models/platform.ts` when the user admin table moved here. It sat
 * beside the organization and pod row models, which are the platform console's own; this one is not
 * — it is `UserDto` reshaped, so it belongs with the client that fetches it and with the table that
 * draws it, where a second console administering the same uaa can reach all three.
 */
import {USER_METADATA_ORG, USER_METADATA_STORE, metadataString, type UserDto, type UserStatus} from './uaa.models';

/**
 * One account, platform-wide.
 *
 * `org` and `store` are read out of uaa's `metadata` bag, which is `Map<String, Object>` and holds
 * whatever a caller put there — so both are null unless the value really was a string.
 */
export interface PlatformUserRow {
  readonly id: string;
  readonly username: string;
  readonly email: string;
  /** Given and family name joined, or `''` — uaa allows both to be null. */
  readonly name: string;
  readonly roles: readonly string[];
  readonly enabled: boolean;
  /** Derived by uaa; `enabled` is only one of its inputs. */
  readonly status: UserStatus;
  readonly lastSignInAt: string | null;
  readonly org: string | null;
  readonly store: string | null;
  /** A monogram for the row's avatar, from the name where there is one and the username where not. */
  readonly initials: string;
}

/**
 * One uaa account, as the platform's tables render it.
 *
 * `org` and `store` come out of the metadata bag through {@link metadataString}, so a value that is
 * not a string reads as absent rather than as `[object Object]`.
 */
export function toPlatformUserRow(user: UserDto): PlatformUserRow {
  const name = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
  const username = user.username ?? '';
  return {
    id: user.id,
    username,
    email: user.email ?? '',
    name,
    roles: user.roles ?? [],
    enabled: user.enabled,
    status: user.status ?? (user.enabled ? 'ACTIVE' : 'DISABLED'),
    lastSignInAt: user.lastSignInAt ?? null,
    org: metadataString(user.metadata, USER_METADATA_ORG),
    store: metadataString(user.metadata, USER_METADATA_STORE),
    initials: initialsOf(name || username),
  };
}

/**
 * The avatar monogram.
 *
 * Derived from whatever identifies the row — the name where there is one, the username where there
 * is not. That is not the same as inventing a name: an avatar is a visual anchor in a list.
 */
function initialsOf(source: string): string {
  const words = source.split(/[\s@._-]+/).filter(Boolean);
  if (!words.length) {
    return '?';
  }
  return words
    .slice(0, 2)
    .map((word) => word.charAt(0).toUpperCase())
    .join('');
}
