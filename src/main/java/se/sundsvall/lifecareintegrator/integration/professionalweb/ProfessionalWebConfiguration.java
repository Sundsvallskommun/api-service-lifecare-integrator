package se.sundsvall.lifecareintegrator.integration.professionalweb;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the ProfessionalWeb properties. The beans themselves are components in this package.
 */
@Configuration
@EnableConfigurationProperties(ProfessionalWebProperties.class)
class ProfessionalWebConfiguration {
}
