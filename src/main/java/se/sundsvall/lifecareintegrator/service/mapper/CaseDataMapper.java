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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
 * Maps the paginated Lifecare family care case data composites (payments, investigations, services, executions and
 * resource allocations) to the public paged responses. Vendor person ids (personnummer) are intentionally dropped and
 * never exposed in the public models.
 */
public final class CaseDataMapper {

	private CaseDataMapper() {}

	public static PagedPaymentResponse toPayments(final ApiPaginationCompositePersonBasedPaymentDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> {
				final var payments = toMappedList(source.getResult(), CaseDataMapper::toPayment);
				return PagedPaymentResponse.create()
					.withPayments(payments)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), payments.size()));
			})
			.orElseGet(() -> PagedPaymentResponse.create().withPayments(emptyList()));
	}

	public static PagedInvestigationResponse toInvestigations(final ApiPaginationCompositePersonBasedInvestigationDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> {
				final var investigations = toMappedList(source.getResult(), CaseDataMapper::toInvestigation);
				return PagedInvestigationResponse.create()
					.withInvestigations(investigations)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), investigations.size()));
			})
			.orElseGet(() -> PagedInvestigationResponse.create().withInvestigations(emptyList()));
	}

	public static PagedServiceResponse toCaseServices(final ApiPaginationCompositePersonBasedServiceDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> {
				final var services = toMappedList(source.getResult(), CaseDataMapper::toCaseService);
				return PagedServiceResponse.create()
					.withServices(services)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), services.size()));
			})
			.orElseGet(() -> PagedServiceResponse.create().withServices(emptyList()));
	}

	public static PagedExecutionResponse toExecutions(final ApiPaginationCompositePersonBasedExecutionDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> {
				final var executions = toMappedList(source.getResult(), CaseDataMapper::toExecution);
				return PagedExecutionResponse.create()
					.withExecutions(executions)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), executions.size()));
			})
			.orElseGet(() -> PagedExecutionResponse.create().withExecutions(emptyList()));
	}

	public static PagedResourceAllocationResponse toResourceAllocations(final ApiPaginationCompositePersonBasedResourceAllocationDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> {
				final var resourceAllocations = toMappedList(source.getResult(), CaseDataMapper::toResourceAllocation);
				return PagedResourceAllocationResponse.create()
					.withResourceAllocations(resourceAllocations)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), resourceAllocations.size()));
			})
			.orElseGet(() -> PagedResourceAllocationResponse.create().withResourceAllocations(emptyList()));
	}

	private static <S, T> List<T> toMappedList(final List<S> sources, final Function<S, T> mapper) {
		return Optional.ofNullable(sources)
			.map(list -> list.stream()
				.map(mapper)
				.toList())
			.orElse(emptyList());
	}

	private static Payment toPayment(final PersonBasedPaymentDTO payment) {
		return Optional.ofNullable(payment)
			.map(source -> Payment.create()
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
				.withPersons(toNamedPersons(source.getPaymentPersonDTOs(), PersonBasedPaymentPersonDTO::getName)))
			.orElse(null);
	}

	private static Investigation toInvestigation(final PersonBasedInvestigationDTO investigation) {
		return Optional.ofNullable(investigation)
			.map(source -> Investigation.create()
				.withId(source.getId())
				.withType(source.getType())
				.withFromDate(toLocalDate(source.getFromDate()))
				.withToDate(toLocalDate(source.getToDate()))
				.withCaseworker(source.getCaseworker())
				.withOrganization(source.getOrganization())
				.withDossierType(source.getDossierType())
				.withApplicant(source.getApplicant())
				.withCoApplicant(source.getCoApplicant())
				.withPersons(toInvestigationPersons(source.getInvestigationPersonDTOs())))
			.orElse(null);
	}

	private static CaseService toCaseService(final PersonBasedServiceDTO service) {
		return Optional.ofNullable(service)
			.map(source -> CaseService.create()
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
				.withPersons(toNamedPersons(source.getServicePersonDTOs(), PersonBasedServicePersonDTO::getName)))
			.orElse(null);
	}

	private static Execution toExecution(final PersonBasedExecutionDTO execution) {
		return Optional.ofNullable(execution)
			.map(source -> Execution.create()
				.withId(source.getId())
				.withType(source.getType())
				.withFromDate(toLocalDate(source.getFromDate()))
				.withToDate(toLocalDate(source.getToDate()))
				.withCaseworker(source.getCaseworker())
				.withOrganization(source.getOrganization())
				.withDossierType(source.getDossierType()))
			.orElse(null);
	}

	private static ResourceAllocation toResourceAllocation(final PersonBasedResourceAllocationDTO resourceAllocation) {
		return Optional.ofNullable(resourceAllocation)
			.map(source -> ResourceAllocation.create()
				.withId(source.getId())
				.withStartDate(toLocalDate(source.getStartDate()))
				.withEndDate(toLocalDate(source.getEndDate()))
				.withPercent(source.getPercent())
				.withResource(source.getResource())
				.withResourceType(source.getResourceType())
				.withServiceId(source.getServiceId()))
			.orElse(null);
	}

	/**
	 * Maps vendor case persons that carry a name only to {@link RelatedPerson} (co-applicant left null). The vendor
	 * personId (personnummer) is intentionally dropped.
	 */
	private static <S> List<RelatedPerson> toNamedPersons(final List<S> persons, final Function<S, String> nameGetter) {
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(person -> RelatedPerson.create().withName(nameGetter.apply(person)))
				.toList())
			.orElse(null);
	}

	/**
	 * Maps vendor investigation persons (name + co-applicant flag) to {@link RelatedPerson}. The vendor personId
	 * (personnummer) is intentionally dropped.
	 */
	private static List<RelatedPerson> toInvestigationPersons(final List<PersonBasedInvestigationPersonDTO> persons) {
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(person -> RelatedPerson.create()
					.withName(person.getName())
					.withCoApplicant(person.getCoApplicant()))
				.toList())
			.orElse(null);
	}
}
