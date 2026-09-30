package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalWebHttpTest {

	private static final URI URI_VALUE = URI.create("https://lifecare.sundsvall.se/WESE.FC.ProfessionalWeb/RenderPdf/PrintDecision");

	/** Lifecare's parameter query page for a decision print, masked. */
	private static final String PARAMETER_QUERY = """
		<html><head><title>Parameterfrågor</title></head><body>
		<form id="myForm" method="post"><input type="checkbox" name="51_0_2_1" value="true" checked><input type="hidden" name="51_0_2_1" value="false"><input type="hidden" name="X-LEGACY-TOKEN" value="TOKEN"></form>
		</body></html>""";

	@Test
	void parameterQueryPageIsAnAnswerNotALostSession() {
		assertThat(ProfessionalWebHttp.needsSession(response(200, "text/html; charset=utf-8", PARAMETER_QUERY))).isFalse();
	}

	@Test
	void signInPageIsALostSession() {
		final var signIn = """
			<form action="/IdentityPortalWeb/login" method="post">
			  <input type="hidden" name="uid" value="">
			  <input type="hidden" name="otp" value="">
			</form>""";

		assertThat(ProfessionalWebHttp.needsSession(response(200, "text/html; charset=utf-8", signIn))).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"<form id=\"myForm\" method=\"post\"><input type=\"text\" name=\"a\" value=\"b\"></form>",
		"<form method=\"post\"><input type=\"hidden\" name=\"X-LEGACY-TOKEN\" value=\"TOKEN\"></form>",
		"<p>myForm X-LEGACY-TOKEN</p>"
	})
	void htmlThatIsNotTheParameterQueryIsALostSession(final String html) {
		assertThat(ProfessionalWebHttp.needsSession(response(200, "text/html", html))).isTrue();
	}

	@Test
	void nonHtmlAnswerIsNeverALostSessionWhateverItsBodyHolds() {
		assertThat(ProfessionalWebHttp.needsSession(response(200, "application/json", PARAMETER_QUERY))).isFalse();
	}

	@Test
	void redirectToTheIdentityPortalIsALostSessionEvenOnTheParameterQueryPage() {
		final var headers = HttpHeaders.of(Map.of("Content-Type", List.of("text/html"), "Location", List.of("/IdentityPortalWeb/login")), (name, value) -> true);

		assertThat(ProfessionalWebHttp.needsSession(new ProfessionalWebResponse(302, headers, PARAMETER_QUERY.getBytes(StandardCharsets.UTF_8), URI_VALUE))).isTrue();
	}

	@ParameterizedTest
	@ValueSource(ints = {
		360, 401
	})
	void sessionStatusesAreALostSession(final int status) {
		assertThat(ProfessionalWebHttp.needsSession(response(status, "application/json", "{}"))).isTrue();
	}

	@Test
	void otherAnswersAreNotALostSession() {
		assertThat(ProfessionalWebHttp.needsSession(response(200, "application/pdf", "%PDF"))).isFalse();
		assertThat(ProfessionalWebHttp.needsSession(response(403, "application/json", "{}"))).isFalse();
		assertThat(ProfessionalWebHttp.needsSession(response(461, "application/json", "{}"))).isFalse();
	}

	@Test
	void encodeFormListKeepsTheOrderAndRepeatedNames() {
		final var fields = List.of(Map.entry("51_0_2_1", "true"), Map.entry("51_0_2_1", "false"), Map.entry("X-LEGACY-TOKEN", "TOKEN"));

		assertThat(new String(ProfessionalWebHttp.encodeForm(fields), StandardCharsets.UTF_8))
			.isEqualTo("51_0_2_1=true&51_0_2_1=false&X-LEGACY-TOKEN=TOKEN");
	}

	@Test
	void encodeFormListEncodesNamesAndValuesAndAllowsAnEmptyValue() {
		final var fields = List.of(Map.entry("a b&c", "å=ö/+"), Map.entry("empty", ""));

		assertThat(new String(ProfessionalWebHttp.encodeForm(fields), StandardCharsets.UTF_8))
			.isEqualTo("a+b%26c=%C3%A5%3D%C3%B6%2F%2B&empty=");
	}

	@Test
	void encodeFormListOfNothingIsAnEmptyBody() {
		assertThat(ProfessionalWebHttp.encodeForm(List.<Map.Entry<String, String>>of())).isEmpty();
	}

	@Test
	void encodeFormMapIsUnchanged() {
		final var fields = new LinkedHashMap<String, String>();
		fields.put("uid", "user");
		fields.put("otp", "sec ret&1");

		assertThat(new String(ProfessionalWebHttp.encodeForm(fields), StandardCharsets.UTF_8)).isEqualTo("uid=user&otp=sec+ret%261");
	}

	private static ProfessionalWebResponse response(final int status, final String contentType, final String body) {
		final var headers = HttpHeaders.of(Map.of("Content-Type", List.of(contentType)), (name, value) -> true);
		return new ProfessionalWebResponse(status, headers, body.getBytes(StandardCharsets.UTF_8), URI_VALUE);
	}
}
