package org.revo.streamer.codec.rtsp.action;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspVersions;
import org.revo.streamer.codec.commons.utils.MessageUtils;
import org.revo.streamer.codec.rtsp.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OptionsAction extends BaseAction<DefaultFullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(OptionsAction.class);

    public OptionsAction(DefaultFullHttpRequest req, RtspSession rtspSession) {
        super(req, rtspSession);
    }

    @Override
    public DefaultFullHttpResponse call() {
        DefaultFullHttpResponse rep = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
        MessageUtils.get(req, RtspHeaderNames.CSEQ).ifPresent(it -> MessageUtils.append(rep, it));
        MessageUtils.get(req, RtspHeaderNames.SESSION).ifPresent(it -> MessageUtils.append(rep, it));
        MessageUtils.set(RtspHeaderNames.PUBLIC, "SETUP, ANNOUNCE, TEARDOWN, RECORD").ifPresent(it -> MessageUtils.append(rep, it));
        MessageUtils.set(RtspHeaderNames.SERVER, "aslive").ifPresent(it -> MessageUtils.append(rep, it));
        MessageUtils.set(RtspHeaderNames.CONTENT_LENGTH, "0").ifPresent(it -> MessageUtils.append(rep, it));
        return rep;
    }

}
