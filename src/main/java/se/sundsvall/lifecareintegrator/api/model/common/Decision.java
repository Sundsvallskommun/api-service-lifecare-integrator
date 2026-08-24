package se.sundsvall.lifecareintegrator.api.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.lifecareintegrator.api.model.elderlycare.ElderlyCareDecisionDetails;
import se.sundsvall.lifecareintegrator.api.model.familycare.FamilyCareDecisionDetails;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = """
	A decision from one of the Lifecare systems, re-modelled to a common shape. \
	Uniqueness is per (source, law, decisionId) — the decisionId alone is only unique within its source system.""")
public class Decision {

	@Schema(description = "The source system the decision originates from", examples = "ELDERLY_CARE", allowableValues = {
		"ELDERLY_CARE", "FAMILY_CARE"
	})
	private String source;

	@Schema(description = """
		The law this decision is made under. Only present for ELDERLY_CARE decisions. SFB decisions \
		(assistansersättning, decided by Försäkringskassan) are delivered by the LSS source, so a decision's law is \
		not always the law of the source that carried it.""", examples = "SOL", allowableValues = {
		"SOL", "LSS", "SFB"
	}, nullable = true)
	private String law;

	@Schema(description = "The decision id in the source system", examples = "12345")
	private String decisionId;

	@Schema(description = "The date the decision was made", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate decided;

	@Schema(description = "The date the decision is valid from", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate validFrom;

	@Schema(description = "The date the decision is valid to", examples = "2026-10-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate validTo;

	@Schema(description = "The decision type", examples = "Ekonomiskt bistånd")
	private String type;

	@Schema(description = "The decision reason", examples = "Bifall")
	private String reason;

	@Schema(description = "Name of the caseworker that made the decision", examples = "Anna Andersson")
	private String decisionMaker;

	@Schema(description = "The decided amount, when the decision concerns an amount", examples = "5000.0")
	private BigDecimal amount;

	@Schema(description = "Details only present for ELDERLY_CARE decisions", nullable = true)
	private ElderlyCareDecisionDetails elderlyCareDetails;

	@Schema(description = "Details only present for FAMILY_CARE decisions", nullable = true)
	private FamilyCareDecisionDetails familyCareDetails;

	public static Decision create() {
		return new Decision();
	}

	public String getSource() {
		return source;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	public Decision withSource(final String source) {
		this.source = source;
		return this;
	}

	public String getLaw() {
		return law;
	}

	public void setLaw(final String law) {
		this.law = law;
	}

	public Decision withLaw(final String law) {
		this.law = law;
		return this;
	}

	public String getDecisionId() {
		return decisionId;
	}

	public void setDecisionId(final String decisionId) {
		this.decisionId = decisionId;
	}

	public Decision withDecisionId(final String decisionId) {
		this.decisionId = decisionId;
		return this;
	}

	public LocalDate getDecided() {
		return decided;
	}

	public void setDecided(final LocalDate decided) {
		this.decided = decided;
	}

	public Decision withDecided(final LocalDate decided) {
		this.decided = decided;
		return this;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(final LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public Decision withValidFrom(final LocalDate validFrom) {
		this.validFrom = validFrom;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(final LocalDate validTo) {
		this.validTo = validTo;
	}

	public Decision withValidTo(final LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Decision withType(final String type) {
		this.type = type;
		return this;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	public Decision withReason(final String reason) {
		this.reason = reason;
		return this;
	}

	public String getDecisionMaker() {
		return decisionMaker;
	}

	public void setDecisionMaker(final String decisionMaker) {
		this.decisionMaker = decisionMaker;
	}

	public Decision withDecisionMaker(final String decisionMaker) {
		this.decisionMaker = decisionMaker;
		return this;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public Decision withAmount(final BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public ElderlyCareDecisionDetails getElderlyCareDetails() {
		return elderlyCareDetails;
	}

	public void setElderlyCareDetails(final ElderlyCareDecisionDetails elderlyCareDetails) {
		this.elderlyCareDetails = elderlyCareDetails;
	}

	public Decision withElderlyCareDetails(final ElderlyCareDecisionDetails elderlyCareDetails) {
		this.elderlyCareDetails = elderlyCareDetails;
		return this;
	}

	public FamilyCareDecisionDetails getFamilyCareDetails() {
		return familyCareDetails;
	}

	public void setFamilyCareDetails(final FamilyCareDecisionDetails familyCareDetails) {
		this.familyCareDetails = familyCareDetails;
	}

	public Decision withFamilyCareDetails(final FamilyCareDecisionDetails familyCareDetails) {
		this.familyCareDetails = familyCareDetails;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Decision that = (Decision) o;
		return Objects.equals(source, that.source) && Objects.equals(law, that.law) && Objects.equals(decisionId, that.decisionId) && Objects.equals(decided, that.decided)
			&& Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo) && Objects.equals(type, that.type) && Objects.equals(reason, that.reason)
			&& Objects.equals(decisionMaker, that.decisionMaker) && Objects.equals(amount, that.amount) && Objects.equals(elderlyCareDetails, that.elderlyCareDetails)
			&& Objects.equals(familyCareDetails, that.familyCareDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, law, decisionId, decided, validFrom, validTo, type, reason, decisionMaker, amount, elderlyCareDetails, familyCareDetails);
	}

	@Override
	public String toString() {
		return "Decision{" +
			"source='" + source + '\'' +
			", law='" + law + '\'' +
			", decisionId='" + decisionId + '\'' +
			", decided=" + decided +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", type='" + type + '\'' +
			", reason='" + reason + '\'' +
			", decisionMaker='" + decisionMaker + '\'' +
			", amount=" + amount +
			", elderlyCareDetails=" + elderlyCareDetails +
			", familyCareDetails=" + familyCareDetails +
			'}';
	}
}
