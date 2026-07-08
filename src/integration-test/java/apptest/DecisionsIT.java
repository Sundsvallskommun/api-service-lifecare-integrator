package apptest;

import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

/**
 * End-to-end tests for the unified decisions endpoint, which fans out to the three Lifecare decision sources (EC SoL,
 * EC LSS and FC) in parallel and merges the results. The decode runs on the {@code MDC_EXECUTOR} async pool;
 * {@code MDCTaskDecorator} propagates the request context so the request-scoped Feign message converters resolve, which
 * keeps the merged result deterministic. Assertions are strict apart from array order.
 */
@WireMockAppTestSuite(files = "classpath:/DecisionsIT/", classes = Application.class)
class DecisionsIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String RESPONSE_FILE = "response.json";

	/**
	 * All three sources return one decision each; the merged response holds all three decisions (sorted by decision date,
	 * most recent first) and reports every source OK.
	 */
	@Test
	void test1_allSourcesOk() {
		setupCall()
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER))
			.withServicePath("/2281/decisions?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * The FC source answers 500 — the request still returns 200 with the two EC decisions and FC reported UNAVAILABLE.
	 */
	@Test
	void test2_familyCareUnavailable() {
		setupCall()
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER))
			.withServicePath("/2281/decisions?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * The party legalIds lookup returns an empty map — the partyId cannot be resolved, so the request fails with 404.
	 */
	@Test
	void test3_partyNotFound() {
		setupCall()
			.withServicePath("/2281/decisions?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
