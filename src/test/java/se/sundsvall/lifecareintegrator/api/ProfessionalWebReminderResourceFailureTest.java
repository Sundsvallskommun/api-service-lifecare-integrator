package se.sundsvall.lifecareintegrator.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebReminderResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";
	private static final String NOT_POSITIVE = "must be greater than 0";

	@MockitoBean
	private ProfessionalWebService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void listRemindersWithServiceIdThatIsNotPositive(final String serviceId) {
		final var response = getViolations("/2281/professional-web/services/" + serviceId + "/reminders");

		assertViolation(response, "listReminders.serviceId", NOT_POSITIVE);
		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getReminderProposalWithServiceIdThatIsNotPositive(final String serviceId) {
		final var response = getViolations("/2281/professional-web/services/" + serviceId + "/reminders/proposal");

		assertViolation(response, "getReminderProposal.serviceId", NOT_POSITIVE);
		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "1.5", "99999999999999999999"
	})
	void serviceIdThatIsNotANumberIsRefused(final String serviceId) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/reminders").exchange(), "serviceId");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/reminders/proposal").exchange(), "serviceId");

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getReminderWithReminderIdThatIsNotPositive(final String reminderId) {
		final var response = getViolations("/2281/professional-web/reminders/" + reminderId);

		assertViolation(response, "getReminder.reminderId", NOT_POSITIVE);
		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void updateReminderWithReminderIdThatIsNotPositive(final String reminderId) {
		final var response = readViolations(webTestClient.put().uri("/2281/professional-web/reminders/" + reminderId)
			.contentType(APPLICATION_JSON).bodyValue("{\"reminderId\": 0}").exchange());

		assertViolation(response, "updateReminder.reminderId", NOT_POSITIVE);
		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void removeReminderWithReminderIdThatIsNotPositive(final String reminderId) {
		final var response = readViolations(webTestClient.delete().uri("/2281/professional-web/reminders/" + reminderId).exchange());

		assertViolation(response, "removeReminder.reminderId", NOT_POSITIVE);
		verifyNoInteractions(serviceMock);
	}

	@Test
	void reminderIdThatIsNotANumberIsRefused() {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/reminders/abc").exchange(), "reminderId");
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/reminders/abc").contentType(APPLICATION_JSON).bodyValue("{\"reminderId\": 0}").exchange(), "reminderId");
		assertBadRequest(webTestClient.delete().uri("/2281/professional-web/reminders/abc").exchange(), "reminderId");

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		assertViolation(getViolations("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/reminders"), "listReminders.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(getViolations("/" + INVALID_MUNICIPALITY_ID + "/professional-web/services/42/reminders/proposal"), "getReminderProposal.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(getViolations("/" + INVALID_MUNICIPALITY_ID + "/professional-web/reminders/7"), "getReminder.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/reminders")
			.contentType(APPLICATION_JSON).bodyValue("{}").exchange()), "createReminder.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.put().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/reminders/7")
			.contentType(APPLICATION_JSON).bodyValue("{\"reminderId\": 7}").exchange()), "updateReminder.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.delete().uri("/" + INVALID_MUNICIPALITY_ID + "/professional-web/reminders/7").exchange()),
			"removeReminder.municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void emptyBodyIsRefused() {
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/reminders").contentType(APPLICATION_JSON).exchange());
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/reminders/7").contentType(APPLICATION_JSON).exchange());

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateReminderRefusedByTheService() {
		doThrow(Problem.valueOf(BAD_REQUEST, "The body's reminderId must be 7")).when(serviceMock).updateReminder(7, "{\"reminderId\": 8}".getBytes(UTF_8));

		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.bodyValue("{\"reminderId\": 8}")
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("The body's reminderId must be 7");

		verify(serviceMock).updateReminder(7, "{\"reminderId\": 8}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getReminderNotFoundInLifecare() {
		when(serviceMock.getReminder(7)).thenThrow(Problem.valueOf(NOT_FOUND, "Lifecare found nothing there"));

		webTestClient.get().uri("/2281/professional-web/reminders/7")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.detail").isEqualTo("Lifecare found nothing there");

		verify(serviceMock).getReminder(7);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateReminderRefusedByLifecare() {
		doThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Påminnelsen är klar")).when(serviceMock).updateReminder(eq(7L), any(byte[].class));

		webTestClient.put().uri("/2281/professional-web/reminders/7")
			.contentType(APPLICATION_JSON)
			.bodyValue("{\"reminderId\": 7}")
			.exchange()
			.expectStatus().isEqualTo(422)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Påminnelsen är klar");

		verify(serviceMock).updateReminder(7, "{\"reminderId\": 7}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	private ConstraintViolationProblem getViolations(final String uri) {
		return readViolations(webTestClient.get().uri(uri).exchange());
	}

	private static ConstraintViolationProblem readViolations(final WebTestClient.ResponseSpec response) {
		return response
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();
	}

	private static void assertViolation(final ConstraintViolationProblem response, final String field, final String message) {
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple(field, message));
	}

	private static void assertBadRequest(final WebTestClient.ResponseSpec response) {
		response.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Bad Request");
	}

	private static void assertBadRequest(final WebTestClient.ResponseSpec response, final String parameter) {
		response.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Bad Request")
			.jsonPath("$.detail").value(String.class, detail -> assertThat(detail).contains("'" + parameter + "'"));
	}
}
