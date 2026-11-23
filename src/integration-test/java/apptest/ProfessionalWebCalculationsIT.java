package apptest;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebCalculationsIT/", classes = Application.class)
class ProfessionalWebCalculationsIT extends AbstractAppTest {

	@Test
	void test01_reckonCreateUpdateAndDeleteCalculation() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/calculations/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/calculations/latest")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("latest-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/calculations/amount")
			.withHttpMethod(POST)
			.withRequest("amount-request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("amount-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/calculations")
			.withHttpMethod(POST)
			.withRequest("create-request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("create-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/calculations/7/edit")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("edit-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/calculations/7")
			.withHttpMethod(PUT)
			.withRequest("update-request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("update-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/calculations/7")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}
}
