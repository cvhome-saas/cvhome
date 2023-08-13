package com.asrevo.cvhome.gateway.config.ssl;


import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import reactor.netty.tcp.AbstractProtocolSslContextSpec;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;

import static io.netty.handler.ssl.SslProvider.JDK;
import static io.netty.handler.ssl.SslProvider.OPENSSL;


public final class AcmeTls1SslContextSpec extends AbstractProtocolSslContextSpec<AcmeTls1SslContextSpec> {

	static final Consumer<SslContextBuilder> DEFAULT_CONFIGURATOR =
			sslCtxBuilder -> sslCtxBuilder.sslProvider(SslProvider.isAlpnSupported(OPENSSL) ? OPENSSL : JDK)
					.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
					.applicationProtocolConfig(new ApplicationProtocolConfig(
							ApplicationProtocolConfig.Protocol.ALPN,
							ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
							ApplicationProtocolConfig.SelectedListenerFailureBehavior.CHOOSE_MY_LAST_PROTOCOL,
							TlsAlpn01Challenge.ACME_TLS_1_PROTOCOL));

	AcmeTls1SslContextSpec(SslContextBuilder sslContextBuilder) {
		super(sslContextBuilder);
	}

	public static AcmeTls1SslContextSpec forServer(PrivateKey key, X509Certificate... keyCertChain) {
		return new AcmeTls1SslContextSpec(SslContextBuilder.forServer(key, keyCertChain));
	}

	@Override
	public AcmeTls1SslContextSpec get() {
		return this;
	}

	@Override
	protected Consumer<SslContextBuilder> defaultConfiguration() {
		return DEFAULT_CONFIGURATOR;
	}
}
