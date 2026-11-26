package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class DateWindowTest {

	private static final LocalDate EARLIEST = LocalDate.parse("1900-01-01");
	private static final LocalDate LATEST = LocalDate.parse("2100-12-31");

	@Test
	void ofWithoutBoundsReadsEverything() {
		assertThat(DateWindow.of(null, null)).isEqualTo(new DateWindow(EARLIEST, LATEST));
	}

	@Test
	void ofWithOnlyToReadsEverythingBefore() {
		final var to = LocalDate.parse("2010-01-01");

		assertThat(DateWindow.of(null, to)).isEqualTo(new DateWindow(EARLIEST, to));
	}

	@Test
	void ofWithOnlyFromReadsEverythingAfter() {
		final var from = LocalDate.parse("2030-01-01");

		assertThat(DateWindow.of(from, null)).isEqualTo(new DateWindow(from, LATEST));
	}

	@Test
	void ofWithBothBoundsReadsTheSpan() {
		final var from = LocalDate.parse("2025-01-01");
		final var to = LocalDate.parse("2026-12-31");

		assertThat(DateWindow.of(from, to)).isEqualTo(new DateWindow(from, to));
	}

	@Test
	void ofWithTheSameDayOnBothBounds() {
		final var day = LocalDate.parse("2026-06-30");

		assertThat(DateWindow.of(day, day)).isEqualTo(new DateWindow(day, day));
	}

	@Test
	void ofWithFromAfterToIsRefused() {
		final var exception = assertThrows(ThrowableProblem.class,
			() -> DateWindow.of(LocalDate.parse("2026-06-30"), LocalDate.parse("2026-01-01")));

		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("'from' must be on or before 'to'");
	}

	@Test
	void ofWithOnlyToBeforeTheEarliestDateIsRefused() {
		final var exception = assertThrows(ThrowableProblem.class, () -> DateWindow.of(null, LocalDate.parse("1800-01-01")));

		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("'to' must be on or after 1900-01-01 when 'from' is left out");
	}

	@Test
	void ofWithOnlyFromAfterTheLatestDateIsRefused() {
		final var exception = assertThrows(ThrowableProblem.class, () -> DateWindow.of(LocalDate.parse("2101-01-01"), null));

		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getDetail()).isEqualTo("'from' must be on or before 2100-12-31 when 'to' is left out");
	}
}
