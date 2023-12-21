package org.revo.streamer.codec.commons.container;

import java.io.Closeable;

public interface Muxer extends Closeable {

    void mux(long timeStamp, byte[] payload);

}
