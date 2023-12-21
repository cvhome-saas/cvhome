package org.revo.streamer.codec.commons.container.audio;

import lombok.SneakyThrows;
import org.revo.streamer.codec.commons.container.Muxer;
import org.revo.streamer.codec.sdp.SdpElementParser;

import java.io.OutputStream;

public class AacAudioMuxer implements Muxer {
    private final OutputStream outputStream;
    private final SdpElementParser sdpElementParser;

    public AacAudioMuxer(OutputStream outputStream, SdpElementParser sdpElementParser) {
        this.outputStream = outputStream;
        this.sdpElementParser = sdpElementParser;
    }

    @SneakyThrows
    public void mux(long timeStamp, byte[] payload) {
        this.outputStream.write(payload);
    }


    @SneakyThrows
    @Override
    public void close() {
        outputStream.close();
    }
}
