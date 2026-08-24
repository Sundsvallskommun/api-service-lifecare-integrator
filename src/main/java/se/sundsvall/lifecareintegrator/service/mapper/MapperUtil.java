package se.sundsvall.lifecareintegrator.service.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static java.util.Collections.emptyList;

/**
 * Shared mapping helpers for the vendor models: the FC API represents dates as strings (sometimes with a time part),
 * the EC API as LocalDateTime — EC sends its date-times without a zone offset ({@code "2024-02-07T00:00:00"}), so the
 * EC models are generated with {@code dateLibrary=java8-localdatetime}. FC request models still take OffsetDateTime,
 * which is what {@link #toOffsetDateTime(LocalDate)} is for.
 */
final class MapperUtil {

	private static final int ISO_DATE_LENGTH = 10;

	/**
	 * The EC date part meaning "not set" — a decision that has not been scheduled carries year 1 rather than null.
	 */
	private static final LocalDate NOT_SET = LocalDate.of(1, 1, 1);

	/**
	 * The EC date part meaning "no end date" — {@code 9999-12-31T23:59:59.9999999} is how EC spells "gäller tills
	 * vidare".
	 */
	private static final LocalDate NO_END_DATE = LocalDate.of(9999, 12, 31);

	private MapperUtil() {}

	static <S, T> List<T> mapList(final List<S> source, final Function<S, T> mapper) {
		return Optional.ofNullable(source)
			.map(list -> list.stream()
				.map(mapper)
				.toList())
			.orElse(emptyList());
	}

	/**
	 * The date part of an EC date-time, with both of EC's sentinels resolved to {@code null}.
	 *
	 * <p>
	 * Neither sentinel is a date: {@code 0001-01-01} means the value was never set and {@code 9999-12-31} means there is
	 * no end. Passed through, they reach the citizen as year 1 and year 9999. Absent is what both of them mean, and it is
	 * also what the decision window filter already treats as unbounded.
	 */
	static LocalDate toLocalDate(final LocalDateTime dateTime) {
		return Optional.ofNullable(dateTime)
			.map(LocalDateTime::toLocalDate)
			.filter(date -> !NOT_SET.equals(date))
			.filter(date -> !NO_END_DATE.equals(date))
			.orElse(null);
	}

	/**
	 * Lenient parse of the FC date strings (plain dates or date-times) — returns null for blank or unparseable values.
	 */
	static LocalDate toLocalDate(final String date) {
		return Optional.ofNullable(date)
			.filter(value -> value.length() >= ISO_DATE_LENGTH)
			.map(MapperUtil::parseLocalDate)
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

	/**
	 * Builds the dept44 {@link PagingMetaData} from an FC pagination composite's fields. {@code count} is the number of
	 * items on the current page; null vendor values default to 0.
	 */
	static PagingMetaData toPagingMetaData(final Integer page, final Integer pageSize, final Integer totalPages, final Integer totalRecords, final int count) {
		return PagingMetaData.create()
			.withPage(toInt(page))
			.withLimit(toInt(pageSize))
			.withCount(count)
			.withTotalPages(toInt(totalPages))
			.withTotalRecords(toLong(totalRecords));
	}

	/**
	 * Converts a vendor monetary {@link Double} to {@link BigDecimal} (null-safe) for the public read models.
	 */
	static BigDecimal toBigDecimal(final Double value) {
		return Optional.ofNullable(value)
			.map(BigDecimal::valueOf)
			.orElse(null);
	}

	/**
	 * Converts a public monetary {@link BigDecimal} back to the vendor {@link Double} (null-safe) for the write-back
	 * request bodies.
	 */
	static Double toDouble(final BigDecimal value) {
		return Optional.ofNullable(value)
			.map(BigDecimal::doubleValue)
			.orElse(null);
	}

	private static int toInt(final Integer value) {
		return Optional.ofNullable(value).orElse(0);
	}

	private static long toLong(final Integer value) {
		return Optional.ofNullable(value).map(Integer::longValue).orElse(0L);
	}

	private static LocalDate parseLocalDate(final String value) {
		try {
			return LocalDate.parse(value.substring(0, ISO_DATE_LENGTH));
		} catch (final DateTimeParseException e) {
			return null;
		}
	}

}
