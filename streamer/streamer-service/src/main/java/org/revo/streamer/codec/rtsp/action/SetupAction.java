package org.revo.streamer.codec.rtsp.action;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspVersions;
import org.revo.streamer.codec.commons.utils.MessageUtils;
import org.revo.streamer.codec.rtsp.RtspSession;
import org.revo.streamer.codec.rtsp.Transport;

import javax.sip.TransportNotSupportedException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;


public class SetupAction extends BaseAction<DefaultFullHttpRequest> {
    public SetupAction(DefaultFullHttpRequest req, RtspSession rtspSession) {
        super(req, rtspSession);
    }

    @Override
    public DefaultFullHttpResponse call() {
        DefaultFullHttpResponse rep = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
        MessageUtils.get(req, RtspHeaderNames.CSEQ)
                .ifPresent(it ->
                        MessageUtils.append(rep, it));
        Optional.of(MessageUtils.get(req, RtspHeaderNames.SESSION)
                        .orElseGet(() ->
                                new SimpleImmutableEntry<>(RtspHeaderNames.SESSION, rtspSession.getId())))
                .ifPresent(it ->
                        MessageUtils.append(rep, it));
        MessageUtils.get(req, RtspHeaderNames.TRANSPORT)
                .map(it -> new SimpleImmutableEntry<>(it.getKey(), Transport.parse(it.getValue())))
                .ifPresent(it -> {
                    try {
                        Transport transport = rtspSession.setup(req.uri(), it.getValue());
                        rep.headers().add(it.getKey(), transport.toString());
                    } catch (TransportNotSupportedException e) {
                        System.out.println(e.getMessage());
                    }
                });

        return rep;
    }
}
