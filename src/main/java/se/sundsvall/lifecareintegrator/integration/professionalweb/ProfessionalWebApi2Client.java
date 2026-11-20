package se.sundsvall.lifecareintegrator.integration.professionalweb;

import feign.Param;
import feign.RequestLine;

public interface ProfessionalWebApi2Client {

	@RequestLine("GET /api2/Reminders/ListRemindersByServiceId?id={id}")
	byte[] listRemindersByServiceId(@Param("id") long id);

	@RequestLine("GET /api2/Reminders/GetProposalForService?id={id}")
	byte[] getProposalForService(@Param("id") long id);

	@RequestLine("GET /api2/Reminders/GetReminderEditComposite?reminderId={reminderId}&readOptions=false")
	byte[] getReminderEditComposite(@Param("reminderId") long reminderId);

	@RequestLine("POST /api2/Reminders/CreateReminder/")
	byte[] createReminder(byte[] body);

	@RequestLine("POST /api2/Reminders/UpdateReminder/")
	void updateReminder(byte[] body);

	@RequestLine("DELETE /api2/Reminders/RemoveReminder/")
	void removeReminder(byte[] body);
}
