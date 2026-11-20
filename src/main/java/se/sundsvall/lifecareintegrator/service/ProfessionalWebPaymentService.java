package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPaymentClient;

import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBusinessTypes.SERVICE;

@Service
public class ProfessionalWebPaymentService {

	private final ProfessionalWebPaymentClient client;
	private final PartyIntegration partyIntegration;

	public ProfessionalWebPaymentService(final ProfessionalWebPaymentClient client, final PartyIntegration partyIntegration) {
		this.client = client;
		this.partyIntegration = partyIntegration;
	}

	public byte[] getPaymentProposal(final long serviceId) {
		return client.getPaymentForCreate(SERVICE, serviceId);
	}

	public byte[] getLatestPayments(final long serviceId) {
		return client.getLatestPayments(SERVICE, serviceId);
	}

	public byte[] hasHousehold(final String municipalityId, final String partyId, final LocalDate date) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return client.hasHouseholdThisDate(personNumber, date.toString());
	}

	public Optional<byte[]> createPayment(final long serviceId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.createPayment(SERVICE, serviceId, body));
	}

	public Optional<byte[]> createPayee(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.createPayee(body));
	}
}
