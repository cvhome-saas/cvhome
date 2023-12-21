package org.revo.streamer.codec.commons.rtp;


import org.revo.streamer.codec.commons.rtp.base.ADTS;
import org.revo.streamer.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.codec.commons.utils.StaticProcs;
import org.revo.streamer.codec.sdp.ElementSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RtpADTSDecoder implements Converter<RtpPkt, List<ADTS>> {
    private final RtpToADTSFrame rtpToAdtsFrame = new RtpToADTSFrame();
    private final ElementSpecific specific;

    public RtpADTSDecoder(ElementSpecific specific) {
        this.specific = specific;
    }

    @Override
    public List<ADTS> convert(RtpPkt rtpPkt) {
        return rtpToAdtsFrame.apply(rtpPkt);
    }

    private class RtpToADTSFrame implements Function<RtpPkt, List<ADTS>> {
        @Override
        public List<ADTS> apply(RtpPkt rtpPkt) {
            List<ADTS> adts = new ArrayList<>();
            int auHeaderLength = StaticProcs.bytesToUIntInt(rtpPkt.getPayload(), 0) >> 3;
            int offset = 2 + auHeaderLength;
            for (int i = 0; i < (auHeaderLength / 2); i++) {
                int size = StaticProcs.bytesToUIntInt(rtpPkt.getPayload(), 2 + (i * 2)) >> 3;
                adts.add(new ADTS(rtpPkt.getPayload(), offset, size, specific));
                offset += size;
            }
            return adts;
        }
    }
}
