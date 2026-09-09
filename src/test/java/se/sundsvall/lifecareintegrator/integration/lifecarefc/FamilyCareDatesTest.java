package se.sundsvall.lifecareintegrator.integration.lifecarefc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.lifecareintegrator.integration.lifecarefc.FamilyCareDates.atUtc;
import static se.sundsvall.lifecareintegrator.integration.lifecarefc.FamilyCareDates.endOfDay;
import static se.sundsvall.lifecareintegrator.integration.lifecarefc.FamilyCareDates.startOfDay;

class FamilyCareDatesTest {

	/**
	 * FamilyCare rejects a bare {@code yyyy-MM-dd} — and {@code ISO_LOCAL_DATE_TIME} would render midnight as
	 * {@code 2026-06-01T00:00}, dropping the seconds field FamilyCare requires.
	 */
	@Test
	void startOfDayIsRenderedWithSeconds() {
		assertThat(startOfDay(LocalDate.of(2026, JUNE, 1))).isEqualTo("2026-06-01T00:00:00");
	}

	@Test
	void endOfDayClosesTheWindowOnTheLastSecondOfTheDay() {
		assertThat(endOfDay(LocalDate.of(2026, JUNE, 30))).isEqualTo("2026-06-30T23:59:59");
	}

	@Test
	void atUtcNormalisesTheOffsetAndKeepsTheSeconds() {
		assertThat(atUtc(OffsetDateTime.parse("2026-06-01T02:00:00+02:00"))).isEqualTo("2026-06-01T00:00:00");
		assertThat(atUtc(OffsetDateTime.parse("2026-06-01T12:34:56Z"))).isEqualTo("2026-06-01T12:34:56");
	}

	@Test
	void nullStaysNullSoAnOptionalParameterIsSimplyOmitted() {
		assertThat(startOfDay(null)).isNull();
		assertThat(endOfDay(null)).isNull();
		assertThat(atUtc(null)).isNull();
	}
}
