package apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

@WireMockAppTestSuite(files = "classpath:/CalculationsIT/", classes = Application.class)
class CalculationsIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getCalculations() {
		setupCall()
			.withServicePath("/2281/calculations?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getCalculationProposal() {
		setupCall()
			.withServicePath("/2281/calculations/proposal?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_createCalculation() {
		setupCall()
			.withServicePath("/2281/calculations")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/calculations/54321$"))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
