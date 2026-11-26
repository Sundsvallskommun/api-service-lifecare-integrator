package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebSessionResponseInterceptor.SessionRequiredException;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession.MODULE;

class ProfessionalWebSessionRetryer implements Retryer {

	static final String UNREACHABLE = "Lifecare could not be reached";
	static final String SESSION_REFUSED = "Lifecare would not accept a freshly established session (status %s)";

	private static final Logger LOG = LoggerFactory.getLogger(ProfessionalWebSessionRetryer.class);

	private final ProfessionalWebSession session;
	private int attempt;

	ProfessionalWebSessionRetryer(final ProfessionalWebSession session) {
		this.session = session;
	}

	@Override
	public void continueOrPropagate(final RetryableException exception) {
		if (!(exception instanceof SessionRequiredException)) {
			throw new ServerProblem(BAD_GATEWAY, UNREACHABLE);
		}
		attempt++;
		if (attempt == 1) {
			LOG.warn("Lifecare wants a session for {} (status {}) - bootstrapping it", MODULE, exception.status());
			session.bootstrapModule(MODULE);
			return;
		}
		if (attempt == 2) {
			LOG.warn("Lifecare still refuses the call (status {}) - signing in again", exception.status());
			session.reset();
			return;
		}
		throw new ServerProblem(BAD_GATEWAY, SESSION_REFUSED.formatted(exception.status()));
	}

	// Feign's Retryer contract: clone() hands each call a fresh retryer with its own attempt count
	@SuppressWarnings({
		"java:S2975", "java:S1182"
	})
	@Override
	public Retryer clone() {
		return new ProfessionalWebSessionRetryer(session);
	}
}
