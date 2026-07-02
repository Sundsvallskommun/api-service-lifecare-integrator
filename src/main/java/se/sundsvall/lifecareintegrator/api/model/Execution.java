package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An execution (verkställighet) from the Lifecare family care system")
public class Execution {

	@Schema(description = "The execution id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The execution type", examples = "Ekonomiskt bistånd")
	private String type;

	@Schema(description = "The date the execution starts", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate fromDate;

	@Schema(description = "The date the execution ends", examples = "2026-10-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate toDate;

	@Schema(description = "The name of the caseworker", examples = "Anna Andersson")
	private String caseworker;

	@Schema(description = "The organization handling the execution", examples = "IFO Vuxen")
	private String organization;

	@Schema(description = "The dossier type", examples = "Vuxen")
	private String dossierType;

	public static Execution create() {
		return new Execution();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Execution withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Execution withType(final String type) {
		this.type = type;
		return this;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(final LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public Execution withFromDate(final LocalDate fromDate) {
		this.fromDate = fromDate;
		return this;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(final LocalDate toDate) {
		this.toDate = toDate;
	}

	public Execution withToDate(final LocalDate toDate) {
		this.toDate = toDate;
		return this;
	}

	public String getCaseworker() {
		return caseworker;
	}

	public void setCaseworker(final String caseworker) {
		this.caseworker = caseworker;
	}

	public Execution withCaseworker(final String caseworker) {
		this.caseworker = caseworker;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public Execution withOrganization(final String organization) {
		this.organization = organization;
		return this;
	}

	public String getDossierType() {
		return dossierType;
	}

	public void setDossierType(final String dossierType) {
		this.dossierType = dossierType;
	}

	public Execution withDossierType(final String dossierType) {
		this.dossierType = dossierType;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Execution that = (Execution) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(fromDate, that.fromDate) && Objects.equals(toDate, that.toDate)
			&& Objects.equals(caseworker, that.caseworker) && Objects.equals(organization, that.organization) && Objects.equals(dossierType, that.dossierType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, fromDate, toDate, caseworker, organization, dossierType);
	}

	@Override
	public String toString() {
		return "Execution{" +
			"id=" + id +
			", type='" + type + '\'' +
			", fromDate=" + fromDate +
			", toDate=" + toDate +
			", caseworker='" + caseworker + '\'' +
			", organization='" + organization + '\'' +
			", dossierType='" + dossierType + '\'' +
			'}';
	}
}
