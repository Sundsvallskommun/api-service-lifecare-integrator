package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebCalculationClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.BODY_NOT_A_JSON_OBJECT;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebJobStimulusServiceTest {

	private static final byte[] BODY = "{ \"personId\": \"19121212-1212\",\n \"text\": \"åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);

	@Mock
	private ProfessionalWebCalculationClient clientMock;

	@InjectMocks
	private ProfessionalWebJobStimulusService service;

	@Test
	void getJobStimulusEndDateAsksWithTheIsoDate() {
		when(clientMock.getJobStimulusToDate("2026-01-05")).thenReturn("\"2028-01-04\"".getBytes(UTF_8));

		assertThat(new String(service.getJobStimulusEndDate(LocalDate.of(2026, 1, 5)), UTF_8)).isEqualTo("\"2028-01-04\"");

		verify(clientMock).getJobStimulusToDate("2026-01-05");
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void saveJobStimulusSendsTheBodyUnchangedAndReturnsLifecaresAnswer() {
		when(clientMock.saveJobStimulus(BODY)).thenReturn("{\"saved\":true}".getBytes(UTF_8));

		assertThat(service.saveJobStimulus(BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"saved\":true}"));

		verify(clientMock).saveJobStimulus(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void saveJobStimulusWithAnEmptyAnswerReturnsNothing() {
		when(clientMock.saveJobStimulus(BODY)).thenReturn(new byte[0]);

		assertThat(service.saveJobStimulus(BODY)).isEmpty();
	}

	@Test
	void saveJobStimulusWithANullAnswerReturnsNothing() {
		when(clientMock.saveJobStimulus(BODY)).thenReturn(null);

		assertThat(service.saveJobStimulus(BODY)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"a\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void saveJobStimulusWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.saveJobStimulus(body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}
}
