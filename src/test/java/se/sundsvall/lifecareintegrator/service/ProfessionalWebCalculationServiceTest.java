package se.sundsvall.lifecareintegrator.service;

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
class ProfessionalWebCalculationServiceTest {

	private static final byte[] LIFECARE_JSON = "{\"calculationId\":7,\"personnummer\":\"191212121212\",\"rows\":[{\"amount\":12.50}]}".getBytes(UTF_8);
	private static final byte[] BODY = "{ \"calculationId\": 7,\n \"text\": \"Beräkning åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);

	@Mock
	private ProfessionalWebCalculationClient clientMock;

	@InjectMocks
	private ProfessionalWebCalculationService service;

	@Test
	void getCalculationProposalAsksForTheServiceAsAnInsats() {
		when(clientMock.getProposalService(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(service.getCalculationProposal(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getProposalService(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getLatestCalculationListsOnlyTheLatestOfTheService() {
		when(clientMock.listCalculations(8, 42, 0, 42, true)).thenReturn(LIFECARE_JSON);

		assertThat(service.getLatestCalculation(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).listCalculations(8, 42, 0, 42, true);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationAsksForTheIdAsACalculation() {
		when(clientMock.getCalculation(3, 7)).thenReturn(LIFECARE_JSON);

		assertThat(service.getCalculation(7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getCalculation(3, 7);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationForEditAsksForTheIdAsACalculation() {
		when(clientMock.getCalculationForEdit(3, 7)).thenReturn(LIFECARE_JSON);

		assertThat(service.getCalculationForEdit(7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getCalculationForEdit(3, 7);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJobStimulusAsksForTheServiceAsAnInsats() {
		when(clientMock.getJobStimulusForService(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(service.getJobStimulus(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJobStimulusForService(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void placePersonsSendsTheBodyUnchanged() {
		when(clientMock.placePersons(BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.placePersons(BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).placePersons(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getAmountSendsTheBodyUnchanged() {
		when(clientMock.getAmount(BODY)).thenReturn("{\"amount\":1250.50}".getBytes(UTF_8));

		assertThat(new String(service.getAmount(BODY), UTF_8)).isEqualTo("{\"amount\":1250.50}");

		verify(clientMock).getAmount(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getSharedCostSendsTheBodyUnchanged() {
		when(clientMock.getSharedCost(BODY)).thenReturn("1250.50".getBytes(UTF_8));

		assertThat(new String(service.getSharedCost(BODY), UTF_8)).isEqualTo("1250.50");

		verify(clientMock).getSharedCost(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJobStimuliSendsTheBodyUnchanged() {
		when(clientMock.getJobStimuliForCalculation(BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.getJobStimuli(BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJobStimuliForCalculation(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createCalculationSendsTheBodyUnchangedForTheServiceAsAnInsats() {
		when(clientMock.create(8, 42, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.createCalculation(42, BODY)).hasValue(LIFECARE_JSON);

		verify(clientMock).create(8, 42, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createCalculationWithAnEmptyAnswerReturnsNothing() {
		when(clientMock.create(8, 42, BODY)).thenReturn(new byte[0]);

		assertThat(service.createCalculation(42, BODY)).isEmpty();
	}

	@Test
	void updateCalculationSendsTheBodyUnchangedForTheIdAsACalculation() {
		when(clientMock.update(3, 7, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.updateCalculation(7, BODY)).hasValue(LIFECARE_JSON);

		verify(clientMock).update(3, 7, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateCalculationWithANullAnswerReturnsNothing() {
		when(clientMock.update(3, 7, BODY)).thenReturn(null);

		assertThat(service.updateCalculation(7, BODY)).isEmpty();
	}

	@Test
	void deleteCalculationSendsAnEmptyBodyForTheIdAsACalculation() {
		service.deleteCalculation(7);

		verify(clientMock).delete(3, 7, new byte[0]);
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"a\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void bodyThatIsNotAJsonObjectAsksNothingOfLifecare(final String body) {
		final var bytes = body.getBytes(UTF_8);
		assertBadBody(() -> service.placePersons(bytes));
		assertBadBody(() -> service.getAmount(bytes));
		assertBadBody(() -> service.getSharedCost(bytes));
		assertBadBody(() -> service.getJobStimuli(bytes));
		assertBadBody(() -> service.createCalculation(42, bytes));
		assertBadBody(() -> service.updateCalculation(7, bytes));

		verifyNoInteractions(clientMock);
	}

	private static void assertBadBody(final org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
		assertThatThrownBy(call)
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
	}
}
