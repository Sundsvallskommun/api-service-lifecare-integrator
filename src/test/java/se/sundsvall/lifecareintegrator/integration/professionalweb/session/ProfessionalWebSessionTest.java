package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebProperties;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.http;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.properties;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.stubSignIn;

class ProfessionalWebSessionTest {

	private WireMockServer wireMock;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		stubSignIn(wireMock);
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void unconfigured() {
		final var properties = new ProfessionalWebProperties(null, null, "a", "saml", null, null, null, Duration.ofMinutes(1), 1, 1, null);

		assertThatThrownBy(() -> session(properties, Clock.systemUTC()).prepare()).hasMessageContaining("not configured");
	}

	@Test
	void sessionPastItsTtlIsReplaced() {
		final var clock = new MutableClock(Instant.parse("2026-09-25T08:00:00Z"));
		final var timed = session(properties(wireMock, "user", "pw", null), clock);

		timed.prepare();
		clock.now = clock.now.plus(Duration.ofMinutes(20));
		timed.prepare();

		wireMock.verify(2, postRequestedFor(urlPathEqualTo("/idp/login/post")));
	}

	@Test
	void seededSessionIsUsedWithoutSigningIn() {
		final var headers = seededSession("ASP.NET_SessionId=s1; LEGACY-TOKEN=seeded", Clock.systemUTC()).prepare();

		assertThat(headers)
			.containsEntry("X-LEGACY-TOKEN", "seeded")
			.hasEntrySatisfying("Cookie", cookie -> assertThat(cookie).contains("ASP.NET_SessionId=s1"));
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WE.Flow.Html")));
	}

	@Test
	void seededSessionWithoutLegacyToken() {
		final var seeded = seededSession("ASP.NET_SessionId=s1", Clock.systemUTC());

		assertThatThrownBy(seeded::prepare).hasMessageContaining("not a signed-in session");
	}

	@Test
	void keepAliveOnlyWithASession() {
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));
		final var seeded = seededSession("LEGACY-TOKEN=seeded", Clock.systemUTC());

		seeded.keepAlive();
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));

		seeded.prepare();
		seeded.keepAlive();
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat"))
			.withHeader("Cookie", containing("LEGACY-TOKEN=seeded")));
	}

	@Test
	void keepAliveResetsAnExpiredSessionInsteadOfPingingIt() {
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));
		final var clock = new MutableClock(Instant.parse("2026-09-25T08:00:00Z"));
		final var seeded = seededSession("LEGACY-TOKEN=seeded", clock);

		seeded.prepare();
		clock.now = clock.now.plus(Duration.ofMinutes(20));

		seeded.keepAlive();

		// A session already past its TTL is thrown away rather than pinged with cookies this service itself would
		// refuse for a real call.
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));
		assertThat(seeded.isEstablished()).isFalse();
	}

	@Test
	void keepAliveFailureIsSwallowed() {
		final var properties = new ProfessionalWebProperties("http://localhost:1", "d", "a", "saml", null, null, null, Duration.ofMinutes(1), 1, 1,
			"LEGACY-TOKEN=x");
		final var seeded = session(properties, Clock.systemUTC());
		seeded.prepare();

		assertThatNoException().isThrownBy(seeded::keepAlive);
	}

	private ProfessionalWebSession seededSession(final String sessionCookie, final Clock clock) {
		return session(properties(wireMock, null, null, sessionCookie), clock);
	}

	private static ProfessionalWebSession session(final ProfessionalWebProperties properties, final Clock clock) {
		final var http = http();
		return new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, clock);
	}

	private static final class MutableClock extends Clock {

		private Instant now;

		private MutableClock(final Instant now) {
			this.now = now;
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(final ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
