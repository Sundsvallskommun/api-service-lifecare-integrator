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
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPaymentClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.BODY_NOT_A_JSON_OBJECT;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebPaymentServiceTest {

	private static final byte[] LIFECARE_JSON = "{\"payment\":{\"amount\":1250.50},\"personnummer\":\"191212121212\"}".getBytes(UTF_8);
	private static final byte[] BODY = "{ \"amount\": 1250.50,\n \"text\": \"Utbetalning åäö\", \"unknown\": {\"a\": [1]} }".getBytes(UTF_8);

	@Mock
	private ProfessionalWebPaymentClient clientMock;

	@InjectMocks
	private ProfessionalWebPaymentService service;

	@Test
	void getPaymentProposalAsksForTheServiceAsAnInsats() {
		when(clientMock.getPaymentForCreate(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(service.getPaymentProposal(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getPaymentForCreate(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getLatestPaymentsAsksForTheServiceAsAnInsats() {
		when(clientMock.getLatestPayments(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(service.getLatestPayments(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getLatestPayments(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void hasHouseholdAsksWithThePersonLifecareNamesForTheServiceAndTheIsoDate() {
		when(clientMock.getPaymentForCreate(8, 42)).thenReturn("{\"payment\":{\"susPersonId\":\"191212121212\",\"amount\":0},\"payees\":[]}".getBytes(UTF_8));
		when(clientMock.hasHouseholdThisDate("191212121212", "2026-01-05")).thenReturn("false".getBytes(UTF_8));

		assertThat(new String(service.hasHousehold(42, LocalDate.of(2026, 1, 5)), UTF_8)).isEqualTo("false");

		verify(clientMock).getPaymentForCreate(8, 42);
		verify(clientMock).hasHouseholdThisDate("191212121212", "2026-01-05");
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"payment\":{\"susPersonId\":\"\"}}", "{\"payment\":{\"susPersonId\":\"  \"}}", "{\"payment\":{\"susPersonId\":null}}",
		"{\"payment\":{}}", "{\"payment\":null}", "{}", "[]", "not json", ""
	})
	void hasHouseholdWhenLifecareNamesNoPersonForTheServiceIsBadGateway(final String paymentForCreate) {
		when(clientMock.getPaymentForCreate(8, 42)).thenReturn(paymentForCreate.getBytes(UTF_8));

		assertThatThrownBy(() -> service.hasHousehold(42, LocalDate.of(2026, 1, 5)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, "Lifecare named no person for the service");

		verify(clientMock, never()).hasHouseholdThisDate(anyString(), anyString());
	}

	@Test
	void createPaymentSendsTheBodyUnchangedForTheServiceAsAnInsats() {
		when(clientMock.createPayment(8, 42, BODY)).thenReturn("{\"paymentId\":5}".getBytes(UTF_8));

		assertThat(service.createPayment(42, BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"paymentId\":5}"));

		verify(clientMock).createPayment(8, 42, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createPaymentWithAnEmptyAnswerReturnsNothing() {
		when(clientMock.createPayment(8, 42, BODY)).thenReturn(new byte[0]);

		assertThat(service.createPayment(42, BODY)).isEmpty();
	}

	@Test
	void createPayeeSendsTheBodyUnchanged() {
		when(clientMock.createPayee(BODY)).thenReturn("{\"payeeId\":3}".getBytes(UTF_8));

		assertThat(service.createPayee(BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"payeeId\":3}"));

		verify(clientMock).createPayee(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createPayeeWithANullAnswerReturnsNothing() {
		when(clientMock.createPayee(BODY)).thenReturn(null);

		assertThat(service.createPayee(BODY)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"amount\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void createPaymentWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.createPayment(42, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"amount\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void createPayeeWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.createPayee(body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}
}
