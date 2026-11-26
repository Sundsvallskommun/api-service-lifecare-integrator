package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedCalculationDTO;
import generated.se.sundsvall.familycare.CommonCalculationExpenseDTO;
import generated.se.sundsvall.familycare.CommonCalculationIncomeDTO;
import generated.se.sundsvall.familycare.CommonCalculationSpecialExpenseDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationPersonDTO;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.Calculation;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationExpense;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationIncome;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPerson;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedCalculationResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPartyId;

public final class CalculationMapper {

	private CalculationMapper() {}

	public static PagedCalculationResponse toCalculations(final ApiPaginationCompositePersonBasedCalculationDTO composite, final Map<String, String> partyIdsByPersonNumber) {
		if (composite == null) {
			return PagedCalculationResponse.create().withCalculations(emptyList());
		}

		final var calculations = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(calculation -> toCalculation(calculation, partyIdsByPersonNumber))
			.toList();
		return PagedCalculationResponse.create()
			.withCalculations(calculations)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), calculations.size()));
	}

	public static Calculation toCalculation(final PersonBasedCalculationDTO calculation, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(calculation)
			.map(source -> Calculation.create()
				.withId(source.getId())
				.withNorm(source.getNorm())
				.withFromDate(toLocalDate(source.getFromDate()))
				.withToDate(toLocalDate(source.getToDate()))
				.withIncomeSum(toBigDecimal(source.getIncomeSum()))
				.withExpenseSum(toBigDecimal(source.getExpenseSum()))
				.withSpecialExpenseSum(toBigDecimal(source.getSpecialExpenseSum()))
				.withNormSum(toBigDecimal(source.getNormSum()))
				.withCommonHouseholdCost(toBigDecimal(source.getCommonHouseholdCost()))
				.withFamilyCost(toBigDecimal(source.getFamilyCost()))
				.withBalance(toBigDecimal(source.getBalance()))
				.withTotalSum(toBigDecimal(source.getTotalSum()))
				.withInvestigationId(source.getInvestigationId())
				.withServiceId(source.getServiceId())
				.withFinalCalculation(source.getFinal())
				.withConnectedApplication(source.getConnectedApplication())
				.withPersons(toCalculationPersons(source.getCalculationPersonDTOs(), partyIdsByPersonNumber))
				.withIncomes(toCalculationIncomes(source.getCalculationIncomesDTOs()))
				.withExpenses(toCalculationExpenses(source.getCalculationExpensesDTOs()))
				.withSpecialExpenses(toCalculationSpecialExpenses(source.getCalculationSpecialExpensesDTOs())))
			.orElse(null);
	}

	private static List<CalculationPerson> toCalculationPersons(final List<PersonBasedCalculationPersonDTO> persons, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(persons).orElse(emptyList()).stream()
			.map(person -> toCalculationPerson(person, partyIdsByPersonNumber))
			.toList();
	}

	public static List<String> collectPersonNumbers(final ApiPaginationCompositePersonBasedCalculationDTO composite) {
		return Optional.ofNullable(composite)
			.map(ApiPaginationCompositePersonBasedCalculationDTO::getResult)
			.orElseGet(List::of).stream()
			.map(PersonBasedCalculationDTO::getCalculationPersonDTOs)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.map(PersonBasedCalculationPersonDTO::getPersonId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}

	/** An unresolvable person keeps a null party id rather than being dropped, which would shrink the household. */
	private static CalculationPerson toCalculationPerson(final PersonBasedCalculationPersonDTO person, final Map<String, String> partyIdsByPersonNumber) {
		return CalculationPerson.create()
			.withPartyId(toPartyId(person.getPersonId(), partyIdsByPersonNumber))
			.withName(person.getName())
			.withAmount(toBigDecimal(person.getAmount()))
			.withDeviationFromDate(toLocalDate(person.getDeviationFromDate()))
			.withDeviationToDate(toLocalDate(person.getDeviationToDate()));
	}

	private static List<CalculationIncome> toCalculationIncomes(final List<CommonCalculationIncomeDTO> incomes) {
		return Optional.ofNullable(incomes).orElse(emptyList()).stream()
			.map(CalculationMapper::toCalculationIncome)
			.toList();
	}

	private static CalculationIncome toCalculationIncome(final CommonCalculationIncomeDTO income) {
		return CalculationIncome.create()
			.withType(income.getType())
			.withAmountApplicant(toBigDecimal(income.getAmountApplicant()))
			.withApplicantSearchDate(toLocalDate(income.getApplicantSearchDate()))
			.withAmountCoApplicant(toBigDecimal(income.getAmountCoApplicant()))
			.withCoApplicantSearchDate(toLocalDate(income.getCoApplicantSearchDate()));
	}

	private static List<CalculationExpense> toCalculationExpenses(final List<CommonCalculationExpenseDTO> expenses) {
		return Optional.ofNullable(expenses).orElse(emptyList()).stream()
			.map(CalculationMapper::toCalculationExpense)
			.toList();
	}

	private static CalculationExpense toCalculationExpense(final CommonCalculationExpenseDTO expense) {
		return CalculationExpense.create()
			.withType(expense.getType())
			.withAppliedAmount(toBigDecimal(expense.getAppliedAmount()))
			.withApprovedAmount(toBigDecimal(expense.getApprovedAmount()));
	}

	private static List<CalculationExpense> toCalculationSpecialExpenses(final List<CommonCalculationSpecialExpenseDTO> specialExpenses) {
		return Optional.ofNullable(specialExpenses).orElse(emptyList()).stream()
			.map(CalculationMapper::toCalculationExpense)
			.toList();
	}

	private static CalculationExpense toCalculationExpense(final CommonCalculationSpecialExpenseDTO specialExpense) {
		return CalculationExpense.create()
			.withType(specialExpense.getType())
			.withAppliedAmount(toBigDecimal(specialExpense.getAppliedAmount()))
			.withApprovedAmount(toBigDecimal(specialExpense.getApprovedAmount()));
	}
}
