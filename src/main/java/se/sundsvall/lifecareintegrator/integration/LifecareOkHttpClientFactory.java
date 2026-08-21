package se.sundsvall.lifecareintegrator.integration;

import feign.Client;
import feign.okhttp.OkHttpClient;
import java.util.List;
import javax.net.ssl.X509TrustManager;
import okhttp3.Protocol;
import se.sundsvall.dept44.security.Truststore;

/**
 * Builds the Feign HTTP client for the Lifecare integrations: the dept44 default OkHttp client, with the protocol list
 * pinned to HTTP/1.1.
 *
 * <p>
 * Lifecare is served by IIS, whose TLS ALPN advertises {@code h2}, so OkHttp negotiates HTTP/2 by default. IIS then
 * resets any stream it cannot serve over HTTP/2 — connection-bound features such as Windows authentication and
 * client-certificate renegotiation have no HTTP/2 equivalent — with the {@code HTTP_1_1_REQUIRED} error code, which is
 * the server asking the client to redo the request over HTTP/1.1. OkHttp does not downgrade on its own; it surfaces
 * {@code StreamResetException: stream was reset: HTTP_1_1_REQUIRED}, which Feign wraps in a {@code RetryableException}
 * and the call fails. Negotiating HTTP/1.1 up front avoids the reset entirely.
 */
public final class LifecareOkHttpClientFactory {

	private LifecareOkHttpClientFactory() {}

	/**
	 * The dept44 truststore-backed OkHttp client, restricted to HTTP/1.1.
	 *
	 * @param  truststore the dept44 truststore
	 * @return            a Feign client that never negotiates HTTP/2
	 */
	public static Client http11Client(final Truststore truststore) {
		return new OkHttpClient(http11OkHttp(truststore));
	}

	/** The underlying OkHttp client — the dept44 truststore configuration plus the HTTP/1.1 protocol pinning. */
	static okhttp3.OkHttpClient http11OkHttp(final Truststore truststore) {
		final var trustManager = (X509TrustManager) truststore.getTrustManagerFactory().getTrustManagers()[0];

		return new okhttp3.OkHttpClient.Builder()
			.sslSocketFactory(truststore.getSSLContext().getSocketFactory(), trustManager)
			.protocols(List.of(Protocol.HTTP_1_1))
			.build();
	}
}
