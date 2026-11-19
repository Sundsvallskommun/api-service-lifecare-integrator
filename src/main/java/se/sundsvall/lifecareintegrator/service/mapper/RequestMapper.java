package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.PersonBasedCalculationExpensePostDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationIncomePostDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationPersonPostDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationSpecialExpensePostDTO;
import generated.se.sundsvall.lifecarefc.PostAktualiseringsBodyRequest;
import generated.se.sundsvall.lifecarefc.PostCalculationBodyRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationExpenseRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationIncomeRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.integration.lifecarefc.FamilyCareDates.startOfDay;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toDouble;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDateTime;

/**
 * Maps the public create requests to the FC POST bodies. The person number is resolved by the service and injected here
 * — it is never present in the public requests. The string-typed date fields are rendered by
 * {@link se.sundsvall.lifecareintegrator.integration.lifecarefc.FamilyCareDates}: FC rejects a bare date.
 */
public final class RequestMapper {

	private RequestMapper() {}

	public static PostAktualiseringsBodyRequest toPostAktualiseringsBodyRequest(final CreateActualisationRequest request, final String personNumber) {
		return Optional.ofNullable(request)
			.map(source -> new PostAktualiseringsBodyRequest()
				.personId(personNumber)
				.date(startOfDay(source.getDate()))
				.type(source.getTypeId())
				.fromWho(source.getFromWhoId())
				.reason(source.getReasonId())
				.organisationId(source.getOrganisationId())
				.organisationUnitId(source.getOrganisationUnitId())
				.caseworkerId(source.getCaseworkerId())
				.specifies(source.getSpecifiesId())
				.serviceId(source.getServiceId())
				.investigationId(source.getInvestigationId())
				.workingStatus(source.getWorkingStatusId()))
			.orElse(null);
	}

	public static PostCalculationBodyRequest toPostCalculationBodyRequest(final CreateCalculationRequest request, final String personNumber,
		final Map<String, String> personNumbersByPartyId) {
		return Optional.ofNullable(request)
			.map(source -> new PostCalculationBodyRequest()
				.personId(personNumber)
				.serviceId(source.getServiceId())
				.investigationId(source.getInvestigationId())
				.normId(source.getNormId())
				.aktualiseringId(source.getActualisationId())
				.calculationDate(startOfDay(source.getCalculationDate()))
				.calculationFromDate(startOfDay(source.getCalculationFromDate()))
				.calculationToDate(startOfDay(source.getCalculationToDate()))
				.hasCustomHouseholdSize(source.getHasCustomHouseholdSize())
				.householdSize(source.getHouseholdSize())
				.calculationPersons(toCalculationPersons(source.getPersons(), personNumbersByPartyId))
				.calculationIncomes(toCalculationIncomes(source.getIncomes()))
				.calculationExpenses(toCalculationExpenses(source.getExpenses()))
				.calculationSpecialExpenses(toCalculationSpecialExpenses(source.getSpecialExpenses())))
			.orElse(null);
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
