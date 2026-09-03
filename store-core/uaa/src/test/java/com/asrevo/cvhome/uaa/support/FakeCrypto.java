package com.asrevo.cvhome.uaa.support;

import java.util.Arrays;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;

/** XOR "encryption" with a switchable key, so a test can make a stored envelope unreadable by changing the key. */
public final class FakeCrypto implements SecretCryptoProvider {

    public static final String ID = "FAKE";

    private byte key;

    public FakeCrypto(byte key) {
        this.key = key;
    }

    public void rekey(byte newKey) {
        this.key = newKey;
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        return EncryptedValue.builder().version(1).keyId(String.valueOf(key)).algorithm(ID).iv(new byte[0])
                .ciphertext(xor(plaintext)).build();
    }

    @Override
    public byte[] decrypt(EncryptedValue encryptedValue) {
        // The envelope names the key it was made with; a re-keyed provider refuses it, as a real one would.
        if (!String.valueOf(key).equals(encryptedValue.getKeyId())) {
            throw new IllegalStateException("cannot decrypt");
        }
        return xor(encryptedValue.getCiphertext());
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
