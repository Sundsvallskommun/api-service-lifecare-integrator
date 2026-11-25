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

	static final String TO_BEFORE_EARLIEST = "'to' must be on or after %s when 'from' is left out".formatted(EARLIEST);
	static final String FROM_AFTER_LATEST = "'from' must be on or before %s when 'to' is left out".formatted(LATEST);

	static DateWindow of(final LocalDate from, final LocalDate to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw Problem.valueOf(BAD_REQUEST, INVALID_DATE_WINDOW);
		}
		if (from == null && to != null && to.isBefore(EARLIEST)) {
			throw Problem.valueOf(BAD_REQUEST, TO_BEFORE_EARLIEST);
		}
		if (to == null && from != null && from.isAfter(LATEST)) {
			throw Problem.valueOf(BAD_REQUEST, FROM_AFTER_LATEST);
		}
		return new DateWindow(Optional.ofNullable(from).orElse(EARLIEST), Optional.ofNullable(to).orElse(LATEST));
	}
}
