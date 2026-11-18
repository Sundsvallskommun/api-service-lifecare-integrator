package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalWebCookiesTest {

	@Test
	void absorbSetsAndOverwrites() {
		final var cookies = new ProfessionalWebCookies(Clock.systemUTC());

		cookies.absorbSetCookie(List.of("ASP.NET_SessionId=abc; path=/; HttpOnly", "LEGACY-TOKEN=a=b=c; secure"));
		cookies.absorbSetCookie(List.of("ASP.NET_SessionId=def; path=/"));

		assertThat(cookies.get("ASP.NET_SessionId")).contains("def");
		assertThat(cookies.get("LEGACY-TOKEN")).contains("a=b=c");
		assertThat(cookies.toHeader()).isEqualTo("ASP.NET_SessionId=def; LEGACY-TOKEN=a=b=c");
		assertThat(cookies.names()).containsExactly("ASP.NET_SessionId", "LEGACY-TOKEN");
	}

	@Test
	void absorbHonoursDeletions() {
		final var cookies = new ProfessionalWebCookies(Clock.systemUTC());
		cookies.set("one", "1");
		cookies.set("two", "2");
		cookies.set("three", "3");

		cookies.absorbSetCookie(List.of(
			"one=; Max-Age=0",
			"two=; expires=Thu, 01-Jan-1970 00:00:00 GMT; path=/",
			"three=kept; Max-Age=3600"));

		assertThat(cookies.has("one")).isFalse();
		assertThat(cookies.has("two")).isFalse();
		assertThat(cookies.get("three")).contains("kept");
	}

	@Test
	void absorbIgnoresMalformedHeadersAndAttributes() {
		final var cookies = new ProfessionalWebCookies(Clock.systemUTC());

		cookies.absorbSetCookie(List.of("no-separator", "=novalue", "name=value; Max-Age=soon; expires=not a date; flag"));

		assertThat(cookies.names()).containsExactly("name");
	}

	@Test
	void clear() {
		final var cookies = new ProfessionalWebCookies(Clock.systemUTC());
		cookies.set("name", "value");

		cookies.clear();

		assertThat(cookies.toHeader()).isEmpty();
		assertThat(cookies.get("name")).isEmpty();
	}

	@Test
	void absorbHonoursAnExpiresDateNotYetPastTheInjectedClock() {
		final var clock = Clock.fixed(Instant.parse("2020-06-15T00:00:00Z"), ZoneOffset.UTC);
		final var cookies = new ProfessionalWebCookies(clock);
		cookies.set("still-valid", "v");

		cookies.absorbSetCookie(List.of("still-valid=v; expires=Wed, 01-Jul-2020 00:00:00 GMT"));

		assertThat(cookies.has("still-valid")).isTrue();
	}

	@Test
	void absorbHonoursAnExpiresDateAlreadyPastTheInjectedClock() {
		final var clock = Clock.fixed(Instant.parse("2020-08-01T00:00:00Z"), ZoneOffset.UTC);
		final var cookies = new ProfessionalWebCookies(clock);
		cookies.set("expired", "v");

		cookies.absorbSetCookie(List.of("expired=v; expires=Wed, 01-Jul-2020 00:00:00 GMT"));

		assertThat(cookies.has("expired")).isFalse();
	}

	@Test
	void absorbCookieHeader() {
		final var cookies = new ProfessionalWebCookies(Clock.systemUTC());

		cookies.absorbCookieHeader("ASP.NET_SessionId=s; LEGACY-TOKEN=a=b ;broken; IDP=x");

		assertThat(cookies.names()).containsExactly("ASP.NET_SessionId", "LEGACY-TOKEN", "IDP");
		assertThat(cookies.get("LEGACY-TOKEN")).contains("a=b");
	}
}
