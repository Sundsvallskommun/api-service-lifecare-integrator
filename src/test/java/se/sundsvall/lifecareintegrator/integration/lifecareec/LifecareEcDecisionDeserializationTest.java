package se.sundsvall.lifecareintegrator.integration.lifecareec;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Deserializes a real EC LSS response, captured from the test environment on 2026-08-24, with the application's own
 * Jackson configuration.
 *
 * <p>
 * EC sends date-times without a zone offset ({@code "2024-02-07T00:00:00"}), which is why the EC models are generated
 * with {@code dateLibrary=java8-localdatetime}. Generating them as {@code OffsetDateTime} instead made every LSS read
 * fail with {@code Text '2024-02-07T00:00:00' could not be parsed at index 19} — an error no hand-written fixture
 * caught, because the fixtures had been written with a {@code Z} the real API never sends. This test holds the real
 * payload so the mistake cannot come back.
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class LifecareEcDecisionDeserializationTest {

	/** No caseworker resolution — asserts what the payload alone yields. */
	private static final Function<String, Optional<String>> NO_NAMES = _ -> Optional.empty();

	@Autowired
	private JsonMapper jsonMapper;

	private List<WEECIntegrationContractsDecisionV1LssDecision> realResponse() throws Exception {
		final var json = new ClassPathResource("lifecare-ec/lss-decisions-response.json").getContentAsString(UTF_8);
		return jsonMapper.readValue(json, new TypeReference<>() {});
	}

	@Test
	void zoneLessDateTimesAreRead() throws Exception {
		final var decisions = realResponse();

		assertThat(decisions).hasSize(4);
		assertThat(decisions.getFirst().getDate()).isEqualTo(LocalDateTime.parse("2024-02-07T00:00:00"));
		assertThat(decisions.getFirst().getFromDate()).isEqualTo(LocalDateTime.parse("2024-02-07T00:00:00"));
	}

	@Test
	void theOpenEndedSentinelWithSevenFractionalDigitsIsRead() {
		// EC spells "no end date" as 9999-12-31T23:59:59.9999999 — more sub-second digits than a millisecond format
		// would accept.
		assertThat(realResponseQuietly().getFirst().getToDate())
			.isEqualTo(LocalDateTime.parse("9999-12-31T23:59:59.999999900"));
	}

	@Test
	void theUnsetDateSentinelIsRead() {
		// A decision that has not been scheduled carries year 1 rather than null.
		final var notScheduled = realResponseQuietly().get(2);

		assertThat(notScheduled.getExecutionStartDate()).isEqualTo(LocalDateTime.parse("0001-01-01T00:00:00"));
		assertThat(notScheduled.getExecutionEndDate()).isEqualTo(LocalDateTime.parse("0001-01-01T00:00:00"));
	}

	@Test
	void theWholeResponseMapsToTheApiModel() {
		final var decisions = realResponseQuietly().stream()
			.map(decision -> DecisionMapper.toDecision(decision, NO_NAMES))
			.toList();

		// The endpoint serves two laws: Law 3 is LSS, Law 7 is SFB — assistansersättning decided by Försäkringskassan,
		// which must not be presented to the citizen as a municipal LSS decision. Both sentinel dates resolve to null.
		assertThat(decisions).extracting("decisionId", "law", "decisionMaker", "validFrom", "validTo")
			.containsExactly(
				tuple("167961", "LSS", "Helsinger Lotta", LocalDate.of(2024, 2, 7), null),
				tuple("167990", "LSS", "Hofling Anneli", LocalDate.of(2024, 4, 9), null),
				tuple("168156", "LSS", "Lena LC Utbildning", LocalDate.of(2026, 5, 26), null),
				tuple("168030", "SFB", "Försäkringskassan", LocalDate.of(2024, 5, 2), null));
	}

	@Test
	void theNotSetSentinelDoesNotReachTheCitizen() {
		final var notScheduled = DecisionMapper.toDecision(realResponseQuietly().get(2), NO_NAMES);

		assertThat(notScheduled.getElderlyCareDetails().getExecutionStartDate()).isNull();
		assertThat(notScheduled.getElderlyCareDetails().getExecutionEndDate()).isNull();
	}

	@Test
	void aBlankSfbCaseworkerIsOmittedUnlessItCanBeResolved() {
		final var sfbDecision = realResponseQuietly().getLast();

		// EC sends the SFB caseworker as an id with empty name fields. Left alone that becomes an empty string.
		assertThat(DecisionMapper.toDecision(sfbDecision, NO_NAMES).getElderlyCareDetails().getSfbCaseworker()).isNull();

		// With the employee directory reachable, the id becomes a name.
		assertThat(DecisionMapper.toDecision(sfbDecision, loginName -> Optional.of(loginName).filter("LOHE"::equals).map(_ -> "Lotta Helsinger"))
			.getElderlyCareDetails().getSfbCaseworker()).isEqualTo("Lotta Helsinger");
	}

	private List<WEECIntegrationContractsDecisionV1LssDecision> realResponseQuietly() {
		try {
			return realResponse();
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
