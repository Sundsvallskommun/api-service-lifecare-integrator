package se.sundsvall.lifecareintegrator.api;

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
import se.sundsvall.lifecareintegrator.api.model.Contact;
import se.sundsvall.lifecareintegrator.api.model.Person;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class PersonResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PERSON_PATH = "/{municipalityId}/person";
	private static final String CONTACTS_PATH = "/{municipalityId}/contacts";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getPerson() {
		// Mock
		final var person = Person.create();
		when(familyCareServiceMock.getPerson(MUNICIPALITY_ID, PARTY_ID)).thenReturn(person);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PERSON_PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Person.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(person);
		verify(familyCareServiceMock).getPerson(MUNICIPALITY_ID, PARTY_ID);
	}

	@Test
	void getContacts() {
		// Mock
		final var contacts = List.of(Contact.create());
		when(familyCareServiceMock.getContacts(MUNICIPALITY_ID, PARTY_ID)).thenReturn(contacts);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(CONTACTS_PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<List<Contact>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(contacts);
		verify(familyCareServiceMock).getContacts(MUNICIPALITY_ID, PARTY_ID);
	}
}
