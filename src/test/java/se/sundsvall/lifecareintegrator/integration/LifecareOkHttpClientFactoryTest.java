package se.sundsvall.lifecareintegrator.integration;

import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import okhttp3.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.security.Truststore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory.http11Client;
import static se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory.http11OkHttp;

@ExtendWith(MockitoExtension.class)
class LifecareOkHttpClientFactoryTest {

	@Mock
	private Truststore truststoreMock;

	@Mock
	private SSLContext sslContextMock;

	@Mock
	private SSLSocketFactory sslSocketFactoryMock;

	@BeforeEach
	void setUp() throws Exception {
		final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init((KeyStore) null);

		when(truststoreMock.getTrustManagerFactory()).thenReturn(trustManagerFactory);
		when(truststoreMock.getSSLContext()).thenReturn(sslContextMock);
		when(sslContextMock.getSocketFactory()).thenReturn(sslSocketFactoryMock);
	}

	@Test
	void pinsProtocolToHttp11() {
		// The whole point: no h2 in the list, so ALPN never negotiates HTTP/2 and IIS never answers HTTP_1_1_REQUIRED.
		assertThat(http11OkHttp(truststoreMock).protocols()).containsExactly(Protocol.HTTP_1_1);
	}

	@Test
	void usesTheTruststoreSocketFactory() {
		assertThat(http11OkHttp(truststoreMock).sslSocketFactory()).isSameAs(sslSocketFactoryMock);
	}

	@Test
	void wrapsTheOkHttpClientForFeign() {
		assertThat(http11Client(truststoreMock)).isInstanceOf(feign.okhttp.OkHttpClient.class);
	}
}
