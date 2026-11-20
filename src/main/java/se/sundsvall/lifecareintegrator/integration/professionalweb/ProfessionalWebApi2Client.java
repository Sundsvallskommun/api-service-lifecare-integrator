package se.sundsvall.lifecareintegrator.integration.professionalweb;

import feign.Param;
import feign.RequestLine;

public interface ProfessionalWebApi2Client {

	@RequestLine("GET /api2/Reminders/ListRemindersByServiceId?id={id}")
	byte[] listRemindersByServiceId(@Param("id") long id);
}
