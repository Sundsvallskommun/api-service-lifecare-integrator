package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalWebPrintPropertiesTest {

	private static final String PREFIX = "integration.lifecare-professionalweb.print";

	@Test
	void templatesDefaultToTheOnesCaremanagementUses() {
		final var properties = bind(Map.of());

		assertThat(properties.decisionTemplateId()).isEqualTo("885bfb68-c97b-47c0-921d-ef00caaa2423");
		assertThat(properties.calculationTemplateId()).isEqualTo("7936e9ec-e32a-40cf-9dea-d95b16e021f0");
		assertThat(properties.documentTemplateId()).isEqualTo("b9f20b49-c8ba-4c44-ad33-b2bf8f0b688a");
	}

	@Test
	void configuredTemplatesAreUsed() {
		final var properties = bind(Map.of(
			PREFIX + ".decision-template-id", "d",
			PREFIX + ".calculation-template-id", "c",
			PREFIX + ".document-template-id", "x"));

		assertThat(properties.decisionTemplateId()).isEqualTo("d");
		assertThat(properties.calculationTemplateId()).isEqualTo("c");
		assertThat(properties.documentTemplateId()).isEqualTo("x");
	}

	private static ProfessionalWebPrintProperties bind(final Map<String, String> values) {
		return new Binder(new MapConfigurationPropertySource(values)).bindOrCreate(PREFIX, ProfessionalWebPrintProperties.class);
	}
}
