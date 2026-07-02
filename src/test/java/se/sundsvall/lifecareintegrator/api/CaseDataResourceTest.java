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
import se.sundsvall.lifecareintegrator.api.model.CaseService;
import se.sundsvall.lifecareintegrator.api.model.Execution;
import se.sundsvall.lifecareintegrator.api.model.Investigation;
import se.sundsvall.lifecareintegrator.api.model.PagedResponse;
import se.sundsvall.lifecareintegrator.api.model.Payment;
import se.sundsvall.lifecareintegrator.api.model.ResourceAllocation;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class CaseDataResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getPayments() {
		// Mock
		final var response = PagedResponse.<Payment>create().withResults(List.of());
		when(familyCareServiceMock.getPayments(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/payments").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<Payment>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getPayments(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}

	@Test
	void getInvestigations() {
		// Mock
		final var response = PagedResponse.<Investigation>create().withResults(List.of());
		when(familyCareServiceMock.getInvestigations(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/investigations").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<Investigation>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getInvestigations(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}

	@Test
	void getServices() {
		// Mock
		final var response = PagedResponse.<CaseService>create().withResults(List.of());
		when(familyCareServiceMock.getServices(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/services").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<CaseService>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getServices(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}

	@Test
	void getExecutions() {
		// Mock
		final var response = PagedResponse.<Execution>create().withResults(List.of());
		when(familyCareServiceMock.getExecutions(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/executions").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<Execution>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getExecutions(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}

	@Test
	void getResourceAllocations() {
		// Mock
		final var response = PagedResponse.<ResourceAllocation>create().withResults(List.of());
		when(familyCareServiceMock.getResourceAllocations(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path("/{municipalityId}/resource-allocations").queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<ResourceAllocation>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getResourceAllocations(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}
}
