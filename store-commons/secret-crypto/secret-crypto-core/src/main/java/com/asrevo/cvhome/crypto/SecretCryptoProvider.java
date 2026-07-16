package com.asrevo.cvhome.crypto;

public interface SecretCryptoProvider {

    EncryptedValue encrypt(byte[] plaintext);

    byte[] decrypt(EncryptedValue encryptedValue);
}
