package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedCalculationDTO;
import generated.se.sundsvall.lifecarefc.CommonCalculationExpenseDTO;
import generated.se.sundsvall.lifecarefc.CommonCalculationIncomeDTO;
import generated.se.sundsvall.lifecarefc.CommonCalculationSpecialExpenseDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationPersonDTO;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationExpense;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationIncome;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPerson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CalculationMapperTest {

	@Test
	void toCalculationsWithNull() {
		// Act
		final var result = CalculationMapper.toCalculations(null);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
		assertThat(result.getPage()).isNull();
		assertThat(result.getPageSize()).isNull();
		assertThat(result.getTotalPages()).isNull();
		assertThat(result.getTotalRecords()).isNull();
	}

	@Test
	void toCalculationWithNull() {
		assertThat(CalculationMapper.toCalculation(null)).isNull();
	}

	@Test
	void toCalculation() {
		// Arrange: person personId must NOT survive the mapping — the model has no such field
		final var source = new PersonBasedCalculationDTO()
			.id(1)
			.norm("Riksnorm 2026")
			.fromDate("2026-05-01")
			.toDate("2026-05-31")
			.incomeSum(10000.0)
			.expenseSum(8000.0)
			.specialExpenseSum(500.0)
			.normSum(6000.0)
			.commonHouseholdCost(1500.0)
			.familyCost(2000.0)
			.balance(-3000.0)
			.totalSum(11500.0)
			.investigationId(2)
			.serviceId(3)
			._final(true)
			.connectedApplication(4)
			.calculationPersonDTOs(List.of(new PersonBasedCalculationPersonDTO()
				.personId("199001011234")
				.name("Kalle Karlsson")
				.amount(4000.0)
				.deviationFromDate("2026-05-01")
				.deviationToDate("2026-05-31")))
			.calculationIncomesDTOs(List.of(new CommonCalculationIncomeDTO()
				.type("Lön")
				.amountApplicant(9000.0)
				.applicantSearchDate("2026-05-02")
				.amountCoApplicant(1000.0)
				.coApplicantSearchDate("2026-05-03")))
			.calculationExpensesDTOs(List.of(new CommonCalculationExpenseDTO()
				.type("Hyra")
				.appliedAmount(7000.0)
				.approvedAmount(6500.0)))
			.calculationSpecialExpensesDTOs(List.of(new CommonCalculationSpecialExpenseDTO()
				.type("Tandvård")
				.appliedAmount(500.0)
				.approvedAmount(450.0)));

		// Act
		final var result = CalculationMapper.toCalculation(source);

		// Assert
		assertThat(result.getId()).isEqualTo(1);
		assertThat(result.getNorm()).isEqualTo("Riksnorm 2026");
		assertThat(result.getFromDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(result.getToDate()).isEqualTo(LocalDate.parse("2026-05-31"));
		assertThat(result.getIncomeSum()).isEqualTo(10000.0);
		assertThat(result.getExpenseSum()).isEqualTo(8000.0);
		assertThat(result.getSpecialExpenseSum()).isEqualTo(500.0);
		assertThat(result.getNormSum()).isEqualTo(6000.0);
		assertThat(result.getCommonHouseholdCost()).isEqualTo(1500.0);
		assertThat(result.getFamilyCost()).isEqualTo(2000.0);
		assertThat(result.getBalance()).isEqualTo(-3000.0);
		assertThat(result.getTotalSum()).isEqualTo(11500.0);
		assertThat(result.getInvestigationId()).isEqualTo(2);
		assertThat(result.getServiceId()).isEqualTo(3);
		assertThat(result.getFinalCalculation()).isTrue();
		assertThat(result.getConnectedApplication()).isEqualTo(4);

		// The person id (personnummer) must never survive the mapping
		assertThat(result.getPersons())
			.extracting(CalculationPerson::getName, CalculationPerson::getAmount, CalculationPerson::getDeviationFromDate, CalculationPerson::getDeviationToDate)
			.containsExactly(tuple("Kalle Karlsson", 4000.0, LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-31")));

		assertThat(result.getIncomes())
			.extracting(CalculationIncome::getType, CalculationIncome::getAmountApplicant, CalculationIncome::getApplicantSearchDate,
				CalculationIncome::getAmountCoApplicant, CalculationIncome::getCoApplicantSearchDate)
			.containsExactly(tuple("Lön", 9000.0, LocalDate.parse("2026-05-02"), 1000.0, LocalDate.parse("2026-05-03")));

		assertThat(result.getExpenses())
			.extracting(CalculationExpense::getType, CalculationExpense::getAppliedAmount, CalculationExpense::getApprovedAmount)
			.containsExactly(tuple("Hyra", 7000.0, 6500.0));

		assertThat(result.getSpecialExpenses())
			.extracting(CalculationExpense::getType, CalculationExpense::getAppliedAmount, CalculationExpense::getApprovedAmount)
			.containsExactly(tuple("Tandvård", 500.0, 450.0));
	}

	@Test
	void toCalculationWithUnparseableDate() {
		// Act
		final var result = CalculationMapper.toCalculation(new PersonBasedCalculationDTO().fromDate("garbage-date").toDate("2026"));

		// Assert
		assertThat(result.getFromDate()).isNull();
		assertThat(result.getToDate()).isNull();
	}

	@Test
	void toCalculationsWithPagination() {
		// Arrange
		final var composite = new ApiPaginationCompositePersonBasedCalculationDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(3)
			.totalNumberOfRecords(25)
			.result(List.of(new PersonBasedCalculationDTO().id(1).norm("Riksnorm 2026")));

		// Act
		final var result = CalculationMapper.toCalculations(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(10);
		assertThat(result.getTotalPages()).isEqualTo(3);
		assertThat(result.getTotalRecords()).isEqualTo(25);
		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().getFirst().getId()).isEqualTo(1);
		assertThat(result.getResults().getFirst().getNorm()).isEqualTo("Riksnorm 2026");
	}
}
