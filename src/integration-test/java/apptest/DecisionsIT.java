package apptest;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/DecisionsIT/", classes = Application.class)
class DecisionsIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test01_getDecisions() {
		setupCall()
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER))
			.withServicePath("/2281/decisions?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getElderlyCareSolDecision() {
		setupCall()
			.withServicePath("/2281/decisions/1001?partyId=" + PARTY_ID + "&source=ELDERLY_CARE&law=SOL")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getFamilyCareDecision() {
		setupCall()
			.withServicePath("/2281/decisions/3003?partyId=" + PARTY_ID + "&source=FAMILY_CARE")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
