package se.sundsvall.lifecareintegrator.integration.familycare;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedDecisionDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedPaymentDTO;
import generated.se.sundsvall.familycare.PersonBasedDecisionDTO;
import generated.se.sundsvall.familycare.PersonBasedPersonDTO;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.exception.ClientProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class FamilyCareIntegrationTest {

	private static final String PERSON_NUMBER = "199001011234";
	private static final String DOCUMENT_ID = "12345";
	private static final LocalDate START_DATE = LocalDate.of(2016, Month.JULY, 1);
	private static final LocalDate END_DATE = LocalDate.of(2026, Month.JULY, 1);
	private static final String START = "2016-07-01T00:00:00";
	private static final String END = "2026-07-01T23:59:59";

	@Mock
	private FamilyCareClient familyCareClientMock;

	@InjectMocks
	private FamilyCareIntegration familyCareIntegration;

	@Test
	void getPerson() {
		// Parameter values
		final var person = new PersonBasedPersonDTO().name("Test Person");

		// Mock
		when(familyCareClientMock.getPerson(PERSON_NUMBER)).thenReturn(person);

		// Act
		final var result = familyCareIntegration.getPerson(PERSON_NUMBER);

		// Verify
		assertThat(result).hasValue(person);
		verify(familyCareClientMock).getPerson(PERSON_NUMBER);
		verifyNoMoreInteractions(familyCareClientMock);
	}

	@Test
	void getPersonNotFound() {
		// Mock
		when(familyCareClientMock.getPerson(PERSON_NUMBER)).thenThrow(new ClientProblem(NOT_FOUND, "Not Found"));

		// Act
		final var result = familyCareIntegration.getPerson(PERSON_NUMBER);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getPersonOtherClientProblemIsRethrown() {
		// Mock
		when(familyCareClientMock.getPerson(PERSON_NUMBER)).thenThrow(new ClientProblem(BAD_GATEWAY, "Bad Gateway"));

		// Act
		final var exception = assertThrows(ClientProblem.class, () -> familyCareIntegration.getPerson(PERSON_NUMBER));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(BAD_GATEWAY);
	}

	@Test
	void getDocumentContent() {
		// Parameter values
		final var content = new byte[] {
			1, 2, 3
		};

		// Mock
		when(familyCareClientMock.getDocumentContent(DOCUMENT_ID)).thenReturn(content);

		// Act
		final var result = familyCareIntegration.getDocumentContent(DOCUMENT_ID);

		// Verify
		assertThat(result).hasValue(content);
	}

	@Test
	void getDocumentContentNotFound() {
		// Mock
		when(familyCareClientMock.getDocumentContent(DOCUMENT_ID)).thenThrow(new ClientProblem(NOT_FOUND, "Not Found"));

		// Act
		final var result = familyCareIntegration.getDocumentContent(DOCUMENT_ID);

		// Verify
		assertThat(result).isEmpty();
	}

	/** FamilyCare's first page is 0, so a single-page result is fetched once, as page 0. */
	@Test
	void getAllDecisionsSinglePage() {
		// Parameter values
		final var decision = new PersonBasedDecisionDTO().id(1);

		// Mock
		when(familyCareClientMock.getDecisions(PERSON_NUMBER, START, END, 100, 0, null))
			.thenReturn(new ApiPaginationCompositePersonBasedDecisionDTO().totalNumberOfPages(1).result(List.of(decision)));

		// Act
		final var result = familyCareIntegration.getAllDecisions(PERSON_NUMBER, START_DATE, END_DATE);

		// Verify
		assertThat(result).containsExactly(decision);
		verify(familyCareClientMock).getDecisions(PERSON_NUMBER, START, END, 100, 0, null);
		verifyNoMoreInteractions(familyCareClientMock);
	}

	@Test
	void getAllDecisionsPagesThroughAllPages() {
		// Parameter values
		final var firstPageDecision = new PersonBasedDecisionDTO().id(1);
		final var secondPageDecision = new PersonBasedDecisionDTO().id(2);

		// Mock
		when(familyCareClientMock.getDecisions(PERSON_NUMBER, START, END, 100, 0, null))
			.thenReturn(new ApiPaginationCompositePersonBasedDecisionDTO().totalNumberOfPages(2).result(List.of(firstPageDecision)));
		when(familyCareClientMock.getDecisions(PERSON_NUMBER, START, END, 100, 1, null))
			.thenReturn(new ApiPaginationCompositePersonBasedDecisionDTO().totalNumberOfPages(2).result(List.of(secondPageDecision)));

		// Act
		final var result = familyCareIntegration.getAllDecisions(PERSON_NUMBER, START_DATE, END_DATE);

		// Verify
		assertThat(result).containsExactly(firstPageDecision, secondPageDecision);
		verify(familyCareClientMock).getDecisions(PERSON_NUMBER, START, END, 100, 0, null);
		verify(familyCareClientMock).getDecisions(PERSON_NUMBER, START, END, 100, 1, null);
		verifyNoMoreInteractions(familyCareClientMock);
	}

	@Test
	void getAllDecisionsWithNullResponse() {
		// Mock
		when(familyCareClientMock.getDecisions(PERSON_NUMBER, START, END, 100, 0, null)).thenReturn(null);

		// Act
		final var result = familyCareIntegration.getAllDecisions(PERSON_NUMBER, START_DATE, END_DATE);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getPaymentsDelegatesWithFormattedDates() {
		// Parameter values
		final var composite = new ApiPaginationCompositePersonBasedPaymentDTO().pageNumber(2);

		// Mock
		when(familyCareClientMock.getPayments(PERSON_NUMBER, START, END, 15, 1, true)).thenReturn(composite);

		// Act
		final var result = familyCareIntegration.getPayments(PERSON_NUMBER, START_DATE, END_DATE, 15, 2, true);

		// Verify
		assertThat(result).isSameAs(composite);
		// Asked for page 2, so FamilyCare is asked for its page 1.
		verify(familyCareClientMock).getPayments(PERSON_NUMBER, START, END, 15, 1, true);
		verifyNoMoreInteractions(familyCareClientMock);
	}

	@Test
	void getContactsDelegates() {
		// Parameter values
		final var contact = new generated.se.sundsvall.familycare.PersonBasedContactDTO().name("Contact");

		// Mock
		when(familyCareClientMock.getContacts(PERSON_NUMBER)).thenReturn(List.of(contact));

		// Act
		final var result = familyCareIntegration.getContacts(PERSON_NUMBER);

		// Verify
		assertThat(result).containsExactly(contact);
	}

	@Test
	void getContactsWithNullResponse() {
		// Mock
		when(familyCareClientMock.getContacts(PERSON_NUMBER)).thenReturn(null);

		// Act + Verify
		assertThat(familyCareIntegration.getContacts(PERSON_NUMBER)).isEmpty();
	}

	/**
	 * Every period read is asked for page 1 — this service's first page — and must reach FamilyCare as page 0, which is
	 * FamilyCare's. Passing the number through unchanged asked for the second page of every read and quietly returned
	 * nothing.
	 */
	@Test
	void periodReadsDelegateWithFormattedDates() {
		// Mock — each period read forwards the formatted window to the client
		when(familyCareClientMock.getActualisation(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedAktualiseringDTO());
		when(familyCareClientMock.getCalculations(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedCalculationDTO());
		when(familyCareClientMock.getInvestigations(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedInvestigationDTO());
		when(familyCareClientMock.getServices(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedServiceDTO());
		when(familyCareClientMock.getExecutions(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedExecutionDTO());
		when(familyCareClientMock.getResourceAllocations(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedResourceAllocationDTO());
		when(familyCareClientMock.getDocuments(PERSON_NUMBER, START, END, 10, 0, true))
			.thenReturn(new generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedDocumentDTO());

		// Act + Verify
		assertThat(familyCareIntegration.getActualisations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(familyCareIntegration.getCalculations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(familyCareIntegration.getInvestigations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(familyCareIntegration.getServices(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(familyCareIntegration.getExecutions(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(familyCareIntegration.getResourceAllocations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(familyCareIntegration.getDocuments(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
	}

	@Test
	void proposalAndWriteBackDelegate() {
		// Parameter values
		final var actualisationBody = new generated.se.sundsvall.familycare.PostAktualiseringsBodyRequest();
		final var calculationBody = new generated.se.sundsvall.familycare.PostCalculationBodyRequest();
		final var file = new org.springframework.mock.web.MockMultipartFile("file", new byte[] {
			1
		});

		// Mock
		when(familyCareClientMock.getActualisationProposal(PERSON_NUMBER)).thenReturn(new generated.se.sundsvall.familycare.PersonBasedAktualiseringProposalDTO());
		when(familyCareClientMock.createActualisation(actualisationBody)).thenReturn(1);
		when(familyCareClientMock.getCalculationProposal(PERSON_NUMBER)).thenReturn(new generated.se.sundsvall.familycare.PersonBasedCalculationProposalDTO());
		when(familyCareClientMock.createCalculation(calculationBody)).thenReturn(2);

		// Act + Verify
		assertThat(familyCareIntegration.getActualisationProposal(PERSON_NUMBER)).isNotNull();
		assertThat(familyCareIntegration.createActualisation(actualisationBody)).isEqualTo(1);
		assertThat(familyCareIntegration.getCalculationProposal(PERSON_NUMBER)).isNotNull();
		assertThat(familyCareIntegration.createCalculation(calculationBody)).isEqualTo(2);

		familyCareIntegration.addActualisationAttachment(5, "type", "sender", "title", "senderName", file);
		verify(familyCareClientMock).postActualisationAttachment(5, "type", "sender", "title", "senderName", file);
	}

	@Test
	void getUsersDelegatesWithFormattedTimestamps() {
		// Parameter values
		final var modifiedAfter = OffsetDateTime.parse("2026-01-01T02:00:00+02:00");

		// Mock
		when(familyCareClientMock.getUsers(50, 10, "2026-01-01T00:00:00", null)).thenReturn(List.of());

		// Act
		final var result = familyCareIntegration.getUsers(50, 10, modifiedAfter, null);

		// Verify
		assertThat(result).isEmpty();
		verify(familyCareClientMock).getUsers(50, 10, "2026-01-01T00:00:00", null);
		verifyNoMoreInteractions(familyCareClientMock);
	}
}
