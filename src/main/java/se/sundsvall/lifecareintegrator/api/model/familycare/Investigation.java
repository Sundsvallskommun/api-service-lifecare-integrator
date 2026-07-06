package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An investigation from the Lifecare family care system")
public class Investigation {

	@Schema(description = "The investigation id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The investigation type", examples = "Ekonomiskt bistånd")
	private String type;

	@Schema(description = "The date the investigation starts", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate fromDate;

	@Schema(description = "The date the investigation ends", examples = "2026-10-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate toDate;

	@Schema(description = "The name of the caseworker", examples = "Anna Andersson")
	private String caseworker;

	@Schema(description = "The organization handling the investigation", examples = "IFO Vuxen")
	private String organization;

	@Schema(description = "The dossier type", examples = "Vuxen")
	private String dossierType;

	@Schema(description = "The name of the applicant", examples = "Bengt Bengtsson")
	private String applicant;

	@Schema(description = "The name of the co-applicant", examples = "Cecilia Cedersson")
	private String coApplicant;

	@Schema(description = "The persons included in the investigation")
	private List<InvestigationPerson> persons;

	public static Investigation create() {
		return new Investigation();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Investigation withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Investigation withType(final String type) {
		this.type = type;
		return this;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(final LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public Investigation withFromDate(final LocalDate fromDate) {
		this.fromDate = fromDate;
		return this;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(final LocalDate toDate) {
		this.toDate = toDate;
	}

	public Investigation withToDate(final LocalDate toDate) {
		this.toDate = toDate;
		return this;
	}

	public String getCaseworker() {
		return caseworker;
	}

	public void setCaseworker(final String caseworker) {
		this.caseworker = caseworker;
	}

	public Investigation withCaseworker(final String caseworker) {
		this.caseworker = caseworker;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public Investigation withOrganization(final String organization) {
		this.organization = organization;
		return this;
	}

	public String getDossierType() {
		return dossierType;
	}

	public void setDossierType(final String dossierType) {
		this.dossierType = dossierType;
	}

	public Investigation withDossierType(final String dossierType) {
		this.dossierType = dossierType;
		return this;
	}

	public String getApplicant() {
		return applicant;
	}

	public void setApplicant(final String applicant) {
		this.applicant = applicant;
	}

	public Investigation withApplicant(final String applicant) {
		this.applicant = applicant;
		return this;
	}

	public String getCoApplicant() {
		return coApplicant;
	}

	public void setCoApplicant(final String coApplicant) {
		this.coApplicant = coApplicant;
	}

	public Investigation withCoApplicant(final String coApplicant) {
		this.coApplicant = coApplicant;
		return this;
	}

	public List<InvestigationPerson> getPersons() {
		return persons;
	}

	public void setPersons(final List<InvestigationPerson> persons) {
		this.persons = persons;
	}

	public Investigation withPersons(final List<InvestigationPerson> persons) {
		this.persons = persons;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Investigation that = (Investigation) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(fromDate, that.fromDate) && Objects.equals(toDate, that.toDate)
			&& Objects.equals(caseworker, that.caseworker) && Objects.equals(organization, that.organization) && Objects.equals(dossierType, that.dossierType)
			&& Objects.equals(applicant, that.applicant) && Objects.equals(coApplicant, that.coApplicant) && Objects.equals(persons, that.persons);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, fromDate, toDate, caseworker, organization, dossierType, applicant, coApplicant, persons);
	}

	@Override
	public String toString() {
		return "Investigation{" +
			"id=" + id +
			", type='" + type + '\'' +
			", fromDate=" + fromDate +
			", toDate=" + toDate +
			", caseworker='" + caseworker + '\'' +
			", organization='" + organization + '\'' +
			", dossierType='" + dossierType + '\'' +
			", applicant='" + applicant + '\'' +
			", coApplicant='" + coApplicant + '\'' +
			", persons=" + persons +
			'}';
	}
}
