package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Optional;
import se.sundsvall.dept44.problem.Problem;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/** The date window for the FC period reads. FC requires both dates, so a bound left open is sent as an outer date. */
record DateWindow(LocalDate start, LocalDate end) {

	static final String INVALID_DATE_WINDOW = "'from' must be on or before 'to'";

	private static final LocalDate EARLIEST = LocalDate.of(1900, JANUARY, 1);
	private static final LocalDate LATEST = LocalDate.of(2100, DECEMBER, 31);

	static DateWindow of(final LocalDate from, final LocalDate to) {
		final var start = Optional.ofNullable(from).orElse(EARLIEST);
		final var end = Optional.ofNullable(to).orElse(LATEST);
		if (start.isAfter(end)) {
			throw Problem.valueOf(BAD_REQUEST, INVALID_DATE_WINDOW);
		}
		return new DateWindow(start, end);
	}
}
