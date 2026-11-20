package se.sundsvall.lifecareintegrator.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebReminderClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ProfessionalWebReminderService {

	static final String BODY_NOT_A_JSON_OBJECT = "The body must be a JSON object";
	static final String REMINDER_ID_MISMATCH = "The body's reminderId must be %s";

	private static final JsonMapper JSON = JsonMapper.builder().build();

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
		return Optional.ofNullable(client.createReminder(body))
			.filter(answer -> answer.length > 0);
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

	private static JsonNode requireJsonObject(final byte[] body) {
		return parse(body)
			.filter(JsonNode::isObject)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT));
	}

	private static Optional<JsonNode> parse(final byte[] body) {
		try {
			return Optional.ofNullable(JSON.readTree(body));
		} catch (final JacksonException _) {
			return Optional.empty();
		}
	}
}
