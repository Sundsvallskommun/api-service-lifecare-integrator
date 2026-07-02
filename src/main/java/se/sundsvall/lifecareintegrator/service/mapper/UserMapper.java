package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.User;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.CaseworkerUser;

import static java.util.Collections.emptyList;

public final class UserMapper {

	private UserMapper() {}

	public static List<CaseworkerUser> toCaseworkerUsers(final List<User> users) {
		return Optional.ofNullable(users)
			.map(list -> list.stream()
				.map(UserMapper::toCaseworkerUser)
				.toList())
			.orElse(emptyList());
	}

	private static CaseworkerUser toCaseworkerUser(final User user) {
		// Intentionally drops personId, vrkId, password and logonTypes — personnummer and credentials never leave this service
		return CaseworkerUser.create()
			.withId(user.getId())
			.withHsaId(user.getHsaId())
			.withNetworkUserId(user.getNetworkUserId())
			.withFirstName(user.getFirstName())
			.withLastName(user.getLastName())
			.withFullName(user.getFullName())
			.withDescription(user.getDescription())
			.withValidFrom(user.getValidFrom())
			.withValidTo(user.getValidTo())
			.withDisabled(user.getDisabled());
	}
}
