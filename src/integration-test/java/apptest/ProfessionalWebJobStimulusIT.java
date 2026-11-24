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
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebJobStimulusIT/", classes = Application.class)
class ProfessionalWebJobStimulusIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test01_getJobStimulus() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/job-stimulus")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getJobStimulusEndDate() {
		setupCall()
			.withServicePath("/2281/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_saveJobStimulus() {
		setupCall()
			.withServicePath("/2281/professional-web/job-stimulus")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		wiremock.verify(1, postRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Calculation/SaveJobStimulus"))
			.withRequestBody(equalTo(fromTestFile(REQUEST_FILE))));

		verifyStubs();
	}
}
