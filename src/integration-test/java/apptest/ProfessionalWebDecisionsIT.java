package apptest;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebDecisionsIT/", classes = Application.class)
class ProfessionalWebDecisionsIT extends AbstractAppTest {

	@Test
	void test01_proposeCreateReadAndUpdateDecision() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=7")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/decision-codes/12/reasons")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("reasons-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/decisions")
			.withHttpMethod(POST)
			.withRequest("create-request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("create-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/decisions/9")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("decision-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/decisions/9")
			.withHttpMethod(PUT)
			.withRequest("update-request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("update-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_lifecareRefusesUpdate() {
		setupCall()
			.withServicePath("/2281/professional-web/decisions/10")
			.withHttpMethod(PUT)
			.withRequest("update-request.json")
			.withExpectedResponseStatus(UNPROCESSABLE_CONTENT)
			.withExpectedResponse("refused-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_proposeWithoutACalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/decisions/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/decisions/proposal?amountType=&calculationId=0")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		wiremock.verify(2, getRequestedFor(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Decision/GetProposalForService?businessType=8&businessId=42&amountType=&calculationId=0&proposalId=0")));

		verifyStubs();
	}
}
