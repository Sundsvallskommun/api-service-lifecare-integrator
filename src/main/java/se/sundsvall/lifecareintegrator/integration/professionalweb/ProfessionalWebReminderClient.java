package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebReminderClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/ListRemindersByServiceId", produces = APPLICATION_JSON_VALUE)
	byte[] listRemindersByServiceId(
		@RequestParam(value = "id") final long serviceId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/GetProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getProposalForService(
		@RequestParam(value = "id") final long serviceId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/GetReminderEditComposite", produces = APPLICATION_JSON_VALUE)
	byte[] getReminderEditComposite(
		@RequestParam(value = "reminderId") final long reminderId,
		@RequestParam(value = "readOptions") final boolean readOptions);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/CreateReminder/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createReminder(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/UpdateReminder/", consumes = APPLICATION_JSON_VALUE)
	void updateReminder(
		@RequestBody final byte[] body);

	@DeleteMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/RemoveReminder/", consumes = APPLICATION_JSON_VALUE)
	void removeReminder(
		@RequestBody final byte[] body);
}
