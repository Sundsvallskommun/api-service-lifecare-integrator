package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import okhttp3.Protocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebConfigurationTest {

	@Mock
	private Truststore truststoreMock;

	@Test
	void professionalWebHttpClientNeverFollowsRedirectsAndIsPinnedToHttp11() throws Exception {
		final var sslContext = SSLContext.getDefault();
		final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init((KeyStore) null);
		when(truststoreMock.getSSLContext()).thenReturn(sslContext);
		when(truststoreMock.getTrustManagerFactory()).thenReturn(trustManagerFactory);
		final var properties = new ProfessionalWebProperties(null, null, "a", "saml", null, null, null, Duration.ofMinutes(20), 3, 30, null, Duration.ofMinutes(5));

		final var client = new ProfessionalWebConfiguration().professionalWebHttpClient(properties, truststoreMock);

		assertThat(client.followRedirects()).isFalse();
		assertThat(client.followSslRedirects()).isFalse();
		assertThat(client.protocols()).containsExactly(Protocol.HTTP_1_1);
		assertThat(client.connectTimeoutMillis()).isEqualTo(3000);
		assertThat(client.readTimeoutMillis()).isEqualTo(30000);
	}
}
