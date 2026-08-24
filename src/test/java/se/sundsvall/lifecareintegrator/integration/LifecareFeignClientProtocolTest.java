package se.sundsvall.lifecareintegrator.integration;

import feign.Client;
import java.util.stream.Stream;
import okhttp3.Protocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.integration.lifecareec.configuration.LifecareEcConfiguration;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.configuration.LifecareFcConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the HTTP/1.1 pinning end to end: the {@code okHttpClient} bean each Lifecare configuration declares must be
 * the one Feign actually resolves, overriding the identically named bean that dept44's {@code FeignConfiguration}
 * contributes to the same child context. A silent loss of that override would put HTTP/2 back on the wire and bring
 * back the {@code HTTP_1_1_REQUIRED} stream resets — with nothing failing at startup to say so.
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class LifecareFeignClientProtocolTest {

	@Autowired
	private FeignClientFactory feignClientFactory;

	private static Stream<String> lifecareClientIds() {
		return Stream.of(LifecareEcConfiguration.CLIENT_ID, LifecareFcConfiguration.CLIENT_ID);
	}

	@ParameterizedTest
	@MethodSource("lifecareClientIds")
	void lifecareClientsNegotiateHttp11Only(final String clientId) {
		assertThat(okHttpDelegateOf(clientId).protocols()).containsExactly(Protocol.HTTP_1_1);
	}

	@Test
	void otherClientsKeepTheDept44Default() {
		// Party is a dept44 service behind the platform's own gateway — no reason to pin it, so it must still come from
		// dept44's FeignConfiguration with HTTP/2 available.
		assertThat(okHttpDelegateOf("party").protocols()).contains(Protocol.HTTP_2);
	}

	private okhttp3.OkHttpClient okHttpDelegateOf(final String clientId) {
		final var client = feignClientFactory.getInstance(clientId, Client.class);

		assertThat(client).isInstanceOf(feign.okhttp.OkHttpClient.class);

		return (okhttp3.OkHttpClient) ReflectionTestUtils.getField(client, "delegate");
	}
}
