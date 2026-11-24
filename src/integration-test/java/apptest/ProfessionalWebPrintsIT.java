package apptest;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebPrintsIT/", classes = Application.class)
class ProfessionalWebPrintsIT extends AbstractAppTest {

	@Test
	void test01_printDecisionAfterParameterQuestions() throws IOException {
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
	void test02_printCalculation() throws IOException {
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
	void test03_printDocument() throws IOException {
		setupCall()
			.withServicePath("/2281/professional-web/documents/22/pdf")
			.withHttpMethod(GET)
			.withHeader(ACCEPT, APPLICATION_PDF_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PDF_VALUE))
			.withExpectedBinaryResponse("print.pdf")
			.sendRequestAndVerifyResponse();
	}
}
