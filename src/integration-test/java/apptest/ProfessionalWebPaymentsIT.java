package apptest;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebPaymentsIT/", classes = Application.class)
class ProfessionalWebPaymentsIT extends AbstractAppTest {

	private static final String PAYMENT_REQUEST = "payment-request.json";

	@Test
	void test01_preparePayeeAndPayment() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/payments/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/payments/latest")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("latest-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/household?date=2026-11-01")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("household-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/payees")
			.withHttpMethod(POST)
			.withRequest("payee-request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("payee-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/payments")
			.withHttpMethod(POST)
			.withRequest(PAYMENT_REQUEST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("payment-response.json")
			.sendRequest();

		wiremock.verify(1, postRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Payee/Create")));
		wiremock.verify(1, postRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Payment/Create"))
			.withRequestBody(equalTo(fromTestFile(PAYMENT_REQUEST))));

		verifyStubs();
	}
}
