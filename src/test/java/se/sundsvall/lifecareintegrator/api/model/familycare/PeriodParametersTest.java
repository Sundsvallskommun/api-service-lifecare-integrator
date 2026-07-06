package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodParametersTest {

	@Test
	void testGettersAndSetters() {
		// Arrange
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";
		final var from = LocalDate.of(2025, 1, 1);
		final var to = LocalDate.of(2026, 12, 31);
		final var ascending = true;
		final var page = 2;
		final var limit = 20;

		// Act
		final var parameters = new PeriodParameters();
		parameters.setPartyId(partyId);
		parameters.setFrom(from);
		parameters.setTo(to);
		parameters.setAscending(ascending);
		parameters.setPage(page);
		parameters.setLimit(limit);

		// Assert
		assertThat(parameters.getPartyId()).isEqualTo(partyId);
		assertThat(parameters.getFrom()).isEqualTo(from);
		assertThat(parameters.getTo()).isEqualTo(to);
		assertThat(parameters.getAscending()).isEqualTo(ascending);
		assertThat(parameters.getPage()).isEqualTo(page);
		assertThat(parameters.getLimit()).isEqualTo(limit);
	}

	@Test
	void testDefaults() {
		final var parameters = new PeriodParameters();

		assertThat(parameters.getPage()).isEqualTo(1);
		assertThat(parameters.getLimit()).isEqualTo(100);
		assertThat(parameters.getPartyId()).isNull();
		assertThat(parameters.getFrom()).isNull();
		assertThat(parameters.getTo()).isNull();
		assertThat(parameters.getAscending()).isNull();
	}

	@Test
	void testEqualsAndHashCode() {
		final var one = new PeriodParameters();
		one.setPartyId("id");
		final var two = new PeriodParameters();
		two.setPartyId("id");

		assertThat(one).isEqualTo(two).hasSameHashCodeAs(two);
	}
}
