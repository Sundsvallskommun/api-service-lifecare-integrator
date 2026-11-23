package se.sundsvall.lifecareintegrator.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebCalculationClient;

import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBusinessTypes.CALCULATION;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBusinessTypes.SERVICE;

@Service
public class ProfessionalWebCalculationService {

	/** Lifecare's web app lists a service's calculations without an investigation. */
	private static final long NO_INVESTIGATION = 0;

	private final ProfessionalWebCalculationClient client;

	public ProfessionalWebCalculationService(final ProfessionalWebCalculationClient client) {
		this.client = client;
	}

	public byte[] getCalculationProposal(final long serviceId) {
		return client.getProposalService(SERVICE, serviceId);
	}

	public byte[] getLatestCalculation(final long serviceId) {
		return client.listCalculations(SERVICE, serviceId, NO_INVESTIGATION, serviceId, true);
	}

	public byte[] getCalculation(final long calculationId) {
		return client.getCalculation(CALCULATION, calculationId);
	}

	public byte[] getCalculationForEdit(final long calculationId) {
		return client.getCalculationForEdit(CALCULATION, calculationId);
	}

	public byte[] placePersons(final byte[] body) {
		requireJsonObject(body);
		return client.placePersons(body);
	}

	public byte[] getAmount(final byte[] body) {
		requireJsonObject(body);
		return client.getAmount(body);
	}

	public byte[] getSharedCost(final byte[] body) {
		requireJsonObject(body);
		return client.getSharedCost(body);
	}

	public byte[] getJobStimuli(final byte[] body) {
		requireJsonObject(body);
		return client.getJobStimuliForCalculation(body);
	}

	public Optional<byte[]> createCalculation(final long serviceId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.create(SERVICE, serviceId, body));
	}

	public Optional<byte[]> updateCalculation(final long calculationId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.update(CALCULATION, calculationId, body));
	}

	public void deleteCalculation(final long calculationId) {
		client.delete(CALCULATION, calculationId, new byte[0]);
	}
}
