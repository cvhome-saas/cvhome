package com.asrevo.cvhome.crypto.aws;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

public class AwsKmsCryptoProvider implements SecretCryptoProvider {

    public static final String PROVIDER_ID = "AWS-KMS";

    private final KmsClient kmsClient;
    private final String keyId;

    public AwsKmsCryptoProvider(KmsClient kmsClient, String keyId) {
        this.kmsClient = kmsClient;
        this.keyId = keyId;
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        EncryptRequest encryptRequest = EncryptRequest.builder()
                .keyId(keyId)
                .plaintext(SdkBytes.fromByteArray(plaintext))
                .build();

        EncryptResponse encryptResponse = kmsClient.encrypt(encryptRequest);

        return EncryptedValue.builder()
                .version(1)
                .keyId(keyId)
                .algorithm(PROVIDER_ID)
                .ciphertext(encryptResponse.ciphertextBlob().asByteArray())
                .build();
    }

    @Override
    public byte[] decrypt(EncryptedValue encryptedValue) {
        DecryptRequest decryptRequest = DecryptRequest.builder()
                .keyId(encryptedValue.getKeyId())
                .ciphertextBlob(SdkBytes.fromByteArray(encryptedValue.getCiphertext()))
                .build();

        DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);

        return decryptResponse.plaintext().asByteArray();
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }
}
