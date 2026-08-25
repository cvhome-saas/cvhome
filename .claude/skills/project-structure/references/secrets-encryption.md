# Secrets: encryption at rest

Tenant-supplied credentials — payment gateway API keys, social-login app secrets, webhook signing secrets —
are **never stored as plaintext**. They are encrypted in the mapper layer on the way into the entity, so what
lands in Postgres is opaque: reading the table directly (a DBA, a leaked backup, a replica) yields nothing
usable.

Library: `store-commons/secret-crypto/*`. Services get it by depending on
`:store-commons:secret-crypto:secret-crypto-autoconfigure`, which auto-configures a single
`SecretCryptoProvider` bean.

## The contract

```java
public interface SecretCryptoProvider {
    EncryptedValue encrypt(byte[] plaintext);
    byte[] decrypt(EncryptedValue encryptedValue);
    String providerId();   // matched against EncryptedValue.algorithm to route decryption
}
```

`EncryptedValue` is a self-describing envelope that serializes to a single string column:

```
ENC:<version>:<keyId>:<algorithm>:<base64 iv>:<base64 ciphertext>
```

```java
public static boolean isEncrypted(String value) { return value != null && value.startsWith("ENC:"); }
```

Two consequences worth internalizing:

- **`isEncrypted()` is a cheap, reliable guard.** Mappers use it to stay idempotent — encrypting an
  already-encrypted value would double-wrap it.
- **The envelope carries `keyId` and `algorithm`**, so a value can always be decrypted by whichever provider and
  key produced it, even after the active provider or key has changed. Key rotation and provider migration don't
  invalidate existing rows.

## The mapper pattern

Encryption happens in the **mapper between DTO and entity** — not in the controller, not in the service. The
entity therefore only ever holds ciphertext.

`store-pod/payment/payment-core/.../mapper/PaymentConfigurationMapper.java`:

```java
@Component
@RequiredArgsConstructor
public class PaymentConfigurationMapper {

    private final SecretCryptoProvider cryptoProvider;

    public PaymentConfiguration toEntity(PersistablePaymentConfiguration dto) {
        ...
        if (dto.getApiKey() != null && !EncryptedValue.isEncrypted(dto.getApiKey())) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getApiKey().getBytes(StandardCharsets.UTF_8));
            entity.setApiKey(encrypted.serialize());
        }
        // same for secretKey, webhookSecret
    }

    public ReadablePaymentConfiguration toDTO(PaymentConfiguration entity) {
        ...
        dto.setApiKey(decrypt(entity.getApiKey()));
        dto.setSecretKey(decrypt(entity.getSecretKey()));
        dto.setWebhookSecret(decrypt(entity.getWebhookSecret()));
    }

    private String decrypt(String value) {
        if (EncryptedValue.isEncrypted(value)) {
            try {
                return new String(cryptoProvider.decrypt(EncryptedValue.deserialize(value)), StandardCharsets.UTF_8);
            } catch (Exception _) {
                log.error("Failed to decrypt value: {}", value);
            }
        }
        return null;     // fail closed — never return raw ciphertext to a caller
    }
}
```

`store-pod/cua/.../web/mapper/SocialLoginConfigMapper.java` is the same pattern for `appId` and `appSecret` of
per-store social login providers (Google, Facebook, …), which are tenant-supplied and equally sensitive.

Points of the pattern to preserve when you add a new secret field:

1. **Encrypt in `toEntity`, decrypt in `toDTO`.** The entity/DB side is always ciphertext.
2. **Guard with `!EncryptedValue.isEncrypted(...)`** before encrypting, so re-saving an unchanged DTO doesn't
   double-encrypt.
3. **Decryption failure returns `null` and logs**, rather than throwing or leaking the stored string. A rotated
   or unavailable key degrades to "no value", not to a 500 or an exposed blob.
4. Note the asymmetry in `PaymentConfigurationMapper`: `toEntity` skips already-encrypted input, while
   `updateEntity` re-encrypts unconditionally — on update the incoming DTO is assumed to be fresh plaintext
   from the user.

## Providers and how one is chosen

`SecretCryptoAutoConfiguration` builds the bean:

```java
List<SecretCryptoProvider> providers = new ArrayList<>(customProviders);
tryCreateLocal(properties.getLocal()).ifPresent(providers::add);
tryCreateAws(properties.getAws()).ifPresent(providers::add);

SecretCryptoProvider activeProvider = providers.stream()
        .filter(p -> p.providerId().equals(resolveActiveProviderId(properties.getType())))
        .findFirst()
        .orElseThrow(...);

return new CachingSecretCryptoProvider(new SecretCryptoProviderRegistry(providers, activeProvider),
                                       properties.getCache().getDuration());
```

Three layers, composed:

| Layer | Module | Role |
|---|---|---|
| `CachingSecretCryptoProvider` | `secret-crypto-caffeine` | Caffeine cache with a TTL, so hot secrets aren't decrypted (or KMS-called) on every request |
| `SecretCryptoProviderRegistry` | `secret-crypto-core` | **Encrypt always via the active provider; decrypt via whichever provider's `providerId()` matches the value's `algorithm`** |
| `LocalAesCryptoProvider` / `AwsKmsCryptoProvider` | `secret-crypto-local` / `secret-crypto-aws` | The actual crypto |

The registry is the piece that makes migration safe:

> *"switching the active provider never breaks decrypting values encrypted under a previously-active provider"*

Flip `type` from `LOCAL` to `AWS` and existing rows still decrypt through the local provider, while new writes
go to KMS.

### Configuration

```yaml
com.asrevo.cvhome.crypto:
  type: LOCAL          # LOCAL | AWS | a custom provider's providerId()
  local:
    key-provider-type: STATIC | ENV | FILE | RANDOM
    key: <hex AES key>          # STATIC only
  aws:
    region: ...
    key-id: ...
  cache:
    duration: ...
```

`common-config.yml` sets `crypto.type: LOCAL` for local development; AWS deployments use KMS.

Local key providers (`secret-crypto-local`): `StaticKeyProvider` (hex key in config),
`EnvironmentVariableKeyProvider`, `FileSystemKeyProvider` (default `~/.cvhome/secret-crypto/keys`),
`RandomKeyProvider`, `CustomCallbackKeyProvider`. When `key-provider-type` is unset the resolution order is
**ENV → FILE → RANDOM (with a warning)**.

> ⚠️ `RandomKeyProvider` generates a fresh key per process — anything encrypted with it is unreadable after a
> restart. It exists as a last-resort fallback for throwaway environments; if you see that warning in a
> long-lived environment, the key configuration is wrong.

Provider construction is best-effort (`tryCreateLocal` / `tryCreateAws` log and skip on failure), so a service
without AWS credentials still starts with the local provider available — but if the **active** provider can't be
constructed, startup fails loudly rather than silently storing plaintext.

## Adding a new encrypted field

1. Store it as a `String` column sized for the `ENC:` envelope (base64 ciphertext is larger than the plaintext).
2. Add the encrypt branch to the mapper's `toEntity` (with the `isEncrypted` guard) and the decrypt call to
   `toDTO`.
3. Ensure the module depends on `:store-commons:secret-crypto:secret-crypto-autoconfigure` and inject
   `SecretCryptoProvider`.
4. Never log the plaintext, never expose the raw column through an API, and never add the field to a
   `toString()`.

## Related

- Module inventory for `secret-crypto/*` — `shared-libraries.md`
- Where `crypto.type` is configured per environment — `configuration.md`
- The `Readable*`/`Persistable*` DTO convention these mappers sit between — `store-pod.md`
