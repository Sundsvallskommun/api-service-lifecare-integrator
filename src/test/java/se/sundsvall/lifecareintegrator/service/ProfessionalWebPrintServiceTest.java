package se.sundsvall.lifecareintegrator.service;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPrintClient;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebPrintProperties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebPrintService.ASKED_AGAIN;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebPrintService.NOT_A_PDF;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebPrintServiceTest {

	private static final byte[] PDF = "%PDF-1.4\nbinaryå".getBytes(UTF_8);
	private static final String PARAMETER_PAGE = """
		<html><body><form id="myForm" method="post" action="">
		  <input type="hidden" name="X-LEGACY-TOKEN" value="page-token">
		  <label>Visa medsökande</label>
		  <input type="checkbox" name="51_0_2_1" value="true" checked>
		  <input type="hidden" name="51_0_2_1" value="false">
		  <input type="checkbox" name="18_0_2_1" value="true">
		  <input type="hidden" name="18_0_2_1" value="false">
		  <input type="checkbox" name="checked_without_value" checked>
		  <input type="radio" name="choice" value="a">
		  <input type="radio" name="choice" value="b" checked>
		  <input type="text" name="disabled_field" value="x" disabled>
		  <select name="language"><option value="en">English</option><option value="sv" selected>Svenska</option></select>
		  <select name="first"><option>Första</option><option>Andra</option></select>
		  <textarea name="note">Rad åäö &amp; mer</textarea>
		  <input type="submit" name="ok" value="Klar">
		  <input type="text" value="no name">
		</form></body></html>
		""";
	private static final String SUBMITTED_FORM = "X-LEGACY-TOKEN=page-token&51_0_2_1=true&51_0_2_1=false&18_0_2_1=false&checked_without_value=on&choice=b"
		+ "&language=sv&first=F%C3%B6rsta&note=Rad+%C3%A5%C3%A4%C3%B6+%26+mer";

	@Mock
	private ProfessionalWebPrintClient clientMock;

	@Captor
	private ArgumentCaptor<Map<String, String>> queryCaptor;

	private ProfessionalWebPrintService service;

	@BeforeEach
	void setUp() {
		service = new ProfessionalWebPrintService(clientMock, new ProfessionalWebPrintProperties("decision-template", "calculation-template", "document-template"));
	}

	@Test
	void getCalculationPdfAsksForThePrintContainerOfTheCalculation() {
		when(clientMock.render(eq("PrintContainer"), anyMap())).thenReturn(PDF);

		assertThat(service.getCalculationPdf(7)).isEqualTo(PDF);

		verify(clientMock).render(eq("PrintContainer"), queryCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "calculation-template"), entry("ownerType", "BERAK"),
			entry("ownerCode", "999999999"), entry("objectId", "7"));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionPdfAsksForTheDecisionPrintWithoutRevisions() {
		when(clientMock.render(eq("PrintDecision"), anyMap())).thenReturn(PDF);

		assertThat(service.getDecisionPdf(9)).isEqualTo(PDF);

		verify(clientMock).render(eq("PrintDecision"), queryCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "decision-template"), entry("decisionId", "9"), entry("hideRevisions", "true"));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentPdfAsksForTheDocumentPrintWithoutRevisions() {
		when(clientMock.render(eq("PrintDocument"), anyMap())).thenReturn(PDF);

		assertThat(service.getDocumentPdf(1)).isEqualTo(PDF);

		verify(clientMock).render(eq("PrintDocument"), queryCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "document-template"), entry("documentId", "1"), entry("hideRevisions", "true"));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void parameterQuestionsAreAnsweredWithLifecaresDefaultsToTheSamePrintAndQuery() {
		when(clientMock.render(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenReturn(PDF);

		assertThat(service.getDecisionPdf(9)).isEqualTo(PDF);

		final var formCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(clientMock).render(eq("PrintDecision"), anyMap());
		verify(clientMock).submitParameters(eq("PrintDecision"), queryCaptor.capture(), formCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "decision-template"), entry("decisionId", "9"), entry("hideRevisions", "true"));
		assertThat(new String(formCaptor.getValue(), UTF_8)).isEqualTo(SUBMITTED_FORM);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void parameterQuestionsAskedAgainAfterTheFormIsBadGateway() {
		when(clientMock.render(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));

		assertThatThrownBy(() -> service.getDecisionPdf(9))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, ASKED_AGAIN);

		verify(clientMock).render(eq("PrintDecision"), anyMap());
		verify(clientMock).submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void somethingElseThanAPdfAfterTheFormIsBadGateway() {
		when(clientMock.render(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenReturn("<html>Fel</html>".getBytes(UTF_8));

		assertThatThrownBy(() -> service.getDecisionPdf(9))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, NOT_A_PDF);

		verify(clientMock).render(eq("PrintDecision"), anyMap());
		verify(clientMock).submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class));
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", "{\"pdf\":false}", "<html><form id=\"myForm\"></form></html>", "<html><input name=\"X-LEGACY-TOKEN\"></html>", "%PD"
	})
	void neitherAPdfNorParameterQuestionsIsBadGatewayWithoutAForm(final String answer) {
		when(clientMock.render(eq("PrintDocument"), anyMap())).thenReturn(answer.getBytes(UTF_8));

		assertThatThrownBy(() -> service.getDocumentPdf(1))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, NOT_A_PDF);

		verify(clientMock).render(eq("PrintDocument"), anyMap());
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void noAnswerAtAllIsBadGateway() {
		when(clientMock.render(eq("PrintDocument"), anyMap())).thenReturn(null);

		assertThatThrownBy(() -> service.getDocumentPdf(1))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, NOT_A_PDF);

		verify(clientMock).render(eq("PrintDocument"), anyMap());
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void formRefusedByLifecareIsLifecaresRefusal() {
		when(clientMock.render(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Mallen saknas"));

		assertThatThrownBy(() -> service.getDecisionPdf(9))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(UNPROCESSABLE_CONTENT, "Mallen saknas");

		verify(clientMock).render(eq("PrintDecision"), anyMap());
		verify(clientMock).submitParameters(eq("PrintDecision"), anyMap(), any(byte[].class));
		verifyNoMoreInteractions(clientMock);
	}
}
