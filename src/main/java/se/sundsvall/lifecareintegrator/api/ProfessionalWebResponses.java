package se.sundsvall.lifecareintegrator.api;

import java.util.Optional;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

final class ProfessionalWebResponses {

	private ProfessionalWebResponses() {}

	static ResponseEntity<byte[]> toCreated(final Optional<byte[]> answer) {
		return answer
			.map(body -> status(CREATED).body(body))
			.orElseGet(() -> noContent().build());
	}

	static ResponseEntity<byte[]> toUpdated(final Optional<byte[]> answer) {
		return answer
			.map(body -> ok().body(body))
			.orElseGet(() -> noContent().build());
	}
}
