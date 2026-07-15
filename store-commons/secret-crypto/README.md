# Secret Crypto Library

A framework-independent, thread-safe Java library for encrypting and decrypting application secrets. It supports pluggable providers, including local AES-256-GCM and AWS KMS.

## Modules

- `secret-crypto-core`: Contains the core SPI (`SecretCryptoProvider`) and common models (`EncryptedValue`).
- `secret-crypto-local`: Implementation of AES-256-GCM with support for various key management strategies (Static, Environment Variables, File System).
- `secret-crypto-aws`: Implementation using AWS KMS.
- `secret-crypto-caffeine`: Decrypt cache layer using Caffeine.
- `secret-crypto-autoconfigure`: Spring Boot auto-configuration for easy integration.

## Standalone Usage

### Local AES Provider

```java
import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.local.LocalAesCryptoProvider;
import com.asrevo.cvhome.crypto.local.StaticKeyProvider;
import java.util.Map;
import java.util.Base64;

// 1. Define your keys
byte[] key = Base64.getDecoder().decode("your-base64-encoded-32-byte-key");
StaticKeyProvider keyProvider = new StaticKeyProvider(Map.of("key-1", key));

// 2. Initialize the provider
LocalAesCryptoProvider crypto = new LocalAesCryptoProvider("key-1", keyProvider);

// 3. Encrypt & Decrypt
EncryptedValue encrypted = crypto.encrypt("my-secret".getBytes());
byte[] decrypted = crypto.decrypt(encrypted);
```

### AWS KMS Provider

```java
import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.aws.AwsKmsCryptoProvider;
import software.amazon.awssdk.services.kms.KmsClient;

KmsClient kmsClient = KmsClient.create();
AwsKmsCryptoProvider crypto = new AwsKmsCryptoProvider(kmsClient, "your-kms-key-id");

EncryptedValue encrypted = crypto.encrypt("my-secret".getBytes());
byte[] decrypted = crypto.decrypt(encrypted);
```

### Caching Decrypt Results

Wrap any provider with `CachingSecretCryptoProvider` to cache decryption results.

```java
import com.asrevo.cvhome.crypto.caffeine.CachingSecretCryptoProvider;
import java.time.Duration;

SecretCryptoProvider cachedProvider = new CachingSecretCryptoProvider(actualProvider, Duration.ofMinutes(10));
```

## Spring Boot Integration

Add the `secret-crypto-autoconfigure` dependency to your project. The `SecretCryptoProvider` bean will be automatically configured based on your `application.yml`.

### Configuration Examples

#### 1. Local AES with Static Keys
```yaml
com:
    asrevo:
      cvhome:
          crypto:
            type: LOCAL
            local:
              active-key-id: key-1
              key-provider-type: STATIC
              keys:
                key-1: "base64-key-here..."
                key-2: "another-base64-key..."
```

#### 2. Local AES with Environment Variables
Looks for variables with prefix `CRYPTO_KEY_`. For `key-1`, it looks for `CRYPTO_KEY_KEY_1`.
```yaml
com:
  asrevo:
    cvhome:
      crypto:
        type: LOCAL
        local:
          active-key-id: key-1
          key-provider-type: ENV
```

#### 3. AWS KMS with Caching Enabled
```yaml
com:
  asrevo:
    cvhome:
      crypto:
        type: AWS
        aws:
          key-id: "your-kms-key-id-or-arn"
          region: "us-east-1"
        cache:
          enabled: true
          duration: 10m
```

## Local Key Providers

The `secret-crypto-local` module provides several `LocalKeyProvider` implementations:

- `StaticKeyProvider`: In-memory map of keys.
- `EnvironmentVariableKeyProvider`: Resolves keys from environment variables.
- `FileSystemKeyProvider`: Reads keys from files in a directory (filename = keyId).
- `CustomCallbackKeyProvider`: Delegates key resolution to a functional callback.
- `CompositeKeyProvider`: Chains multiple providers together.