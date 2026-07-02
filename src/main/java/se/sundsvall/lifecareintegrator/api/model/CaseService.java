package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A service (insats) from the Lifecare family care system")
public class CaseService {

	@Schema(description = "The service id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The service type", examples = "Ekonomiskt bistånd")
	private String type;

	@Schema(description = "The organization handling the service", examples = "IFO Vuxen")
	private String organization;

	@Schema(description = "The date the service starts", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate startDate;

	@Schema(description = "The date the service ends", examples = "2026-10-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate endDate;

	@Schema(description = "The name of the caseworker", examples = "Anna Andersson")
	private String caseworker;

	@Schema(description = "The name of the co-caseworker", examples = "Bengt Bengtsson")
	private String coCaseworker;

	@Schema(description = "The id of the investigation the service belongs to", examples = "23456")
	private Integer investigationId;

	@Schema(description = "The id of the decision the service belongs to", examples = "34567")
	private Integer decisionId;

	@Schema(description = "The name of the applicant", examples = "Cecilia Cedersson")
	private String applicant;

	@Schema(description = "The name of the co-applicant", examples = "David Davidsson")
	private String coApplicant;

	@Schema(description = "Names of the persons the service concerns", examples = "[\"Anna Andersson\"]")
	private List<String> persons;

	public static CaseService create() {
		return new CaseService();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public CaseService withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public CaseService withType(final String type) {
		this.type = type;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public CaseService withOrganization(final String organization) {
		this.organization = organization;
		return this;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
	}

	public CaseService withStartDate(final LocalDate startDate) {
		this.startDate = startDate;
		return this;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(final LocalDate endDate) {
		this.endDate = endDate;
	}

	public CaseService withEndDate(final LocalDate endDate) {
		this.endDate = endDate;
		return this;
	}

	public String getCaseworker() {
		return caseworker;
	}

	public void setCaseworker(final String caseworker) {
		this.caseworker = caseworker;
	}

	public CaseService withCaseworker(final String caseworker) {
		this.caseworker = caseworker;
		return this;
	}

	public String getCoCaseworker() {
		return coCaseworker;
	}

	public void setCoCaseworker(final String coCaseworker) {
		this.coCaseworker = coCaseworker;
	}

	public CaseService withCoCaseworker(final String coCaseworker) {
		this.coCaseworker = coCaseworker;
		return this;
	}

	public Integer getInvestigationId() {
		return investigationId;
	}

	public void setInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
	}

	public CaseService withInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
		return this;
	}

	public Integer getDecisionId() {
		return decisionId;
	}

	public void setDecisionId(final Integer decisionId) {
		this.decisionId = decisionId;
	}

	public CaseService withDecisionId(final Integer decisionId) {
		this.decisionId = decisionId;
		return this;
	}

	public String getApplicant() {
		return applicant;
	}

	public void setApplicant(final String applicant) {
		this.applicant = applicant;
	}

	public CaseService withApplicant(final String applicant) {
		this.applicant = applicant;
		return this;
	}

	public String getCoApplicant() {
		return coApplicant;
	}

	public void setCoApplicant(final String coApplicant) {
		this.coApplicant = coApplicant;
	}

	public CaseService withCoApplicant(final String coApplicant) {
		this.coApplicant = coApplicant;
		return this;
	}

	public List<String> getPersons() {
		return persons;
	}

	public void setPersons(final List<String> persons) {
		this.persons = persons;
	}

	public CaseService withPersons(final List<String> persons) {
		this.persons = persons;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CaseService that = (CaseService) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(organization, that.organization) && Objects.equals(startDate, that.startDate)
			&& Objects.equals(endDate, that.endDate) && Objects.equals(caseworker, that.caseworker) && Objects.equals(coCaseworker, that.coCaseworker)
			&& Objects.equals(investigationId, that.investigationId) && Objects.equals(decisionId, that.decisionId) && Objects.equals(applicant, that.applicant)
			&& Objects.equals(coApplicant, that.coApplicant) && Objects.equals(persons, that.persons);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, organization, startDate, endDate, caseworker, coCaseworker, investigationId, decisionId, applicant, coApplicant, persons);
	}

	@Override
	public String toString() {
		return "CaseService{" +
			"id=" + id +
			", type='" + type + '\'' +
			", organization='" + organization + '\'' +
			", startDate=" + startDate +
			", endDate=" + endDate +
			", caseworker='" + caseworker + '\'' +
			", coCaseworker='" + coCaseworker + '\'' +
			", investigationId=" + investigationId +
			", decisionId=" + decisionId +
			", applicant='" + applicant + '\'' +
			", coApplicant='" + coApplicant + '\'' +
			", persons=" + persons +
			'}';
	}
}
