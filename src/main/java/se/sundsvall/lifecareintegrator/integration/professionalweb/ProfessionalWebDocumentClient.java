package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration.CLIENT_ID;

// Paths as Lifecare's web app sends them: the proposals have no trailing slash, every other Document path needs one
@FeignClient(name = CLIENT_ID, contextId = "lifecare-professionalweb-document", url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebDocumentClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetDocumentsListForClient/", produces = APPLICATION_JSON_VALUE)
	byte[] getDocumentsListForClient(
		@RequestParam(value = "id") final String personNumber);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetJournalNoteWithContent/", produces = APPLICATION_JSON_VALUE)
	byte[] getJournalNoteWithContent(
		@RequestParam(value = "id") final long id,
		@RequestParam(value = "hideRevisions") final boolean hideRevisions,
		@RequestParam(value = "inEdit") final boolean inEdit);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetDocumentWithContent/", produces = APPLICATION_JSON_VALUE)
	byte[] getDocumentWithContent(
		@RequestParam(value = "id") final long id,
		@RequestParam(value = "hideRevisions") final boolean hideRevisions,
		@RequestParam(value = "inEdit") final boolean inEdit);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetNoteProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getNoteProposalForService(
		@RequestParam(value = "id") final long serviceId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetDocumentProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getDocumentProposalForService(
		@RequestParam(value = "id") final long serviceId);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/CreateJournalNote/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createJournalNote(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/CreateDocument/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createDocument(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/UpdateJournalNote/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] updateJournalNote(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/UpdateDocument/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] updateDocument(
		@RequestBody final byte[] body);
}
