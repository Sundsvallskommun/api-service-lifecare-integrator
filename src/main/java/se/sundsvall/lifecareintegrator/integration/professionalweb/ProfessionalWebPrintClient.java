package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration.CLIENT_ID;

/** Lifecare's own prints, outside api2: the pages its web app opens to print, answered with a PDF. */
@FeignClient(name = CLIENT_ID, contextId = "lifecare-professionalweb-print", url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebPrintClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/RenderPdf/{print}")
	byte[] render(
		@PathVariable("print") final String print,
		@RequestParam final Map<String, String> query);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/RenderPdf/{print}", consumes = APPLICATION_FORM_URLENCODED_VALUE)
	byte[] submitParameters(
		@PathVariable("print") final String print,
		@RequestParam final Map<String, String> query,
		@RequestBody final byte[] form);
}
