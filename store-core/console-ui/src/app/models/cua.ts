/**
 * The shopper-facing auth pod's own contracts. Distinct from `@models/auth`, which is the *staff*
 * side served by `uaa` — two authorization servers, two enums named `SocialProvider`, different
 * members in each.
 */

/**
 * One provider's sign-in credentials for this store, **in cleartext**.
 *
 * `SocialLoginConfigMapper.toDTO` decrypts `appId` and `appSecret` before serialising, so a `GET`
 * hands the browser the live OAuth app secret. The console shows it behind a reveal toggle rather
 * than hiding what the endpoint already sent. See lessons.md, "Store management — payment and
 * social-login reads return secrets in cleartext".
 *
 * Both fields read `null` when the stored value predates encryption, because the mapper only sets
 * them when the stored form is encrypted — which is indistinguishable from "not configured".
 */
export interface ReadableSocialLoginConfig {
  readonly providerId: string;
  readonly appId: string | null;
  readonly appSecret: string | null;
  readonly enabled: boolean;
}

/**
 * What `POST /private/social-login-config` takes — as an **array**, one entry per provider.
 *
 * seller-core's signature said a single object, which is wrong: the controller binds
 * `List<PersistableSocialLoginConfig>` and `saveConfigs` iterates it.
 *
 * Every field is required in practice even though none is annotated. `saveConfigs` calls
 * `repository.save(mapper.toEntity(dto))` on an entity built from scratch, and `APP_ID` and
 * `APP_SECRET` are `nullable = false` — so a config sent without them is a constraint violation the
 * server answers as a 500, not a 400.
 */
export interface PersistableSocialLoginConfig {
  readonly providerId: string;
  readonly appId: string;
  readonly appSecret: string;
  readonly enabled: boolean;
}
