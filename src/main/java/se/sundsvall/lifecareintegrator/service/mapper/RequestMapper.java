package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.PersonBasedCalculationExpensePostDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationIncomePostDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationPersonPostDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationSpecialExpensePostDTO;
import generated.se.sundsvall.familycare.PostAktualiseringsBodyRequest;
import generated.se.sundsvall.familycare.PostCalculationBodyRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationExpenseRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationIncomeRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareDates.startOfDay;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toDouble;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDateTime;

/**
 * Maps the public create requests to the FC POST bodies. The person number is resolved by the service and injected here
 * — it is never present in the public requests. The string-typed date fields are rendered by
 * {@link se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareDates}: FC rejects a bare date.
 */
public final class RequestMapper {

	private RequestMapper() {}

	public static PostAktualiseringsBodyRequest toPostAktualiseringsBodyRequest(final CreateActualisationRequest request, final String personNumber) {
		return new PostAktualiseringsBodyRequest()
			.personId(personNumber)
			.date(startOfDay(request.getDate()))
			.type(request.getTypeId())
			.fromWho(request.getFromWhoId())
			.reason(request.getReasonId())
			.organisationId(request.getOrganisationId())
			.organisationUnitId(request.getOrganisationUnitId())
			.caseworkerId(request.getCaseworkerId())
			.specifies(request.getSpecifiesId())
			.serviceId(request.getServiceId())
			.investigationId(request.getInvestigationId())
			.workingStatus(request.getWorkingStatusId());
	}

	public static PostCalculationBodyRequest toPostCalculationBodyRequest(final CreateCalculationRequest request, final String personNumber,
		final Map<String, String> personNumbersByPartyId) {
		return new PostCalculationBodyRequest()
			.personId(personNumber)
			.serviceId(request.getServiceId())
			.investigationId(request.getInvestigationId())
			.normId(request.getNormId())
			.aktualiseringId(request.getActualisationId())
			.calculationDate(startOfDay(request.getCalculationDate()))
			.calculationFromDate(startOfDay(request.getCalculationFromDate()))
			.calculationToDate(startOfDay(request.getCalculationToDate()))
			.hasCustomHouseholdSize(request.getHasCustomHouseholdSize())
			.householdSize(request.getHouseholdSize())
			.calculationPersons(toCalculationPersons(request.getPersons(), personNumbersByPartyId))
			.calculationIncomes(toCalculationIncomes(request.getIncomes()))
			.calculationExpenses(toCalculationExpenses(request.getExpenses()))
			.calculationSpecialExpenses(toCalculationSpecialExpenses(request.getSpecialExpenses()));
	}

	private static List<PersonBasedCalculationPersonPostDTO> toCalculationPersons(final List<CalculationPersonRequest> persons, final Map<String, String> personNumbersByPartyId) {
		return Optional.ofNullable(persons).orElse(emptyList()).stream()
			.map(person -> new PersonBasedCalculationPersonPostDTO()
				.personId(personNumbersByPartyId.get(person.getPartyId()))
				.numberOfDays(person.getNumberOfDays())
				.deviationFromDate(toLocalDateTime(person.getDeviationFromDate()))
				.deviationToDate(toLocalDateTime(person.getDeviationToDate())))
			.toList();
	}

	private static List<PersonBasedCalculationIncomePostDTO> toCalculationIncomes(final List<CalculationIncomeRequest> incomes) {
		return Optional.ofNullable(incomes).orElse(emptyList()).stream()
			.map(income -> new PersonBasedCalculationIncomePostDTO()
				.id(income.getTypeId())
				.applicantAmount(toDouble(income.getApplicantAmount()))
				.applicantAmountDate(toLocalDateTime(income.getApplicantAmountDate()))
				.coApplicantAmount(toDouble(income.getCoApplicantAmount()))
				.coApplicantAmountDate(toLocalDateTime(income.getCoApplicantAmountDate()))
				.note(income.getNote()))
			.toList();
	}

	private static List<PersonBasedCalculationExpensePostDTO> toCalculationExpenses(final List<CalculationExpenseRequest> expenses) {
		return Optional.ofNullable(expenses).orElse(emptyList()).stream()
			.map(expense -> new PersonBasedCalculationExpensePostDTO()
				.id(expense.getTypeId())
				.amount(toDouble(expense.getAmount()))
				.approvedAmount(toDouble(expense.getApprovedAmount()))
				.note(expense.getNote()))
			.toList();
	}

	private static List<PersonBasedCalculationSpecialExpensePostDTO> toCalculationSpecialExpenses(final List<CalculationExpenseRequest> specialExpenses) {
		return Optional.ofNullable(specialExpenses).orElse(emptyList()).stream()
			.map(specialExpense -> new PersonBasedCalculationSpecialExpensePostDTO()
				.id(specialExpense.getTypeId())
				.amount(toDouble(specialExpense.getAmount()))
				.approvedAmount(toDouble(specialExpense.getApprovedAmount()))
				.note(specialExpense.getNote()))
			.toList();
	}

}
