package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.InvocationContext;
import feign.Response;
import feign.ResponseInterceptor;
import feign.RetryableException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;

import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp.IDENTITY_PORTAL_PATH;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp.SESSION_REQUIRED_STATUS;

class ProfessionalWebSessionResponseInterceptor implements ResponseInterceptor {

	private final ProfessionalWebSession session;

	ProfessionalWebSessionResponseInterceptor(final ProfessionalWebSession session) {
		this.session = session;
	}

	@Override
	public Object intercept(final InvocationContext context, final Chain chain) throws Exception {
		final var response = context.response();
		session.absorbSetCookies(findHeader(response, "Set-Cookie"));
		if (needsSession(response)) {
			throw new SessionRequiredException(response);
		}
		return chain.next(context);
	}

	static boolean needsSession(final Response response) {
		if (response.status() == SESSION_REQUIRED_STATUS || response.status() == 401) {
			return true;
		}
		if (findHeader(response, "Location").stream().anyMatch(location -> location.contains(IDENTITY_PORTAL_PATH))) {
			return true;
		}
		return isSuccess(response) && findHeader(response, "Content-Type").stream().anyMatch(type -> type.contains("text/html"));
	}

	private static boolean isSuccess(final Response response) {
		return response.status() >= 200 && response.status() < 300;
	}

	private static Collection<String> findHeader(final Response response, final String name) {
		return Optional.ofNullable(response.headers().get(name)).orElse(List.of());
	}

	/** Lifecare wants a session for this call; the retryer establishes one and lets Feign ask again. */
	static final class SessionRequiredException extends RetryableException {

		private static final long serialVersionUID = 1L;

		SessionRequiredException(final Response response) {
			super(response.status(), "Lifecare wants a session (status " + response.status() + ")", response.request().httpMethod(), (Long) null, response.request());
		}
	}
}
