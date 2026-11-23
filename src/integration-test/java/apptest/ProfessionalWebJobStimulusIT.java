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

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebJobStimulusIT/", classes = Application.class)
class ProfessionalWebJobStimulusIT extends AbstractAppTest {

	private static final String SAVE_REQUEST = "save-request.json";

	@Test
	void test01_readEndDateAndSavePeriods() {
		setupCall()
			.withServicePath("/2281/professional-web/job-stimulus/end-date?fromDate=2026-11-01")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("end-date-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/job-stimulus")
			.withHttpMethod(POST)
			.withRequest(SAVE_REQUEST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("save-response.json")
			.sendRequest();

		wiremock.verify(1, postRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Calculation/SaveJobStimulus"))
			.withRequestBody(equalTo(fromTestFile(SAVE_REQUEST))));

		verifyStubs();
	}
}
