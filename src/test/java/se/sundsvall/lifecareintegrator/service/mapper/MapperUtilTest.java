package se.sundsvall.lifecareintegrator.service.mapper;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPartyId;

class MapperUtilTest {

	@Test
	void toPartyIdLooksUpTheGivenPersonNumber() {
		final var partyIdsByPersonNumber = Map.of("199001011234", "party-1");

		assertThat(toPartyId("199001011234", partyIdsByPersonNumber)).isEqualTo("party-1");
		assertThat(toPartyId("201001011234", partyIdsByPersonNumber)).isNull();
	}

	@Test
	void toPartyIdWithoutPersonNumber() {
		// Map.of rejects a null key, so this also proves the person number is checked before the lookup
		assertThat(toPartyId(null, Map.of("199001011234", "party-1"))).isNull();
	}
}
