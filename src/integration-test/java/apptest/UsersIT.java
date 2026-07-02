package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

@WireMockAppTestSuite(files = "classpath:/UsersIT/", classes = Application.class)
class UsersIT extends AbstractAppTest {

	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getUsers() {
		setupCall()
			.withServicePath("/2281/users?limit=100")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
