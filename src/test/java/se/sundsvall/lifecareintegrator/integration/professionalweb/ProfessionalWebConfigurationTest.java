package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.net.http.HttpClient;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.security.Truststore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebConfigurationTest {

	@Mock
	private Truststore truststoreMock;

	@Test
	void professionalWebHttpClientNeverFollowsRedirectsAndIsPinnedToHttp11() throws Exception {
		final var sslContext = SSLContext.getDefault();
		when(truststoreMock.getSSLContext()).thenReturn(sslContext);
		final var properties = new ProfessionalWebProperties(null, null, "a", "saml", null, null, null, Duration.ofMinutes(20), 3, 30, null, Duration.ofMinutes(5));

		final var client = new ProfessionalWebConfiguration().professionalWebHttpClient(properties, truststoreMock);

		assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NEVER);
		assertThat(client.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
		assertThat(client.connectTimeout()).contains(Duration.ofSeconds(3));
		assertThat(client.sslContext()).isSameAs(sslContext);
	}
}
