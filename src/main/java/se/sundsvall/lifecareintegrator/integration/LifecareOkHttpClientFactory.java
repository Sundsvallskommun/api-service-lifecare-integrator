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
 * The pin is required, not a preference: Lifecare's IIS advertises {@code h2} over ALPN but resets any stream it
 * cannot serve over HTTP/2 with {@code HTTP_1_1_REQUIRED}, and OkHttp does not downgrade on its own.
 */
public final class LifecareOkHttpClientFactory {

	private LifecareOkHttpClientFactory() {}

	/**
	 * The dept44 truststore-backed OkHttp client, restricted to HTTP/1.1.
	 *
	 * @param  truststore the dept44 truststore
	 * @return            a Feign client that never negotiates HTTP/2
	 */
	public static Client createHttp11Client(final Truststore truststore) {
		return new OkHttpClient(createHttp11OkHttpClient(truststore));
	}

	/** The underlying OkHttp client — the dept44 truststore configuration plus the HTTP/1.1 protocol pinning. */
	public static okhttp3.OkHttpClient createHttp11OkHttpClient(final Truststore truststore) {
		final var trustManager = (X509TrustManager) truststore.getTrustManagerFactory().getTrustManagers()[0];

		return new okhttp3.OkHttpClient.Builder()
			.sslSocketFactory(truststore.getSSLContext().getSocketFactory(), trustManager)
			.protocols(List.of(Protocol.HTTP_1_1))
			.build();
	}
}
