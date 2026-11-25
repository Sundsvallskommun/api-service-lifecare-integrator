package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebProperties;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.http;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.loginPage;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.properties;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.stubSignIn;

class ProfessionalWebSignInTest {

	private WireMockServer wireMock;
	private ProfessionalWebSignIn signIn;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		signIn = new ProfessionalWebSignIn(properties(wireMock, "user", "pw", null), http());
		stubSignIn(wireMock);
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void wrongCredentials() {
		wireMock.stubFor(post(urlPathEqualTo("/idp/login/post")).atPriority(1).willReturn(ok(loginPage("/idp/login/retry"))));

		assertThatThrownBy(() -> signIn.signIn(cookies())).hasMessageContaining("sign in a second time");
	}

	@Test
	void waitingRoom() {
		wireMock.stubFor(post(urlPathEqualTo("/idp/login/post")).atPriority(1)
			.willReturn(ok("<form action=\"/idp/wait\"><input type=hidden name=poll value=1></form>")));
		wireMock.stubFor(post(urlPathEqualTo("/idp/wait")).willReturn(ok("<form action=\"/idp/wait\"><input type=hidden name=poll value=2></form>")));

		assertThatThrownBy(() -> signIn.signIn(cookies())).hasMessageContaining("waiting at /idp/wait");
	}

	@Test
	void noPasswordField() {
		wireMock.stubFor(get(urlPathEqualTo("/idp/login")).atPriority(1).willReturn(ok("<p>Ange engångskod</p>")));

		assertThatThrownBy(() -> signIn.signIn(cookies())).hasMessageContaining("no password field");
	}

	@Test
	void noAccount() {
		final var withoutAccount = new ProfessionalWebSignIn(properties(wireMock, null, null, null), http());

		assertThatThrownBy(() -> withoutAccount.signIn(cookies())).hasMessageContaining("No Lifecare account configured");
	}

	@Test
	void unreachable() {
		final var properties = new ProfessionalWebProperties("http://localhost:1", "d", "a", "saml", null, "u", "p", Duration.ofMinutes(1), 1, 1, null, Duration.ofMinutes(5));
		final var broken = new ProfessionalWebSignIn(properties, http());

		assertThatThrownBy(() -> broken.signIn(cookies())).hasMessageContaining("could not be reached");
	}

	private static ProfessionalWebCookies cookies() {
		return new ProfessionalWebCookies(Clock.systemUTC());
	}
}
