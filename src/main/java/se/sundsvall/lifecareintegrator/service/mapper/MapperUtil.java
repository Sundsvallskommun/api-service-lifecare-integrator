package se.sundsvall.lifecareintegrator.service.mapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Shared mapping helpers for the vendor models: the FC API represents dates as strings (sometimes with a time part),
 * the EC API as OffsetDateTime.
 */
final class MapperUtil {

	private static final int ISO_DATE_LENGTH = 10;

	private MapperUtil() {}

	static LocalDate toLocalDate(final OffsetDateTime dateTime) {
		return Optional.ofNullable(dateTime)
			.map(OffsetDateTime::toLocalDate)
			.orElse(null);
	}

	/**
	 * Lenient parse of the FC date strings (plain dates or date-times) — returns null for blank or unparseable values.
	 */
	static LocalDate toLocalDate(final String date) {
		return Optional.ofNullable(date)
			.filter(value -> value.length() >= ISO_DATE_LENGTH)
			.map(value -> {
				try {
					return LocalDate.parse(value.substring(0, ISO_DATE_LENGTH));
				} catch (final DateTimeParseException e) {
					return null;
				}
			})
			.orElse(null);
	}

	static String toStringValue(final Integer value) {
		return Optional.ofNullable(value)
			.map(String::valueOf)
			.orElse(null);
	}

	static String toDateString(final LocalDate date) {
		return Optional.ofNullable(date)
			.map(LocalDate::toString)
			.orElse(null);
	}

	static OffsetDateTime toOffsetDateTime(final LocalDate date) {
		return Optional.ofNullable(date)
			.map(LocalDate::atStartOfDay)
			.map(dateTime -> dateTime.atOffset(ZoneOffset.UTC))
			.orElse(null);
	}
}
