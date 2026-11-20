package apptest;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

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

	@Test
	void test02_writeReminders() {
		setupCall()
			.withServicePath("/2281/professional-web/reminders")
			.withHttpMethod(POST)
			.withRequest("create-request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("create-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(PUT)
			.withRequest("update-request.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_lifecareRefuses() {
		setupCall()
			.withServicePath("/2281/professional-web/reminders/7")
			.withHttpMethod(PUT)
			.withRequest("update-request-7.json")
			.withExpectedResponseStatus(UNPROCESSABLE_CONTENT)
			.withExpectedResponse("refused-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/reminders/8")
			.withHttpMethod(PUT)
			.withRequest("update-request-8.json")
			.withExpectedResponseStatus(BAD_GATEWAY)
			.withExpectedResponse("failed-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_sessionExpired() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/reminders")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
