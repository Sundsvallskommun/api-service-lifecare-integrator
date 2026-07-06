package se.sundsvall.lifecareintegrator.api;

import java.time.LocalDate;
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
import se.sundsvall.lifecareintegrator.api.model.common.CreatedResource;
import se.sundsvall.lifecareintegrator.api.model.common.PagedResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.Calculation;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class CalculationResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PATH = "/{municipalityId}/calculations";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getCalculations() {
		// Mock
		final var response = PagedResponse.<Calculation>create().withResults(List.of());
		when(familyCareServiceMock.getCalculations(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<Calculation>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getCalculations(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}

	@Test
	void getCalculationProposal() {
		// Mock
		final var proposal = CalculationProposal.create();
		when(familyCareServiceMock.getCalculationProposal(MUNICIPALITY_ID, PARTY_ID)).thenReturn(proposal);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/proposal").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CalculationProposal.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(proposal);
		verify(familyCareServiceMock).getCalculationProposal(MUNICIPALITY_ID, PARTY_ID);
	}

	@Test
	void createCalculation() {
		// Parameter values
		final var request = CreateCalculationRequest.create()
			.withPartyId(PARTY_ID)
			.withNormId(1)
			.withCalculationDate(LocalDate.parse("2026-05-01"))
			.withCalculationFromDate(LocalDate.parse("2026-05-01"))
			.withCalculationToDate(LocalDate.parse("2026-05-31"));

		// Mock
		when(familyCareServiceMock.createCalculation(eq(MUNICIPALITY_ID), any(CreateCalculationRequest.class))).thenReturn(12345);

		// Call
		final var result = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/calculations/12345")
			.expectBody(CreatedResource.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(12345);
		verify(familyCareServiceMock).createCalculation(eq(MUNICIPALITY_ID), any(CreateCalculationRequest.class));
	}
}
