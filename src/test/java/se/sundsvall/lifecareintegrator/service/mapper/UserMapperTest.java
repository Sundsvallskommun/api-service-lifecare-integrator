package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.User;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

	private static final OffsetDateTime VALID_FROM = OffsetDateTime.parse("2026-01-01T00:00:00Z");
	private static final OffsetDateTime VALID_TO = OffsetDateTime.parse("2026-12-31T00:00:00Z");

	@Test
	void toCaseworkersWithNull() {
		assertThat(UserMapper.toCaseworkers(null)).isEmpty();
	}

	@Test
	void toCaseworkers() {
		// Arrange: personId, vrkId, password and logonTypes must never survive — the model has no such fields
		final var source = new User()
			.id("user-1")
			.personId("199001011234")
			.hsaId("SE1234")
			.networkUserId("net-1")
			.vrkId("vrk-1")
			.firstName("Anna")
			.lastName("Andersson")
			.fullName("Anna Andersson")
			.description("Handläggare")
			.validFrom(VALID_FROM)
			.validTo(VALID_TO)
			.disabled(false)
			.password("secret");

		// Act
		final var result = UserMapper.toCaseworkers(List.of(source));

		// Assert
		assertThat(result).hasSize(1);
		final var user = result.getFirst();
		assertThat(user.getId()).isEqualTo("user-1");
		assertThat(user.getHsaId()).isEqualTo("SE1234");
		assertThat(user.getNetworkUserId()).isEqualTo("net-1");
		assertThat(user.getFirstName()).isEqualTo("Anna");
		assertThat(user.getLastName()).isEqualTo("Andersson");
		assertThat(user.getFullName()).isEqualTo("Anna Andersson");
		assertThat(user.getDescription()).isEqualTo("Handläggare");
		assertThat(user.getValidFrom()).isEqualTo(VALID_FROM);
		assertThat(user.getValidTo()).isEqualTo(VALID_TO);
		assertThat(user.getDisabled()).isFalse();
	}
}
