package se.sundsvall.lifecareintegrator.integration.professionalweb;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Keeps the ProfessionalWeb session alive between calls.
 *
 * <p>
 * Plain Spring scheduling rather than a dept44 scheduler with ShedLock: the integrator has no database to lock in, and
 * each instance holds its own session, so every instance keeping its own session alive is exactly right.
 * </p>
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
