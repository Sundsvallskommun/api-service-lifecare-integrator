package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import feign.InvocationContext;
import feign.Response;
import feign.ResponseInterceptor;
import feign.RetryableException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebParameterForm;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;

import static feign.Request.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp.IDENTITY_PORTAL_PATH;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp.SESSION_REQUIRED_STATUS;

class ProfessionalWebSessionResponseInterceptor implements ResponseInterceptor {

	static final String HTML_ANSWER_TO_WRITE = "Lifecare answered a write with an HTML page";

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
		// A write is sent again only when Lifecare refused it for the session, so an HTML page is an answer, not a lost session
		if (!isRead(response) && isHtmlPage(response)) {
			throw new ServerProblem(BAD_GATEWAY, HTML_ANSWER_TO_WRITE);
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
		return isRead(response) && isHtmlPage(response);
	}

	private static boolean isRead(final Response response) {
		return response.request().httpMethod() == GET;
	}

	private static boolean isHtmlPage(final Response response) {
		return isSuccess(response) && findHeader(response, "Content-Type").stream().anyMatch(type -> type.contains("text/html")) && !isParameterQuestions(response);
	}

	// A print's parameter questions are an HTML page too, but one the caller answers
	private static boolean isParameterQuestions(final Response response) {
		if (response.body() == null) {
			return false;
		}
		try (InputStream stream = response.body().asInputStream()) {
			return ProfessionalWebParameterForm.isParameterQuestions(stream.readAllBytes());
		} catch (final IOException _) {
			return false;
		}
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
