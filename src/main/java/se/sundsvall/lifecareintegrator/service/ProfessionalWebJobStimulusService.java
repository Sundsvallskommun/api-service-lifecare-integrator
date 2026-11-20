package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebCalculationClient;

import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;

@Service
public class ProfessionalWebJobStimulusService {

	private final ProfessionalWebCalculationClient client;

	public ProfessionalWebJobStimulusService(final ProfessionalWebCalculationClient client) {
		this.client = client;
	}

	public byte[] getJobStimulusEndDate(final LocalDate fromDate) {
		return client.getJobStimulusToDate(fromDate.toString());
	}

	public Optional<byte[]> saveJobStimulus(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.saveJobStimulus(body));
	}
}
