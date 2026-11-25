package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Duration;
import okhttp3.OkHttpClient;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebProperties;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * A fake Lifecare and MobilityGuard for the tests that sign in: the flow a browser walks, ending in LEGACY-TOKEN=tok.
 */
public final class SignInStubs {

	private SignInStubs() {}

	public static void stubSignIn(final WireMockServer wireMock) {
		wireMock.stubFor(get(urlPathEqualTo("/WE.Flow.Html")).withQueryParam("domain", equalTo("Domain"))
			.willReturn(aResponse().withStatus(302).withHeader("Location", "/idp/login").withHeader("Set-Cookie", "ASP.NET_SessionId=s; path=/")));
		wireMock.stubFor(get(urlPathEqualTo("/idp/login")).willReturn(ok(loginPage("/idp/login/post"))));
		wireMock.stubFor(post(urlPathEqualTo("/idp/login/post")).willReturn(ok("""
			<form method="post" action="/IdentityPortalWeb/acs"><textarea name="SAMLResponse">PHNhbWw+</textarea></form>
			""")));
		wireMock.stubFor(post(urlPathEqualTo("/IdentityPortalWeb/acs")).willReturn(aResponse().withStatus(302)
			.withHeader("Location", "/WE.Flow.Html/").withHeader("Set-Cookie", "LEGACY-TOKEN=tok; path=/; secure")));
		wireMock.stubFor(get(urlPathEqualTo("/WE.Flow.Html/")).willReturn(ok("<html>Lifecare</html>")));
	}

	public static String loginPage(final String action) {
		return """
			<form action="%s" method="post">
			  <input type="hidden" name="uid" value="">
			  <input type="hidden" name="otp" value="">
			  <input type="hidden" name="state" value="s1">
			</form>
			""".formatted(action);
	}

	public static ProfessionalWebProperties properties(final WireMockServer wireMock, final String username, final String password, final String sessionCookie) {
		return new ProfessionalWebProperties(wireMock.baseUrl() + "/", "Domain", "Actor_Professional", "saml", "Sundsvall_Intra",
			username, password, Duration.ofMinutes(20), 1, 5, sessionCookie, Duration.ofMinutes(5));
	}

	public static ProfessionalWebHttp http() {
		return new ProfessionalWebHttp(new OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).build());
	}
}
