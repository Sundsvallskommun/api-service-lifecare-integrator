package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.common.CreatedResource;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedActualisationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PeriodParameters;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ActualisationResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PATH = "/{municipalityId}/actualisations";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getActualisations() {
		// Mock
		final var response = PagedActualisationResponse.create()
			.withActualisations(List.of())
			.withMetaData(PagingMetaData.create().withPage(1).withLimit(20).withCount(0).withTotalPages(0).withTotalRecords(0));
		when(familyCareServiceMock.getActualisations(eq(MUNICIPALITY_ID), any(PeriodParameters.class))).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH)
				.queryParam("partyId", PARTY_ID)
				.queryParam("from", "2025-01-01")
				.queryParam("to", "2026-12-31")
				.queryParam("page", 1)
				.queryParam("limit", 20)
				.queryParam("ascending", true)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(PagedActualisationResponse.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getActualisations(eq(MUNICIPALITY_ID), any(PeriodParameters.class));
	}

	@Test
	void getActualisationProposal() {
		// Mock
		final var proposal = ActualisationProposal.create();
		when(familyCareServiceMock.getActualisationProposal(MUNICIPALITY_ID, PARTY_ID)).thenReturn(proposal);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/proposal").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(ActualisationProposal.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(proposal);
		verify(familyCareServiceMock).getActualisationProposal(MUNICIPALITY_ID, PARTY_ID);
	}

	@Test
	void createActualisation() {
		// Parameter values
		final var request = CreateActualisationRequest.create()
			.withPartyId(PARTY_ID)
			.withDate(LocalDate.parse("2026-05-01"))
			.withTypeId(1);

		// Mock
		when(familyCareServiceMock.createActualisation(eq(MUNICIPALITY_ID), any(CreateActualisationRequest.class))).thenReturn(12345);

		// Call
		final var result = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/actualisations/12345")
			.expectBody(CreatedResource.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(12345);
		verify(familyCareServiceMock).createActualisation(eq(MUNICIPALITY_ID), any(CreateActualisationRequest.class));
	}

	@Test
	void addActualisationAttachment() {
		// Parameter values
		final var actualisationId = 12345;

		final var builder = new MultipartBodyBuilder();
		builder.part("file", "%PDF-1.4".getBytes()).filename("document.pdf");
		builder.part("documentType", "DOC_TYPE");
		builder.part("senderType", "SENDER_TYPE");

		// Call
		webTestClient.post()
			.uri(builder2 -> builder2.path(PATH + "/{actualisationId}/attachments").build(Map.of("municipalityId", MUNICIPALITY_ID, "actualisationId", actualisationId)))
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(builder.build())
			.exchange()
			.expectStatus().isNoContent();

		// Verification
		verify(familyCareServiceMock).addActualisationAttachment(eq(actualisationId), eq("DOC_TYPE"), eq("SENDER_TYPE"), eq(null), eq(null), any(MultipartFile.class));
	}
}
