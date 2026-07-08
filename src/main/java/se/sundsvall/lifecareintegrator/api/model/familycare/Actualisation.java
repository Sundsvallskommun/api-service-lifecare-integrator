package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An actualisation (aktualisering) from Lifecare Family Care, re-modelled to a common shape")
public class Actualisation {

	@Schema(description = "The actualisation id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The actualisation type", examples = "Ansökan")
	private String type;

	@Schema(description = "Name of the person the actualisation concerns", examples = "Kalle Karlsson")
	private String name;

	@Schema(description = "The date of the actualisation", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate date;

	@Schema(description = "The reason for the actualisation", examples = "Ansökan om ekonomiskt bistånd")
	private String reason;

	@Schema(description = "What the actualisation regards", examples = "Ekonomiskt bistånd")
	private String regards;

	@Schema(description = "Who initiated the actualisation", examples = "Den enskilde")
	private String fromWho;

	@Schema(description = "Name of the caseworker handling the actualisation", examples = "Anna Andersson")
	private String caseworker;

	@Schema(description = "The organization handling the actualisation", examples = "Vuxen försörjningsstöd")
	private String organization;

	@Schema(description = "The actualisation status", examples = "Inledd utredning")
	private String status;

	@Schema(description = "The id of the investigation connected to the actualisation", examples = "456")
	private Integer investigationId;

	@Schema(description = "The id of the service connected to the actualisation", examples = "789")
	private Integer serviceId;

	@Schema(description = "The id of the decision connected to the actualisation", examples = "1011")
	private Integer decisionId;

	public static Actualisation create() {
		return new Actualisation();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Actualisation withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Actualisation withType(final String type) {
		this.type = type;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Actualisation withName(final String name) {
		this.name = name;
		return this;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(final LocalDate date) {
		this.date = date;
	}

	public Actualisation withDate(final LocalDate date) {
		this.date = date;
		return this;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	public Actualisation withReason(final String reason) {
		this.reason = reason;
		return this;
	}

	public String getRegards() {
		return regards;
	}

	public void setRegards(final String regards) {
		this.regards = regards;
	}

	public Actualisation withRegards(final String regards) {
		this.regards = regards;
		return this;
	}

	public String getFromWho() {
		return fromWho;
	}

	public void setFromWho(final String fromWho) {
		this.fromWho = fromWho;
	}

	public Actualisation withFromWho(final String fromWho) {
		this.fromWho = fromWho;
		return this;
	}

	public String getCaseworker() {
		return caseworker;
	}

	public void setCaseworker(final String caseworker) {
		this.caseworker = caseworker;
	}

	public Actualisation withCaseworker(final String caseworker) {
		this.caseworker = caseworker;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public Actualisation withOrganization(final String organization) {
		this.organization = organization;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public Actualisation withStatus(final String status) {
		this.status = status;
		return this;
	}

	public Integer getInvestigationId() {
		return investigationId;
	}

	public void setInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
	}

	public Actualisation withInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public Actualisation withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public Integer getDecisionId() {
		return decisionId;
	}

	public void setDecisionId(final Integer decisionId) {
		this.decisionId = decisionId;
	}

	public Actualisation withDecisionId(final Integer decisionId) {
		this.decisionId = decisionId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Actualisation that = (Actualisation) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(name, that.name) && Objects.equals(date, that.date)
			&& Objects.equals(reason, that.reason) && Objects.equals(regards, that.regards) && Objects.equals(fromWho, that.fromWho)
			&& Objects.equals(caseworker, that.caseworker) && Objects.equals(organization, that.organization) && Objects.equals(status, that.status)
			&& Objects.equals(investigationId, that.investigationId) && Objects.equals(serviceId, that.serviceId) && Objects.equals(decisionId, that.decisionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, name, date, reason, regards, fromWho, caseworker, organization, status, investigationId, serviceId, decisionId);
	}

	@Override
	public String toString() {
		return "Actualisation{" +
			"id=" + id +
			", type='" + type + '\'' +
			", name='" + name + '\'' +
			", date=" + date +
			", reason='" + reason + '\'' +
			", regards='" + regards + '\'' +
			", fromWho='" + fromWho + '\'' +
			", caseworker='" + caseworker + '\'' +
			", organization='" + organization + '\'' +
			", status='" + status + '\'' +
			", investigationId=" + investigationId +
			", serviceId=" + serviceId +
			", decisionId=" + decisionId +
			'}';
	}
}
