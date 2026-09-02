package com.asrevo.cvhome.uaa.keys;

import java.util.Arrays;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;

/** XOR "encryption" with a switchable key, so a test can make a stored envelope unreadable by changing the key. */
final class FakeCrypto implements SecretCryptoProvider {

    static final String ID = "FAKE";

    private byte key;

    FakeCrypto(byte key) {
        this.key = key;
    }

    void rekey(byte newKey) {
        this.key = newKey;
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        return EncryptedValue.builder().version(1).keyId("k").algorithm(ID).iv(new byte[0]).ciphertext(xor(plaintext)).build();
    }

    @Override
    public byte[] decrypt(EncryptedValue encryptedValue) {
        byte[] plain = xor(encryptedValue.getCiphertext());
        // A wrong key yields bytes that are not JSON; the mapper turns the parse failure into "unusable".
        if (plain.length == 0 || plain[0] != '{') {
            throw new IllegalStateException("cannot decrypt");
        }
        return plain;
    }

    @Override
    public String providerId() {
        return ID;
    }

    private byte[] xor(byte[] in) {
        byte[] out = Arrays.copyOf(in, in.length);
        for (int i = 0; i < out.length; i++) {
            out[i] ^= key;
        }
        return out;
    }

}
