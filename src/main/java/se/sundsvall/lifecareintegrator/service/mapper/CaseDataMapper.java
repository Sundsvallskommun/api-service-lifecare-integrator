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
import se.sundsvall.lifecareintegrator.api.model.common.PagedResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.CaseService;
import se.sundsvall.lifecareintegrator.api.model.familycare.Execution;
import se.sundsvall.lifecareintegrator.api.model.familycare.Investigation;
import se.sundsvall.lifecareintegrator.api.model.familycare.InvestigationPerson;
import se.sundsvall.lifecareintegrator.api.model.familycare.Payment;
import se.sundsvall.lifecareintegrator.api.model.familycare.ResourceAllocation;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;

/**
 * Maps the paginated Lifecare family care case data composites (payments, investigations, services, executions and
 * resource allocations) to the public paged models. Vendor person ids (personnummer) are intentionally dropped and
 * never exposed in the public models.
 */
public final class CaseDataMapper {

	private CaseDataMapper() {}

	public static PagedResponse<Payment> toPayments(final ApiPaginationCompositePersonBasedPaymentDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> CaseDataMapper.<Payment>toPagedResponse(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords())
				.withResults(toMappedList(source.getResult(), CaseDataMapper::toPayment)))
			.orElseGet(CaseDataMapper::toEmptyPagedResponse);
	}

	public static PagedResponse<Investigation> toInvestigations(final ApiPaginationCompositePersonBasedInvestigationDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> CaseDataMapper.<Investigation>toPagedResponse(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords())
				.withResults(toMappedList(source.getResult(), CaseDataMapper::toInvestigation)))
			.orElseGet(CaseDataMapper::toEmptyPagedResponse);
	}

	public static PagedResponse<CaseService> toCaseServices(final ApiPaginationCompositePersonBasedServiceDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> CaseDataMapper.<CaseService>toPagedResponse(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords())
				.withResults(toMappedList(source.getResult(), CaseDataMapper::toCaseService)))
			.orElseGet(CaseDataMapper::toEmptyPagedResponse);
	}

	public static PagedResponse<Execution> toExecutions(final ApiPaginationCompositePersonBasedExecutionDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> CaseDataMapper.<Execution>toPagedResponse(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords())
				.withResults(toMappedList(source.getResult(), CaseDataMapper::toExecution)))
			.orElseGet(CaseDataMapper::toEmptyPagedResponse);
	}

	public static PagedResponse<ResourceAllocation> toResourceAllocations(final ApiPaginationCompositePersonBasedResourceAllocationDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> CaseDataMapper.<ResourceAllocation>toPagedResponse(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords())
				.withResults(toMappedList(source.getResult(), CaseDataMapper::toResourceAllocation)))
			.orElseGet(CaseDataMapper::toEmptyPagedResponse);
	}

	private static <T> PagedResponse<T> toPagedResponse(final Integer page, final Integer pageSize, final Integer totalPages, final Integer totalRecords) {
		return PagedResponse.<T>create()
			.withPage(page)
			.withPageSize(pageSize)
			.withTotalPages(totalPages)
			.withTotalRecords(totalRecords);
	}

	private static <T> PagedResponse<T> toEmptyPagedResponse() {
		return PagedResponse.<T>create().withResults(emptyList());
	}

	private static <S, T> List<T> toMappedList(final List<S> sources, final java.util.function.Function<S, T> mapper) {
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
				.withAmount(source.getAmount())
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
				.withPersons(toPaymentPersonNames(source.getPaymentPersonDTOs())))
			.orElse(null);
	}

	private static List<String> toPaymentPersonNames(final List<PersonBasedPaymentPersonDTO> persons) {
		// Intentionally maps names only — the personId (personnummer) never leaves this service
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(PersonBasedPaymentPersonDTO::getName)
				.toList())
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

	private static List<InvestigationPerson> toInvestigationPersons(final List<PersonBasedInvestigationPersonDTO> persons) {
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(CaseDataMapper::toInvestigationPerson)
				.toList())
			.orElse(null);
	}

	private static InvestigationPerson toInvestigationPerson(final PersonBasedInvestigationPersonDTO person) {
		// Intentionally drops the personId — personnummer never leaves this service
		return InvestigationPerson.create()
			.withName(person.getName())
			.withCoApplicant(person.getCoApplicant());
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
				.withPersons(toServicePersonNames(source.getServicePersonDTOs())))
			.orElse(null);
	}

	private static List<String> toServicePersonNames(final List<PersonBasedServicePersonDTO> persons) {
		// Intentionally maps names only — the personId (personnummer) never leaves this service
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(PersonBasedServicePersonDTO::getName)
				.toList())
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
}
