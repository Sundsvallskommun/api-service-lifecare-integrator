package se.sundsvall.lifecareintegrator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebDecisionClient;

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
class ProfessionalWebDecisionServiceTest {

	private static final byte[] LIFECARE_JSON = "{\"decisionId\":9,\"personnummer\":\"191212121212\",\"unknown\":[1]}".getBytes(UTF_8);
	private static final byte[] BODY = "{ \"decisionId\": 9,\n \"text\": \"Beslut åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);

	@Mock
	private ProfessionalWebDecisionClient clientMock;

	@InjectMocks
	private ProfessionalWebDecisionService service;

	@Test
	void getDecisionProposalAsksForTheServiceAsAnInsatsWithProposalIdZero() {
		when(clientMock.getProposalForService(8, 42, "TotalSum", 7, 0)).thenReturn(LIFECARE_JSON);

		assertThat(service.getDecisionProposal(42, "TotalSum", 7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getProposalForService(8, 42, "TotalSum", 7, 0);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionReasonsAsksWithTheDecisionCode() {
		when(clientMock.getMappedDecisionReasons(5)).thenReturn("[]".getBytes(UTF_8));

		assertThat(new String(service.getDecisionReasons(5), UTF_8)).isEqualTo("[]");

		verify(clientMock).getMappedDecisionReasons(5);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionAsksForTheIdAsADecision() {
		when(clientMock.getDecision(4, 9)).thenReturn(LIFECARE_JSON);

		assertThat(service.getDecision(9)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDecision(4, 9);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createDecisionSendsTheBodyUnchangedForTheServiceAsAnInsats() {
		when(clientMock.create(8, 42, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.createDecision(42, BODY)).hasValue(LIFECARE_JSON);

		verify(clientMock).create(8, 42, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createDecisionWithAnEmptyAnswerReturnsNothing() {
		when(clientMock.create(8, 42, BODY)).thenReturn(new byte[0]);

		assertThat(service.createDecision(42, BODY)).isEmpty();
	}

	@Test
	void updateDecisionSendsTheBodyUnchangedForTheIdAsADecision() {
		when(clientMock.update(4, 9, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.updateDecision(9, BODY)).hasValue(LIFECARE_JSON);

		verify(clientMock).update(4, 9, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateDecisionWithANullAnswerReturnsNothing() {
		when(clientMock.update(4, 9, BODY)).thenReturn(null);

		assertThat(service.updateDecision(9, BODY)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"decisionId\": 9}]", "\"text\"", "42", "null", "{not json", ""
	})
	void writeWithABodyThatIsNotAJsonObjectAsksNothingOfLifecare(final String body) {
		assertThatThrownBy(() -> service.createDecision(42, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.updateDecision(9, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}
}
