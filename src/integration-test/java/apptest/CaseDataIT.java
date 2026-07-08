package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

@WireMockAppTestSuite(files = "classpath:/CaseDataIT/", classes = Application.class)
class CaseDataIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getPayments() {
		setupCall()
			.withServicePath("/2281/payments?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getInvestigations() {
		setupCall()
			.withServicePath("/2281/investigations?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getServices() {
		setupCall()
			.withServicePath("/2281/services?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_getExecutions() {
		setupCall()
			.withServicePath("/2281/executions?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_getResourceAllocations() {
		setupCall()
			.withServicePath("/2281/resource-allocations?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
