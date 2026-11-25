package apptest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebIT/", classes = Application.class)
class ProfessionalWebIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test01_getCalculationProposal() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/calculations/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getLatestCalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/calculations/latest")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_placePersons() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/place-persons")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getCalculationAmount() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/amount")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getCalculationSharedCost() throws ClassNotFoundException {
		final var sharedCost = setupCall()
			.withServicePath("/2281/professional-web/calculations/shared-cost")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(BigDecimal.class);

		// The JSON comparison above is numeric. BigDecimal.equals also compares the scale, so the bare number has to
		// arrive as Lifecare wrote it
		assertThat(sharedCost).isEqualTo(new BigDecimal("2030.50"));
	}

	@Test
	void test06_getCalculationJobStimuli() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/job-stimuli")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createCalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/calculations")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_getCalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/7")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_getCalculationForEdit() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/7/edit")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_updateCalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/7")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_deleteCalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/7")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test12_getDecisionProposal() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=7")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_getDecisionReasons() {
		setupCall()
			.withServicePath("/2281/professional-web/decision-codes/12/reasons")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_createDecision() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/decisions")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_getDecision() {
		setupCall()
			.withServicePath("/2281/professional-web/decisions/9")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_updateDecision() {
		setupCall()
			.withServicePath("/2281/professional-web/decisions/9")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test17_getDocuments() {
		setupCall()
			.withServicePath("/2281/professional-web/persons/" + PARTY_ID + "/documents")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test18_getJournalNoteProposal() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/journal-notes/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test19_createJournalNote() {
		setupCall()
			.withServicePath("/2281/professional-web/journal-notes")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test20_getJournalNoteForEdit() {
		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21/edit")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test21_updateJournalNote() {
		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test22_getJournalNote() {
		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test23_getDocumentProposal() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/documents/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test24_createDocument() {
		setupCall()
			.withServicePath("/2281/professional-web/documents")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test25_getDocumentForEdit() {
		setupCall()
			.withServicePath("/2281/professional-web/documents/22/edit")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test26_updateDocument() {
		setupCall()
			.withServicePath("/2281/professional-web/documents/22")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test27_getDocument() {
		setupCall()
			.withServicePath("/2281/professional-web/documents/22")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test28_getJobStimulus() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/job-stimulus")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test29_getJobStimulusEndDate() {
		setupCall()
			.withServicePath("/2281/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test30_saveJobStimulus() {
		setupCall()
			.withServicePath("/2281/professional-web/job-stimulus")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test31_getPaymentProposal() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/payments/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test32_getLatestPayments() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/payments/latest")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test33_hasHousehold() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/household?date=2026-11-01")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test34_createPayee() {
		setupCall()
			.withServicePath("/2281/professional-web/payees")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test35_createPayment() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/payments")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test36_getDecisionPdf() throws IOException {
		setupCall()
			.withServicePath("/2281/professional-web/decisions/9/pdf")
			.withHttpMethod(GET)
			.withHeader(ACCEPT, APPLICATION_PDF_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PDF_VALUE))
			.withExpectedBinaryResponse("decision.pdf")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test37_getCalculationPdf() throws IOException {
		setupCall()
			.withServicePath("/2281/professional-web/calculations/7/pdf")
			.withHttpMethod(GET)
			.withHeader(ACCEPT, APPLICATION_PDF_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PDF_VALUE))
			.withExpectedBinaryResponse("print.pdf")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test38_getDocumentPdf() throws IOException {
		setupCall()
			.withServicePath("/2281/professional-web/documents/22/pdf")
			.withHttpMethod(GET)
			.withHeader(ACCEPT, APPLICATION_PDF_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PDF_VALUE))
			.withExpectedBinaryResponse("print.pdf")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test39_listReminders() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/reminders")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test40_getReminderProposal() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/reminders/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test41_getReminder() {
		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test42_createReminder() {
		setupCall()
			.withServicePath("/2281/professional-web/reminders")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test43_updateReminder() {
		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test44_removeReminder() {
		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}
}
