/**
 * The shopper-facing auth pod's own contracts. Distinct from `@models/auth`, which is the *staff*
 * side served by `uaa` — two authorization servers, two enums named `SocialProvider`, different
 * members in each.
 */

/**
 * One provider's sign-in credentials for this store.
 *
 * `appSecret` is always `null`: the read no longer returns it. `appId` still comes back, because an
 * OAuth2 client id travels in the authorization URL and is public by construction, and a seller has
 * to be able to see which application their store is wired to.
 *
 * That is what `hasAppSecret` is for. Without it the console cannot tell "no secret" from "secret
 * withheld", and warns that a fully configured provider is incomplete. An empty secret field means
 * "keep the stored one", never "clear it".
 *
 * `appId` reads `null` when the stored credentials cannot be decrypted — a key that has since
 * changed, say — which the API reports as unconfigured rather than failing the whole screen.
 */
export interface ReadableSocialLoginConfig {
  readonly providerId: string;
  readonly appId: string | null;
  readonly appSecret: string | null;
  readonly hasAppSecret: boolean;
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
