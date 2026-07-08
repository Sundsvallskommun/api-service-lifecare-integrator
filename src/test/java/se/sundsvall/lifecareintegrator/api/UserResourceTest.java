package se.sundsvall.lifecareintegrator.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.familycare.Caseworker;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class UserResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/{municipalityId}/users";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getUsers() {
		// Mock
		final var users = List.of(Caseworker.create());
		when(familyCareServiceMock.getUsers(100, null, null, null)).thenReturn(users);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("limit", 100).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<List<Caseworker>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(users);
		verify(familyCareServiceMock).getUsers(100, null, null, null);
	}

	@Test
	void getUsersWithAllParameters() {
		// Parameter values
		final var offset = 10;
		final var modifiedAfter = OffsetDateTime.parse("2026-01-01T00:00:00Z");
		final var modifiedBefore = OffsetDateTime.parse("2026-06-30T00:00:00Z");

		// Mock
		final var users = List.of(Caseworker.create());
		when(familyCareServiceMock.getUsers(100, offset, modifiedAfter, modifiedBefore)).thenReturn(users);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("limit", 100)
				.queryParam("offset", offset)
				.queryParam("modifiedAfter", modifiedAfter.toString())
				.queryParam("modifiedBefore", modifiedBefore.toString())
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<List<Caseworker>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(users);
		verify(familyCareServiceMock).getUsers(100, offset, modifiedAfter, modifiedBefore);
	}
}
