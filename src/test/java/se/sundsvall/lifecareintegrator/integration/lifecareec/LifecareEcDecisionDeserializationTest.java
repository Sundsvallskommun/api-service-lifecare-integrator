package se.sundsvall.lifecareintegrator.integration.lifecareec;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

	@Autowired
	private JsonMapper jsonMapper;

	private List<WEECIntegrationContractsDecisionV1LssDecision> realResponse() throws Exception {
		final var json = new ClassPathResource("lifecare-ec/lss-decisions-response.json").getContentAsString(UTF_8);
		return jsonMapper.readValue(json, new TypeReference<>() {});
	}

	@Test
	void zoneLessDateTimesAreRead() throws Exception {
		final var decisions = realResponse();

		assertThat(decisions).hasSize(3);
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
		final var notScheduled = realResponseQuietly().getLast();

		assertThat(notScheduled.getExecutionStartDate()).isEqualTo(LocalDateTime.parse("0001-01-01T00:00:00"));
		assertThat(notScheduled.getExecutionEndDate()).isEqualTo(LocalDateTime.parse("0001-01-01T00:00:00"));
	}

	@Test
	void theWholeResponseMapsToTheApiModel() {
		final var decisions = realResponseQuietly().stream()
			.map(DecisionMapper::toDecision)
			.toList();

		assertThat(decisions).extracting("decisionId", "law", "source", "type", "validFrom", "validTo")
			.containsExactly(
				org.assertj.core.api.Assertions.tuple("167961", "LSS", "ELDERLY_CARE", "Beslut att verkställa",
					LocalDate.of(2024, 2, 7), LocalDate.of(9999, 12, 31)),
				org.assertj.core.api.Assertions.tuple("167990", "LSS", "ELDERLY_CARE", "Beslut att verkställa",
					LocalDate.of(2024, 4, 9), LocalDate.of(9999, 12, 31)),
				org.assertj.core.api.Assertions.tuple("168156", "LSS", "ELDERLY_CARE", "Beslut att verkställa",
					LocalDate.of(2026, 5, 26), LocalDate.of(9999, 12, 31)));

		assertThat(decisions).allSatisfy(decision -> assertThat(decision.getDecisionMaker()).isNotBlank());
	}

	private List<WEECIntegrationContractsDecisionV1LssDecision> realResponseQuietly() {
		try {
			return realResponse();
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
