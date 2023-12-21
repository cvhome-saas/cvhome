package org.revo.streamer.codec.commons.rtp.base;

public interface Payload {
    byte[] getPayload();

    int headerSize();

}
