package se.sundsvall.lifecareintegrator.integration.elderlycare.configuration;

import feign.Client;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.net.URLDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.lifecareintegrator.integration.LifecareOkHttpClientFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.lifecareintegrator.integration.elderlycare.configuration.ElderlyCareConfiguration.CLIENT_ID;
import static se.sundsvall.lifecareintegrator.integration.elderlycare.configuration.ElderlyCareConfiguration.toQueryValue;

@ExtendWith(MockitoExtension.class)
class ElderlyCareConfigurationTest {

	@Spy
	private FeignMultiCustomizer feignMultiCustomizerSpy;

	@Mock
	private FeignBuilderCustomizer feignBuilderCustomizerMock;

	@Mock
	private Truststore truststoreMock;

	@Mock
	private Client clientMock;

	@Mock
	private ElderlyCareProperties propertiesMock;

	@Test
	void testFeignBuilderCustomizer() {
		final var configuration = new ElderlyCareConfiguration();

		when(propertiesMock.domain()).thenReturn("the-domain");
		when(propertiesMock.key()).thenReturn("the-key");
		when(propertiesMock.connectTimeout()).thenReturn(1);
		when(propertiesMock.readTimeout()).thenReturn(2);
		when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);

		try (final MockedStatic<FeignMultiCustomizer> feignMultiCustomizerMock = Mockito.mockStatic(FeignMultiCustomizer.class)) {
			feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

			final var customizer = configuration.feignBuilderCustomizer(propertiesMock);

			final ArgumentCaptor<ProblemErrorDecoder> errorDecoderCaptor = ArgumentCaptor.forClass(ProblemErrorDecoder.class);
			final ArgumentCaptor<RequestInterceptor> interceptorCaptor = ArgumentCaptor.forClass(RequestInterceptor.class);

			verify(feignMultiCustomizerSpy).withErrorDecoder(errorDecoderCaptor.capture());
			verify(feignMultiCustomizerSpy).withRequestInterceptor(interceptorCaptor.capture());
			verify(feignMultiCustomizerSpy).withRequestTimeoutsInSeconds(1, 2);
			verify(feignMultiCustomizerSpy).composeCustomizersToOne();

			assertThat(errorDecoderCaptor.getValue()).hasFieldOrPropertyWithValue("integrationName", CLIENT_ID);
			assertThat(customizer).isSameAs(feignBuilderCustomizerMock);

			final var template = new RequestTemplate();
			interceptorCaptor.getValue().apply(template);

			assertThat(template.queries().get("domain")).containsExactly("the-domain");
			assertThat(template.queries().get("key")).containsExactly("the-key");
			// The key travels in the query string only: a second carrier makes EC reject the request as ambiguous.
			assertThat(template.headers()).doesNotContainKey("X-API-Key");
		}
	}

	@Test
	void toQueryValueEncodesTheKey() {
		assertThat(toQueryValue("B3zXYTywcGodU+eJwc7ZJut8IcVXV/abc=")).isEqualTo("B3zXYTywcGodU%2BeJwc7ZJut8IcVXV%2Fabc%3D");
	}

	@Test
	void toQueryValueRoundTripsToTheKey() {
		final var key = "B3zXYTywcGodU+eJwc7ZJut8IcVXV/abc=";

		assertThat(URLDecoder.decode(toQueryValue(key), UTF_8)).isEqualTo(key);
	}

	@Test
	void toQueryValueEncodesASpaceWithoutAPlus() {
		assertThat(toQueryValue("a b")).isEqualTo("a%20b");
	}

	@Test
	void testOkHttpClientIsPinnedToHttp11() {
		final var configuration = new ElderlyCareConfiguration();

		try (final MockedStatic<LifecareOkHttpClientFactory> factoryMock = Mockito.mockStatic(LifecareOkHttpClientFactory.class)) {
			factoryMock.when(() -> LifecareOkHttpClientFactory.http11Client(truststoreMock)).thenReturn(clientMock);

			assertThat(configuration.okHttpClient(truststoreMock)).isSameAs(clientMock);
		}
	}
}
