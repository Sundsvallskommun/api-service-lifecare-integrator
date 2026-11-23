package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPaymentClient;
import tools.jackson.databind.JsonNode;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.parse;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBusinessTypes.SERVICE;

@Service
public class ProfessionalWebPaymentService {

	static final String NO_PERSON = "Lifecare named no person for the service";

	private final ProfessionalWebPaymentClient client;

	public ProfessionalWebPaymentService(final ProfessionalWebPaymentClient client) {
		this.client = client;
	}

	public byte[] getPaymentProposal(final long serviceId) {
		return client.getPaymentForCreate(SERVICE, serviceId);
	}

	public byte[] getLatestPayments(final long serviceId) {
		return client.getLatestPayments(SERVICE, serviceId);
	}

	public byte[] hasHousehold(final long serviceId, final LocalDate date) {
		final var personId = findPersonId(client.getPaymentForCreate(SERVICE, serviceId))
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, NO_PERSON));
		return client.hasHouseholdThisDate(personId, date.toString());
	}

	// The person Lifecare's own payment form asks about is the one its underlag names in payment.susPersonId
	private static Optional<String> findPersonId(final byte[] paymentForCreate) {
		return parse(paymentForCreate)
			.map(underlag -> underlag.path("payment").path("susPersonId"))
			.filter(JsonNode::isValueNode)
			.filter(personId -> !personId.isNull())
			.map(JsonNode::asString)
			.filter(StringUtils::hasText);
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
