package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedExecutionDTO;
import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedInvestigationDTO;
import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedPaymentDTO;
import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedResourceAllocationDTO;
import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedServiceDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedExecutionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedInvestigationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedInvestigationPersonDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedPaymentDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedPaymentPersonDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedResourceAllocationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedServiceDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedServicePersonDTO;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.api.model.familycare.InvestigationPerson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CaseDataMapperTest {

	@Test
	void toPaymentsWithNull() {
		final var result = CaseDataMapper.toPayments(null);
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void toPayments() {
		// Arrange: person personId (personnummer) must never survive — only names are mapped
		final var composite = new ApiPaginationCompositePersonBasedPaymentDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(2)
			.totalNumberOfRecords(15)
			.result(List.of(new PersonBasedPaymentDTO()
				.id(1)
				.amount(5000.0)
				.paymentMethod("Bankgiro")
				.payDate("2026-05-01T00:00:00")
				.clearing("1234")
				.accountNumber("567890")
				.name("Kalle Karlsson")
				.streetAddress("Storgatan 1")
				.careOfAddress("c/o Karlsson")
				.postalCode("85101")
				.postalAddress("Sundsvall")
				.billingNumber("B-100")
				.localNumber("L-200")
				.voucherNumber("V-300")
				.message("Utbetalning maj")
				.investigationExecutionId(2)
				.serviceId(3)
				.connectedApplication(4)
				.concernedMonth("2026-05")
				.paymentPersonDTOs(List.of(new PersonBasedPaymentPersonDTO().personId("199001011234").name("Kalle Karlsson")))));

		// Act
		final var result = CaseDataMapper.toPayments(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(10);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getTotalRecords()).isEqualTo(15);

		final var payment = result.getResults().getFirst();
		assertThat(payment.getId()).isEqualTo(1);
		assertThat(payment.getAmount()).isEqualTo(5000.0);
		assertThat(payment.getPaymentMethod()).isEqualTo("Bankgiro");
		assertThat(payment.getPayDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(payment.getClearing()).isEqualTo("1234");
		assertThat(payment.getAccountNumber()).isEqualTo("567890");
		assertThat(payment.getName()).isEqualTo("Kalle Karlsson");
		assertThat(payment.getStreetAddress()).isEqualTo("Storgatan 1");
		assertThat(payment.getCareOfAddress()).isEqualTo("c/o Karlsson");
		assertThat(payment.getPostalCode()).isEqualTo("85101");
		assertThat(payment.getPostalAddress()).isEqualTo("Sundsvall");
		assertThat(payment.getBillingNumber()).isEqualTo("B-100");
		assertThat(payment.getLocalNumber()).isEqualTo("L-200");
		assertThat(payment.getVoucherNumber()).isEqualTo("V-300");
		assertThat(payment.getMessage()).isEqualTo("Utbetalning maj");
		assertThat(payment.getInvestigationExecutionId()).isEqualTo(2);
		assertThat(payment.getServiceId()).isEqualTo(3);
		assertThat(payment.getConnectedApplication()).isEqualTo(4);
		assertThat(payment.getConcernedMonth()).isEqualTo("2026-05");
		assertThat(payment.getPersons()).containsExactly("Kalle Karlsson");
	}

	@Test
	void toPaymentsWithUnparseableDate() {
		final var composite = new ApiPaginationCompositePersonBasedPaymentDTO()
			.result(List.of(new PersonBasedPaymentDTO().payDate("garbage-date")));

		final var result = CaseDataMapper.toPayments(composite);

		assertThat(result.getResults().getFirst().getPayDate()).isNull();
	}

	@Test
	void toInvestigationsWithNull() {
		final var result = CaseDataMapper.toInvestigations(null);
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void toInvestigations() {
		// Arrange: person personId (personnummer) dropped — only name + coApplicant mapped
		final var composite = new ApiPaginationCompositePersonBasedInvestigationDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(2)
			.totalNumberOfRecords(15)
			.result(List.of(new PersonBasedInvestigationDTO()
				.id(1)
				.type("Utredning EB")
				.fromDate("2026-05-01")
				.toDate("2026-05-31")
				.caseworker("Anna Andersson")
				.organization("Vuxen försörjningsstöd")
				.dossierType("EB")
				.applicant("Kalle Karlsson")
				.coApplicant("Lisa Larsson")
				.investigationPersonDTOs(List.of(
					new PersonBasedInvestigationPersonDTO().personId("199001011234").name("Kalle Karlsson").coApplicant(false),
					new PersonBasedInvestigationPersonDTO().personId("199212312345").name("Lisa Larsson").coApplicant(true)))));

		// Act
		final var result = CaseDataMapper.toInvestigations(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(10);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getTotalRecords()).isEqualTo(15);

		final var investigation = result.getResults().getFirst();
		assertThat(investigation.getId()).isEqualTo(1);
		assertThat(investigation.getType()).isEqualTo("Utredning EB");
		assertThat(investigation.getFromDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(investigation.getToDate()).isEqualTo(LocalDate.parse("2026-05-31"));
		assertThat(investigation.getCaseworker()).isEqualTo("Anna Andersson");
		assertThat(investigation.getOrganization()).isEqualTo("Vuxen försörjningsstöd");
		assertThat(investigation.getDossierType()).isEqualTo("EB");
		assertThat(investigation.getApplicant()).isEqualTo("Kalle Karlsson");
		assertThat(investigation.getCoApplicant()).isEqualTo("Lisa Larsson");
		assertThat(investigation.getPersons())
			.extracting(InvestigationPerson::getName, InvestigationPerson::getCoApplicant)
			.containsExactly(tuple("Kalle Karlsson", false), tuple("Lisa Larsson", true));
	}

	@Test
	void toInvestigationsWithUnparseableDate() {
		final var composite = new ApiPaginationCompositePersonBasedInvestigationDTO()
			.result(List.of(new PersonBasedInvestigationDTO().fromDate("garbage-date").toDate("2026")));

		final var result = CaseDataMapper.toInvestigations(composite);

		assertThat(result.getResults().getFirst().getFromDate()).isNull();
		assertThat(result.getResults().getFirst().getToDate()).isNull();
	}

	@Test
	void toCaseServicesWithNull() {
		final var result = CaseDataMapper.toCaseServices(null);
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void toCaseServices() {
		// Arrange: person personId (personnummer) must never survive — only names are mapped
		final var composite = new ApiPaginationCompositePersonBasedServiceDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(2)
			.totalNumberOfRecords(15)
			.result(List.of(new PersonBasedServiceDTO()
				.id(1)
				.type("Insats EB")
				.organization("Vuxen försörjningsstöd")
				.startDate("2026-05-01")
				.endDate("2026-05-31")
				.caseworker("Anna Andersson")
				.coCaseworker("Bo Bengtsson")
				.investigationId(2)
				.decisionId(3)
				.applicant("Kalle Karlsson")
				.coApplicant("Lisa Larsson")
				.servicePersonDTOs(List.of(new PersonBasedServicePersonDTO().personId("199001011234").name("Kalle Karlsson")))));

		// Act
		final var result = CaseDataMapper.toCaseServices(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(10);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getTotalRecords()).isEqualTo(15);

		final var service = result.getResults().getFirst();
		assertThat(service.getId()).isEqualTo(1);
		assertThat(service.getType()).isEqualTo("Insats EB");
		assertThat(service.getOrganization()).isEqualTo("Vuxen försörjningsstöd");
		assertThat(service.getStartDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(service.getEndDate()).isEqualTo(LocalDate.parse("2026-05-31"));
		assertThat(service.getCaseworker()).isEqualTo("Anna Andersson");
		assertThat(service.getCoCaseworker()).isEqualTo("Bo Bengtsson");
		assertThat(service.getInvestigationId()).isEqualTo(2);
		assertThat(service.getDecisionId()).isEqualTo(3);
		assertThat(service.getApplicant()).isEqualTo("Kalle Karlsson");
		assertThat(service.getCoApplicant()).isEqualTo("Lisa Larsson");
		assertThat(service.getPersons()).containsExactly("Kalle Karlsson");
	}

	@Test
	void toExecutionsWithNull() {
		final var result = CaseDataMapper.toExecutions(null);
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void toExecutions() {
		// Arrange
		final var composite = new ApiPaginationCompositePersonBasedExecutionDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(2)
			.totalNumberOfRecords(15)
			.result(List.of(new PersonBasedExecutionDTO()
				.id(1)
				.type("Verkställighet")
				.fromDate("2026-05-01")
				.toDate("2026-05-31")
				.caseworker("Anna Andersson")
				.organization("Vuxen försörjningsstöd")
				.dossierType("EB")));

		// Act
		final var result = CaseDataMapper.toExecutions(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(10);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getTotalRecords()).isEqualTo(15);

		final var execution = result.getResults().getFirst();
		assertThat(execution.getId()).isEqualTo(1);
		assertThat(execution.getType()).isEqualTo("Verkställighet");
		assertThat(execution.getFromDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(execution.getToDate()).isEqualTo(LocalDate.parse("2026-05-31"));
		assertThat(execution.getCaseworker()).isEqualTo("Anna Andersson");
		assertThat(execution.getOrganization()).isEqualTo("Vuxen försörjningsstöd");
		assertThat(execution.getDossierType()).isEqualTo("EB");
	}

	@Test
	void toResourceAllocationsWithNull() {
		final var result = CaseDataMapper.toResourceAllocations(null);
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void toResourceAllocations() {
		// Arrange
		final var composite = new ApiPaginationCompositePersonBasedResourceAllocationDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(2)
			.totalNumberOfRecords(15)
			.result(List.of(new PersonBasedResourceAllocationDTO()
				.id(1)
				.startDate("2026-05-01")
				.endDate("2026-05-31")
				.percent(50)
				.resource("Handläggare")
				.resourceType("Personal")
				.serviceId(2)));

		// Act
		final var result = CaseDataMapper.toResourceAllocations(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(10);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getTotalRecords()).isEqualTo(15);

		final var allocation = result.getResults().getFirst();
		assertThat(allocation.getId()).isEqualTo(1);
		assertThat(allocation.getStartDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(allocation.getEndDate()).isEqualTo(LocalDate.parse("2026-05-31"));
		assertThat(allocation.getPercent()).isEqualTo(50);
		assertThat(allocation.getResource()).isEqualTo("Handläggare");
		assertThat(allocation.getResourceType()).isEqualTo("Personal");
		assertThat(allocation.getServiceId()).isEqualTo(2);
	}
}
