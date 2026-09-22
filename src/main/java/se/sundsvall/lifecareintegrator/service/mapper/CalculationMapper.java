package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedCalculationDTO;
import generated.se.sundsvall.lifecarefc.CommonCalculationExpenseDTO;
import generated.se.sundsvall.lifecarefc.CommonCalculationIncomeDTO;
import generated.se.sundsvall.lifecarefc.CommonCalculationSpecialExpenseDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationPersonDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import se.sundsvall.lifecareintegrator.api.model.familycare.Calculation;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationExpense;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationIncome;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPerson;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedCalculationResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;

public final class CalculationMapper {

	private CalculationMapper() {}

	public static PagedCalculationResponse toCalculations(final ApiPaginationCompositePersonBasedCalculationDTO composite, final UnaryOperator<String> partyIdResolver) {
		return Optional.ofNullable(composite)
			.map(source -> toCalculationList(source.getResult(), partyIdResolver))
			.map(calculations -> PagedCalculationResponse.create()
				.withCalculations(calculations)
				.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), calculations.size())))
			.orElseGet(() -> PagedCalculationResponse.create().withCalculations(emptyList()));
	}

	public static Calculation toCalculation(final PersonBasedCalculationDTO calculation, final UnaryOperator<String> partyIdResolver) {
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
				.withPersons(toCalculationPersons(source.getCalculationPersonDTOs(), partyIdResolver))
				.withIncomes(toCalculationIncomes(source.getCalculationIncomesDTOs()))
				.withExpenses(toCalculationExpenses(source.getCalculationExpensesDTOs()))
				.withSpecialExpenses(toCalculationSpecialExpenses(source.getCalculationSpecialExpensesDTOs())))
			.orElse(null);
	}

	private static List<Calculation> toCalculationList(final List<PersonBasedCalculationDTO> calculations, final UnaryOperator<String> partyIdResolver) {
		return Optional.ofNullable(calculations)
			.map(list -> list.stream()
				.map(calculation -> toCalculation(calculation, partyIdResolver))
				.toList())
			.orElse(emptyList());
	}

	private static List<CalculationPerson> toCalculationPersons(final List<PersonBasedCalculationPersonDTO> persons, final UnaryOperator<String> partyIdResolver) {
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(person -> toCalculationPerson(person, partyIdResolver))
				.toList())
			.orElse(null);
	}

	/**
	 * Every personnummer appearing on the persons of these calculations, so the caller can resolve them in one batch
	 * rather than one lookup per person.
	 */
	public static List<String> personNumbersOf(final ApiPaginationCompositePersonBasedCalculationDTO composite) {
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

	/**
	 * The personnummer Lifecare carries on a calculation person is replaced by the party id it resolves to — the
	 * personnummer itself never leaves this service. A person the party service cannot resolve keeps a null party id;
	 * the name is still useful to a consumer, and dropping the row would silently shrink a household instead.
	 */
	private static CalculationPerson toCalculationPerson(final PersonBasedCalculationPersonDTO person, final UnaryOperator<String> partyIdResolver) {
		return CalculationPerson.create()
			.withPartyId(partyIdResolver.apply(person.getPersonId()))
			.withName(person.getName())
			.withAmount(toBigDecimal(person.getAmount()))
			.withDeviationFromDate(toLocalDate(person.getDeviationFromDate()))
			.withDeviationToDate(toLocalDate(person.getDeviationToDate()));
	}

	private static List<CalculationIncome> toCalculationIncomes(final List<CommonCalculationIncomeDTO> incomes) {
		return Optional.ofNullable(incomes)
			.map(list -> list.stream()
				.map(CalculationMapper::toCalculationIncome)
				.toList())
			.orElse(null);
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
		return Optional.ofNullable(expenses)
			.map(list -> list.stream()
				.map(CalculationMapper::toCalculationExpense)
				.toList())
			.orElse(null);
	}

	private static CalculationExpense toCalculationExpense(final CommonCalculationExpenseDTO expense) {
		return CalculationExpense.create()
			.withType(expense.getType())
			.withAppliedAmount(toBigDecimal(expense.getAppliedAmount()))
			.withApprovedAmount(toBigDecimal(expense.getApprovedAmount()));
	}

	private static List<CalculationExpense> toCalculationSpecialExpenses(final List<CommonCalculationSpecialExpenseDTO> specialExpenses) {
		return Optional.ofNullable(specialExpenses)
			.map(list -> list.stream()
				.map(CalculationMapper::toCalculationExpense)
				.toList())
			.orElse(null);
	}

	private static CalculationExpense toCalculationExpense(final CommonCalculationSpecialExpenseDTO specialExpense) {
		return CalculationExpense.create()
			.withType(specialExpense.getType())
			.withAppliedAmount(toBigDecimal(specialExpense.getAppliedAmount()))
			.withApprovedAmount(toBigDecimal(specialExpense.getApprovedAmount()));
	}
}
