package se.sundsvall.lifecareintegrator.integration.professionalweb.session;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Keeps the ProfessionalWeb session alive between calls. Plain Spring scheduling: there is no database to lock in, and
 * every instance keeps its own session alive.
 */
@Configuration
@EnableScheduling
class ProfessionalWebKeepAlive {

	private final ProfessionalWebSession session;

	ProfessionalWebKeepAlive(final ProfessionalWebSession session) {
		this.session = session;
	}

	@Scheduled(fixedDelayString = "${integration.lifecare-professionalweb.keep-alive-interval:5m}", initialDelayString = "${integration.lifecare-professionalweb.keep-alive-interval:5m}")
	void keepAlive() {
		session.keepAlive();
	}
}
