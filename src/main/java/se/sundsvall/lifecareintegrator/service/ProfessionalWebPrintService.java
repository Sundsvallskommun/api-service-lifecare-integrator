package se.sundsvall.lifecareintegrator.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebParameterForm;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPrintClient;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebPrintProperties;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class ProfessionalWebPrintService {

	static final String NOT_A_PDF = "Lifecare answered the print with something that is not a PDF";
	static final String ASKED_AGAIN = "Lifecare asked the print's parameter questions again after they were answered";

	/** The print container's owner for a calculation, as Lifecare's web app asks for it. */
	private static final String CALCULATION_OWNER_TYPE = "BERAK";
	private static final String CALCULATION_OWNER_CODE = "999999999";
	private static final byte[] PDF_SIGNATURE = "%PDF".getBytes(US_ASCII);

	private final ProfessionalWebPrintClient client;
	private final ProfessionalWebPrintProperties properties;

	public ProfessionalWebPrintService(final ProfessionalWebPrintClient client, final ProfessionalWebPrintProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	public byte[] getCalculationPdf(final long calculationId) {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", properties.calculationTemplateId());
		query.put("ownerType", CALCULATION_OWNER_TYPE);
		query.put("ownerCode", CALCULATION_OWNER_CODE);
		query.put("objectId", String.valueOf(calculationId));
		return print("PrintContainer", query);
	}

	public byte[] getDecisionPdf(final long decisionId) {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", properties.decisionTemplateId());
		query.put("decisionId", String.valueOf(decisionId));
		query.put("hideRevisions", "true");
		return print("PrintDecision", query);
	}

	public byte[] getDocumentPdf(final long documentId) {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", properties.documentTemplateId());
		query.put("documentId", String.valueOf(documentId));
		query.put("hideRevisions", "true");
		return print("PrintDocument", query);
	}

	private byte[] print(final String print, final Map<String, String> query) {
		final var answer = client.render(print, query);
		if (isPdf(answer)) {
			return answer;
		}
		final var form = ProfessionalWebParameterForm.answerWithDefaults(answer)
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, NOT_A_PDF));
		final var rendered = client.submitParameters(print, query, form);
		if (isPdf(rendered)) {
			return rendered;
		}
		if (ProfessionalWebParameterForm.isParameterQuestions(rendered)) {
			throw Problem.valueOf(BAD_GATEWAY, ASKED_AGAIN);
		}
		throw Problem.valueOf(BAD_GATEWAY, NOT_A_PDF);
	}

	private static boolean isPdf(final byte[] answer) {
		return answer != null && answer.length >= PDF_SIGNATURE.length
			&& Arrays.equals(answer, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length);
	}
}
