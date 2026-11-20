package apptest;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebRemindersIT/", classes = Application.class)
class ProfessionalWebRemindersIT extends AbstractAppTest {

	@Test
	void test01_readReminders() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/reminders")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("list-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/reminders/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("reminder-response.json")
			.sendRequestAndVerifyResponse();
	}
}
