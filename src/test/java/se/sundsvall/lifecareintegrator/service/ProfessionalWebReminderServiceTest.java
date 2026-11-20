package se.sundsvall.lifecareintegrator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebReminderClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebReminderService.BODY_NOT_A_JSON_OBJECT;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebReminderServiceTest {

	private static final byte[] LIFECARE_JSON = "{\"id\":42,\"unknownField\":{\"nested\":[1,2]},\"personnummer\":\"191212121212\"}".getBytes(UTF_8);
	private static final byte[] CREATE_BODY = "{ \"serviceId\": 42,\n  \"text\": \"Påminnelse åäö\", \"personnummer\": \"19121212-1212\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);
	private static final byte[] UPDATE_BODY = "{\"reminderId\": 7, \"text\": \"Påminnelse åäö\", \"personnummer\": \"191212121212\", \"unknown\": {\"a\": [1]}}".getBytes(UTF_8);

	@Mock
	private ProfessionalWebReminderClient clientMock;

	@InjectMocks
	private ProfessionalWebReminderService service;

	@Test
	void listReminders() {
		when(clientMock.listRemindersByServiceId(42)).thenReturn(LIFECARE_JSON);

		assertThat(service.listReminders(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).listRemindersByServiceId(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getReminderProposal() {
		when(clientMock.getProposalForService(42)).thenReturn(LIFECARE_JSON);

		assertThat(service.getReminderProposal(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getProposalForService(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getReminder() {
		when(clientMock.getReminderEditComposite(7, false)).thenReturn(LIFECARE_JSON);

		assertThat(service.getReminder(7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getReminderEditComposite(7, false);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createReminderSendsTheBodyUnchangedAndReturnsLifecaresAnswer() {
		when(clientMock.createReminder(CREATE_BODY)).thenReturn("{\"id\":9}".getBytes(UTF_8));

		assertThat(service.createReminder(CREATE_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"id\":9}"));

		verify(clientMock).createReminder(CREATE_BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createReminderWithAnEmptyLifecareAnswerReturnsNothing() {
		when(clientMock.createReminder("{}".getBytes(UTF_8))).thenReturn(new byte[0]);

		assertThat(service.createReminder("{}".getBytes(UTF_8))).isEmpty();

		verify(clientMock).createReminder("{}".getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createReminderWithANullLifecareAnswerReturnsNothing() {
		when(clientMock.createReminder("{}".getBytes(UTF_8))).thenReturn(null);

		assertThat(service.createReminder("{}".getBytes(UTF_8))).isEmpty();

		verify(clientMock).createReminder("{}".getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"reminderId\": 7}]", "\"text\"", "42", "null", "{not json"
	})
	void createReminderWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.createReminder(body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}

	@Test
	void updateReminderSendsTheBodyUnchanged() {
		service.updateReminder(7, UPDATE_BODY);

		verify(clientMock).updateReminder(UPDATE_BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"reminderId\": 8}", "{\"text\": \"x\"}", "{\"reminderId\": \"7\"}", "{\"reminderId\": null}", "{\"reminderId\": 7.5}", "{\"nested\": {\"reminderId\": 7}}"
	})
	void updateReminderWhoseBodyDoesNotNameTheSameReminder(final String body) {
		assertThatThrownBy(() -> service.updateReminder(7, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, "The body's reminderId must be 7");

		verifyNoInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"reminderId\": 7}]", "\"text\"", "42", "null", "{not json"
	})
	void updateReminderWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.updateReminder(7, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}

	@Test
	void removeReminderSendsTheId() {
		service.removeReminder(7);

		verify(clientMock).removeReminder("{\"id\":7}".getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}
}
