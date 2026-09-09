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
 * End-to-end tests for the decision endpoints. The list reads the three Lifecare decision sources (EC SoL, EC LSS and
 * FC) one after the other and merges the results; the single-decision read addresses one source by (source, law,
 * decisionId) and checks the decision belongs to the party. Assertions are strict apart from array order.
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

	/**
	 * A SoL decision read by id from EC. The decision carries the party's personnummer, so it is published.
	 */
	@Test
	void test4_elderlyCareSolById() {
		setupCall()
			.withServicePath("/2281/decisions/1001?partyId=" + PARTY_ID + "&source=ELDERLY_CARE&law=SOL")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * FC has no by-id read: the party's decisions are listed over the default window and the one with the id is
	 * picked out.
	 */
	@Test
	void test5_familyCareById() {
		setupCall()
			.withServicePath("/2281/decisions/3003?partyId=" + PARTY_ID + "&source=FAMILY_CARE")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * EC serves any decision by id — here it belongs to another person, so the request fails with 404 rather than
	 * publishing it (or confirming it exists with a 403).
	 */
	@Test
	void test6_elderlyCareByIdWrongPerson() {
		setupCall()
			.withServicePath("/2281/decisions/1001?partyId=" + PARTY_ID + "&source=ELDERLY_CARE&law=SOL")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * EC answers 404 on the LSS by-id read — the Feign client dismisses it and the request fails with 404.
	 */
	@Test
	void test7_elderlyCareLssByIdNotFound() {
		setupCall()
			.withServicePath("/2281/decisions/9999?partyId=" + PARTY_ID + "&source=ELDERLY_CARE&law=LSS")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
