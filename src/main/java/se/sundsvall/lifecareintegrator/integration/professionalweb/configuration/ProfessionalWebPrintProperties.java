package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The Lifecare print templates the print resources render with. The defaults are the templates
 * api-service-caremanagement
 * uses.
 *
 * @param decisionTemplateId    the template a decision is printed with
 * @param calculationTemplateId the template a calculation is printed with
 * @param documentTemplateId    the template a written document or form is printed with. Lifecare ties templates to the
 *                              document's owner and refuses one the owner lacks
 */
@ConfigurationProperties("integration.lifecare-professionalweb.print")
public record ProfessionalWebPrintProperties(
	@DefaultValue("885bfb68-c97b-47c0-921d-ef00caaa2423") String decisionTemplateId,
	@DefaultValue("7936e9ec-e32a-40cf-9dea-d95b16e021f0") String calculationTemplateId,
	@DefaultValue("b9f20b49-c8ba-4c44-ad33-b2bf8f0b688a") String documentTemplateId) {
}
