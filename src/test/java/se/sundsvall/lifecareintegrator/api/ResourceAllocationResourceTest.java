package se.sundsvall.lifecareintegrator.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedResourceAllocationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PeriodParameters;
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
class ResourceAllocationResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PATH = "/{municipalityId}/resource-allocations";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getResourceAllocations() {
		// Mock
		final var response = PagedResourceAllocationResponse.create()
			.withResourceAllocations(List.of())
			.withMetaData(PagingMetaData.create().withPage(1).withLimit(20).withCount(0).withTotalPages(0).withTotalRecords(0));
		when(familyCareServiceMock.getResourceAllocations(eq(MUNICIPALITY_ID), any(PeriodParameters.class))).thenReturn(response);

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
			.expectBody(PagedResourceAllocationResponse.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getResourceAllocations(eq(MUNICIPALITY_ID), any(PeriodParameters.class));
	}
}
