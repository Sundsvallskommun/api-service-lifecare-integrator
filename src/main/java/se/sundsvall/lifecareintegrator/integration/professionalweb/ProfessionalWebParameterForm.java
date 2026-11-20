package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The parameter questions some Lifecare print templates ask before they render, such as "Visa medsökande" on a
 * decision with a co-applicant. The page is a form, {@code form#myForm}, a browser posts back to the same address; its
 * field names differ per template, so they are read off the page. The fields are taken as a browser submits them with
 * every question left at Lifecare's default: in document order, a checkbox only when checked, and the token field as
 * the page gives it.
 */
public final class ProfessionalWebParameterForm {

	private static final String FORM_ID = "myForm";
	private static final String TOKEN_FIELD = "X-LEGACY-TOKEN";
	private static final String VALUE = "value";
	private static final List<String> IGNORED_TYPES = List.of("submit", "button", "reset", "image", "file");
	private static final List<String> CHECKABLE_TYPES = List.of("checkbox", "radio");

	private ProfessionalWebParameterForm() {}

	public static boolean isParameterQuestions(final byte[] page) {
		if (page == null) {
			return false;
		}
		final var html = new String(page, UTF_8);
		return html.contains("id=\"" + FORM_ID + "\"") && html.contains("name=\"" + TOKEN_FIELD + "\"");
	}

	public static Optional<List<Map.Entry<String, String>>> readFields(final byte[] page) {
		if (!isParameterQuestions(page)) {
			return Optional.empty();
		}
		return Optional.ofNullable(Jsoup.parse(new String(page, UTF_8)).getElementById(FORM_ID))
			.map(ProfessionalWebParameterForm::readFields);
	}

	private static List<Map.Entry<String, String>> readFields(final Element form) {
		final var fields = new ArrayList<Map.Entry<String, String>>();
		for (final var element : form.select("input[name], select[name], textarea[name]")) {
			toSubmittedValue(element).ifPresent(value -> fields.add(Map.entry(element.attr("name"), value)));
		}
		return fields;
	}

	private static Optional<String> toSubmittedValue(final Element element) {
		if (!StringUtils.hasText(element.attr("name")) || element.hasAttr("disabled")) {
			return Optional.empty();
		}
		if ("select".equals(element.tagName())) {
			return Optional.ofNullable(element.selectFirst("option[selected]"))
				.or(() -> Optional.ofNullable(element.selectFirst("option")))
				.map(ProfessionalWebParameterForm::toOptionValue);
		}
		if ("textarea".equals(element.tagName())) {
			return Optional.of(element.wholeText());
		}
		final var type = element.attr("type").toLowerCase(Locale.ROOT);
		if (IGNORED_TYPES.contains(type)) {
			return Optional.empty();
		}
		if (CHECKABLE_TYPES.contains(type)) {
			return Optional.of(element).filter(checkable -> checkable.hasAttr("checked")).map(ProfessionalWebParameterForm::toCheckedValue);
		}
		return Optional.of(element.attr(VALUE));
	}

	private static String toOptionValue(final Element option) {
		if (option.hasAttr(VALUE)) {
			return option.attr(VALUE);
		}
		return option.text();
	}

	private static String toCheckedValue(final Element checkable) {
		if (checkable.hasAttr(VALUE)) {
			return checkable.attr(VALUE);
		}
		return "on";
	}
}
