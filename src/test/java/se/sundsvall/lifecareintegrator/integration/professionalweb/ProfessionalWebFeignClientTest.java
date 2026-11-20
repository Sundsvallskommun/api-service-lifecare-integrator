package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.net.URI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebFeignClient.toQueryParams;

class ProfessionalWebFeignClientTest {

	@Test
	void toQueryParamsDecodesEachPairInOrder() {
		assertThat(toQueryParams(URI.create("http://host/path?reminderId=7&text=a%20b%C3%A5&readOptions=false")))
			.containsExactly(entry("reminderId", "7"), entry("text", "a bå"), entry("readOptions", "false"));
	}

	@Test
	void toQueryParamsGivesAParameterWithoutValueAnEmptyOne() {
		assertThat(toQueryParams(URI.create("http://host/path?flag&id=1")))
			.containsExactly(entry("flag", ""), entry("id", "1"));
	}

	@Test
	void toQueryParamsWithoutQueryIsEmpty() {
		assertThat(toQueryParams(URI.create("http://host/path"))).isEmpty();
	}
}
