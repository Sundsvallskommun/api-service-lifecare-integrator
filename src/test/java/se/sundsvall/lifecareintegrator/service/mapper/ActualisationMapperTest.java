package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringDTO;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActualisationMapperTest {

	@Test
	void toActualisationsWithNull() {
		// Act
		final var result = ActualisationMapper.toActualisations(null);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getResults()).isEmpty();
		assertThat(result.getPage()).isNull();
		assertThat(result.getPageSize()).isNull();
		assertThat(result.getTotalPages()).isNull();
		assertThat(result.getTotalRecords()).isNull();
	}

	@Test
	void toActualisationWithNull() {
		assertThat(ActualisationMapper.toActualisation(null)).isNull();
	}

	@Test
	void toActualisation() {
		// Arrange: personId must NOT survive the mapping — the model has no such field
		final var source = new PersonBasedAktualiseringDTO()
			.id(1)
			.type("Ansökan")
			.personId("199001011234")
			.name("Ansökan om försörjningsstöd")
			.date("2026-05-01T00:00:00")
			.reason("Försörjningsstöd")
			.regards("Ekonomi")
			.fromWho("Egen ansökan")
			.caseworker("Anna Andersson")
			.organization("Vuxen försörjningsstöd")
			.status("Pågående")
			.investigationId(2)
			.serviceId(3)
			.decisionId(4);

		// Act
		final var result = ActualisationMapper.toActualisation(source);

		// Assert
		assertThat(result.getId()).isEqualTo(1);
		assertThat(result.getType()).isEqualTo("Ansökan");
		assertThat(result.getName()).isEqualTo("Ansökan om försörjningsstöd");
		assertThat(result.getDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(result.getReason()).isEqualTo("Försörjningsstöd");
		assertThat(result.getRegards()).isEqualTo("Ekonomi");
		assertThat(result.getFromWho()).isEqualTo("Egen ansökan");
		assertThat(result.getCaseworker()).isEqualTo("Anna Andersson");
		assertThat(result.getOrganization()).isEqualTo("Vuxen försörjningsstöd");
		assertThat(result.getStatus()).isEqualTo("Pågående");
		assertThat(result.getInvestigationId()).isEqualTo(2);
		assertThat(result.getServiceId()).isEqualTo(3);
		assertThat(result.getDecisionId()).isEqualTo(4);
	}

	@Test
	void toActualisationWithUnparseableDate() {
		// Act
		final var result = ActualisationMapper.toActualisation(new PersonBasedAktualiseringDTO().date("garbage-date"));

		// Assert
		assertThat(result.getDate()).isNull();
	}

	@Test
	void toActualisationsWithPagination() {
		// Arrange
		final var composite = new ApiPaginationCompositePersonBasedAktualiseringDTO()
			.pageNumber(2)
			.pageSize(20)
			.totalNumberOfPages(5)
			.totalNumberOfRecords(100)
			.result(List.of(new PersonBasedAktualiseringDTO().id(1).name("Ansökan")));

		// Act
		final var result = ActualisationMapper.toActualisations(composite);

		// Assert
		assertThat(result.getPage()).isEqualTo(2);
		assertThat(result.getPageSize()).isEqualTo(20);
		assertThat(result.getTotalPages()).isEqualTo(5);
		assertThat(result.getTotalRecords()).isEqualTo(100);
		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().getFirst().getId()).isEqualTo(1);
		assertThat(result.getResults().getFirst().getName()).isEqualTo("Ansökan");
	}
}
