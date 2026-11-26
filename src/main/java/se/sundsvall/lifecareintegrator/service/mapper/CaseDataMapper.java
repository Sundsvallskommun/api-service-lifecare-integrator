package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedExecutionDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedInvestigationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedPaymentDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedResourceAllocationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedServiceDTO;
import generated.se.sundsvall.familycare.PersonBasedExecutionDTO;
import generated.se.sundsvall.familycare.PersonBasedInvestigationDTO;
import generated.se.sundsvall.familycare.PersonBasedInvestigationPersonDTO;
import generated.se.sundsvall.familycare.PersonBasedPaymentDTO;
import generated.se.sundsvall.familycare.PersonBasedPaymentPersonDTO;
import generated.se.sundsvall.familycare.PersonBasedResourceAllocationDTO;
import generated.se.sundsvall.familycare.PersonBasedServiceDTO;
import generated.se.sundsvall.familycare.PersonBasedServicePersonDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.CaseService;
import se.sundsvall.lifecareintegrator.api.model.familycare.Execution;
import se.sundsvall.lifecareintegrator.api.model.familycare.Investigation;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedExecutionResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedInvestigationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedPaymentResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedResourceAllocationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedServiceResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.Payment;
import se.sundsvall.lifecareintegrator.api.model.familycare.RelatedPerson;
import se.sundsvall.lifecareintegrator.api.model.familycare.ResourceAllocation;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;

/**
 * Maps the paginated Lifecare family care case data composites (payments, investigations, services, executions, and
 * resource allocations) to the public paged responses. Vendor person ids (personnummer) are intentionally dropped and
 * never exposed in the
 * public models.
 */
public final class CaseDataMapper {

	private CaseDataMapper() {}

	public static PagedPaymentResponse toPayments(final ApiPaginationCompositePersonBasedPaymentDTO composite) {
		if (composite == null) {
			return PagedPaymentResponse.create().withPayments(emptyList());
		}

		final var payments = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(CaseDataMapper::toPayment)
			.toList();
		return PagedPaymentResponse.create()
			.withPayments(payments)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), payments.size()));
	}

	public static PagedInvestigationResponse toInvestigations(final ApiPaginationCompositePersonBasedInvestigationDTO composite) {
		if (composite == null) {
			return PagedInvestigationResponse.create().withInvestigations(emptyList());
		}

		final var investigations = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(CaseDataMapper::toInvestigation)
			.toList();
		return PagedInvestigationResponse.create()
			.withInvestigations(investigations)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), investigations.size()));
	}

	public static PagedServiceResponse toCaseServices(final ApiPaginationCompositePersonBasedServiceDTO composite) {
		if (composite == null) {
			return PagedServiceResponse.create().withServices(emptyList());
		}

		final var services = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(CaseDataMapper::toCaseService)
			.toList();
		return PagedServiceResponse.create()
			.withServices(services)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), services.size()));
	}

	public static PagedExecutionResponse toExecutions(final ApiPaginationCompositePersonBasedExecutionDTO composite) {
		if (composite == null) {
			return PagedExecutionResponse.create().withExecutions(emptyList());
		}

		final var executions = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(CaseDataMapper::toExecution)
			.toList();
		return PagedExecutionResponse.create()
			.withExecutions(executions)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), executions.size()));
	}

	public static PagedResourceAllocationResponse toResourceAllocations(final ApiPaginationCompositePersonBasedResourceAllocationDTO composite) {
		if (composite == null) {
			return PagedResourceAllocationResponse.create().withResourceAllocations(emptyList());
		}

		final var resourceAllocations = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(CaseDataMapper::toResourceAllocation)
			.toList();
		return PagedResourceAllocationResponse.create()
			.withResourceAllocations(resourceAllocations)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), resourceAllocations.size()));
	}

	private static Payment toPayment(final PersonBasedPaymentDTO source) {
		return Payment.create()
			.withId(source.getId())
			.withAmount(toBigDecimal(source.getAmount()))
			.withPaymentMethod(source.getPaymentMethod())
			.withPayDate(toLocalDate(source.getPayDate()))
			.withClearing(source.getClearing())
			.withAccountNumber(source.getAccountNumber())
			.withName(source.getName())
			.withStreetAddress(source.getStreetAddress())
			.withCareOfAddress(source.getCareOfAddress())
			.withPostalCode(source.getPostalCode())
			.withPostalAddress(source.getPostalAddress())
			.withBillingNumber(source.getBillingNumber())
			.withLocalNumber(source.getLocalNumber())
			.withVoucherNumber(source.getVoucherNumber())
			.withMessage(source.getMessage())
			.withInvestigationExecutionId(source.getInvestigationExecutionId())
			.withServiceId(source.getServiceId())
			.withConnectedApplication(source.getConnectedApplication())
			.withConcernedMonth(source.getConcernedMonth())
			.withPersons(toPaymentPersons(source.getPaymentPersonDTOs()));
	}

	private static Investigation toInvestigation(final PersonBasedInvestigationDTO source) {
		return Investigation.create()
			.withId(source.getId())
			.withType(source.getType())
			.withFromDate(toLocalDate(source.getFromDate()))
			.withToDate(toLocalDate(source.getToDate()))
			.withCaseworker(source.getCaseworker())
			.withOrganization(source.getOrganization())
			.withDossierType(source.getDossierType())
			.withApplicant(source.getApplicant())
			.withCoApplicant(source.getCoApplicant())
			.withPersons(toInvestigationPersons(source.getInvestigationPersonDTOs()));
	}

	private static CaseService toCaseService(final PersonBasedServiceDTO source) {
		return CaseService.create()
			.withId(source.getId())
			.withType(source.getType())
			.withOrganization(source.getOrganization())
			.withStartDate(toLocalDate(source.getStartDate()))
			.withEndDate(toLocalDate(source.getEndDate()))
			.withCaseworker(source.getCaseworker())
			.withCoCaseworker(source.getCoCaseworker())
			.withInvestigationId(source.getInvestigationId())
			.withDecisionId(source.getDecisionId())
			.withApplicant(source.getApplicant())
			.withCoApplicant(source.getCoApplicant())
			.withPersons(toServicePersons(source.getServicePersonDTOs()));
	}

	private static Execution toExecution(final PersonBasedExecutionDTO source) {
		return Execution.create()
			.withId(source.getId())
			.withType(source.getType())
			.withFromDate(toLocalDate(source.getFromDate()))
			.withToDate(toLocalDate(source.getToDate()))
			.withCaseworker(source.getCaseworker())
			.withOrganization(source.getOrganization())
			.withDossierType(source.getDossierType());
	}

	private static ResourceAllocation toResourceAllocation(final PersonBasedResourceAllocationDTO source) {
		return ResourceAllocation.create()
			.withId(source.getId())
			.withStartDate(toLocalDate(source.getStartDate()))
			.withEndDate(toLocalDate(source.getEndDate()))
			.withPercent(source.getPercent())
			.withResource(source.getResource())
			.withResourceType(source.getResourceType())
			.withServiceId(source.getServiceId());
	}

	/** The vendor personId (personnummer) is intentionally dropped. */
	private static List<RelatedPerson> toPaymentPersons(final List<PersonBasedPaymentPersonDTO> persons) {
		return Optional.ofNullable(persons).orElse(emptyList()).stream()
			.map(person -> RelatedPerson.create().withName(person.getName()))
			.toList();
	}

	/** The vendor personId (personnummer) is intentionally dropped. */
	private static List<RelatedPerson> toServicePersons(final List<PersonBasedServicePersonDTO> persons) {
		return Optional.ofNullable(persons).orElse(emptyList()).stream()
			.map(person -> RelatedPerson.create().withName(person.getName()))
			.toList();
	}

	/**
	 * Maps vendor investigation persons (name and co-applicant flag) to {@link RelatedPerson}. The vendor personId
	 * (personnummer) is intentionally dropped.
	 */
	private static List<RelatedPerson> toInvestigationPersons(final List<PersonBasedInvestigationPersonDTO> persons) {
		return Optional.ofNullable(persons).orElse(emptyList()).stream()
			.map(person -> RelatedPerson.create()
				.withName(person.getName())
				.withCoApplicant(person.getCoApplicant()))
			.toList();
	}
}
