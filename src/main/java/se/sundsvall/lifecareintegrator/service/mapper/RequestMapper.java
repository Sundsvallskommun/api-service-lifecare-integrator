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
import java.util.function.Function;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toDateString;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toOffsetDateTime;

/**
 * Maps the public create requests to the FC POST bodies. The person number is resolved by the service and injected
 * here — it is never present in the public requests.
 */
public final class RequestMapper {

	private RequestMapper() {}

	public static PostAktualiseringsBodyRequest toPostAktualiseringsBodyRequest(final CreateActualisationRequest request, final String personNumber) {
		return Optional.ofNullable(request)
			.map(source -> new PostAktualiseringsBodyRequest()
				.personId(personNumber)
				.date(toDateString(source.getDate()))
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
				.calculationDate(toDateString(source.getCalculationDate()))
				.calculationFromDate(toDateString(source.getCalculationFromDate()))
				.calculationToDate(toDateString(source.getCalculationToDate()))
				.hasCustomHouseholdSize(source.getHasCustomHouseholdSize())
				.householdSize(source.getHouseholdSize())
				.calculationPersons(mapList(source.getPersons(), person -> new PersonBasedCalculationPersonPostDTO()
					.personId(personNumbersByPartyId.get(person.getPartyId()))
					.numberOfDays(person.getNumberOfDays())
					.deviationFromDate(toOffsetDateTime(person.getDeviationFromDate()))
					.deviationToDate(toOffsetDateTime(person.getDeviationToDate()))))
				.calculationIncomes(mapList(source.getIncomes(), income -> new PersonBasedCalculationIncomePostDTO()
					.id(income.getTypeId())
					.applicantAmount(income.getApplicantAmount())
					.applicantAmountDate(toOffsetDateTime(income.getApplicantAmountDate()))
					.coApplicantAmount(income.getCoApplicantAmount())
					.coApplicantAmountDate(toOffsetDateTime(income.getCoApplicantAmountDate()))
					.note(income.getNote())))
				.calculationExpenses(mapList(source.getExpenses(), expense -> new PersonBasedCalculationExpensePostDTO()
					.id(expense.getTypeId())
					.amount(expense.getAmount())
					.approvedAmount(expense.getApprovedAmount())
					.note(expense.getNote())))
				.calculationSpecialExpenses(mapList(source.getSpecialExpenses(), specialExpense -> new PersonBasedCalculationSpecialExpensePostDTO()
					.id(specialExpense.getTypeId())
					.amount(specialExpense.getAmount())
					.approvedAmount(specialExpense.getApprovedAmount())
					.note(specialExpense.getNote()))))
			.orElse(null);
	}

	private static <S, T> List<T> mapList(final List<S> source, final Function<S, T> mapper) {
		return Optional.ofNullable(source)
			.map(list -> list.stream()
				.map(mapper)
				.toList())
			.orElse(emptyList());
	}
}
