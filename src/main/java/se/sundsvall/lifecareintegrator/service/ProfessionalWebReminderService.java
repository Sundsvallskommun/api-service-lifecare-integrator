package se.sundsvall.lifecareintegrator.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebReminderClient;
import tools.jackson.databind.JsonNode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;

@Service
public class ProfessionalWebReminderService {

	static final String REMINDER_ID_MISMATCH = "The body's reminderId must be %s";

	private final ProfessionalWebReminderClient client;

	public ProfessionalWebReminderService(final ProfessionalWebReminderClient client) {
		this.client = client;
	}

	public byte[] listReminders(final long serviceId) {
		return client.listRemindersByServiceId(serviceId);
	}

	public byte[] getReminderProposal(final long serviceId) {
		return client.getProposalForService(serviceId);
	}

	public byte[] getReminder(final long reminderId) {
		return client.getReminderEditComposite(reminderId, false);
	}

	public Optional<byte[]> createReminder(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.createReminder(body));
	}

	public void updateReminder(final long reminderId, final byte[] body) {
		Optional.ofNullable(requireJsonObject(body).get("reminderId"))
			.filter(JsonNode::canConvertToLong)
			.filter(bodyReminderId -> bodyReminderId.longValue() == reminderId)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, REMINDER_ID_MISMATCH.formatted(reminderId)));
		client.updateReminder(body);
	}

	public void removeReminder(final long reminderId) {
		client.removeReminder(("{\"id\":" + reminderId + "}").getBytes(UTF_8));
	}

}
