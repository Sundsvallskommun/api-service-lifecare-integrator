package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * A resolved date window for the FC period reads. FC requires both dates, but the public API accepts an open window —
 * missing bounds fall back to a wide default (DEFAULT_LOOKBACK_YEARS back until today).
 */
record DateWindow(LocalDate start, LocalDate end) {

	static final String INVALID_DATE_WINDOW = "'from' must be on or before 'to'";
	static final int DEFAULT_LOOKBACK_YEARS = 10;

	// The default window boundaries are resolved in Sundsvall local time, independent of the server time zone.
	private static final ZoneId ZONE = ZoneId.of("Europe/Stockholm");

	static DateWindow of(final LocalDate from, final LocalDate to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw Problem.valueOf(BAD_REQUEST, INVALID_DATE_WINDOW);
		}
		return new DateWindow(
			Optional.ofNullable(from).orElseGet(() -> LocalDate.now(ZONE).minusYears(DEFAULT_LOOKBACK_YEARS)),
			Optional.ofNullable(to).orElseGet(() -> LocalDate.now(ZONE)));
	}
}
