package se.sundsvall.lifecareintegrator.api.model.elderlycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "Decision details specific to the elderly care (EC) system. The LSS-specific fields are only present on LSS decisions")
public class ElderlyCareDecisionDetails {

	@Schema(description = "Id of the investigation the decision belongs to", examples = "5678")
	private Integer investigationId;

	@Schema(description = "The decision code", examples = "Beviljad")
	private String code;

	@Schema(description = "The service category", examples = "Hemtjänst")
	private String serviceCategory;

	@Schema(description = "Granted hours", examples = "10.5")
	private Double hours;

	@Schema(description = "The type of the granted hours", examples = "Per vecka")
	private String hourType;

	@Schema(description = "The type of the decided amount", examples = "Per månad")
	private String amountType;

	@Schema(description = "Granted quantity", examples = "2.0")
	private Double quantity;

	@Schema(description = "The type of the granted quantity", examples = "Per dag")
	private String quantityType;

	@Schema(description = "Granted visits", examples = "3.0")
	private Double visits;

	@Schema(description = "The type of the granted visits", examples = "Per vecka")
	private String visitType;

	@Schema(description = "Granted days", examples = "5.0")
	private Double days;

	@Schema(description = "The type of the granted days", examples = "Per månad")
	private String dayType;

	@Schema(description = "The decision level", examples = "Delegation")
	private String decisionLevel;

	@Schema(description = "The date the execution of the decision starts", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate executionStartDate;

	@Schema(description = "The date the execution of the decision ends", examples = "2026-10-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate executionEndDate;

	@Schema(description = "The iteration number of the decision", examples = "1")
	private Integer iterationNumber;

	@Schema(description = "The number of days the decision covers", examples = "184.0")
	private Double daysOfDecision;

	@Schema(description = "Ids of the orders created from the decision")
	private List<Integer> orderIds;

	@Schema(description = "Whether the decision is deleted in the source system", examples = "false")
	private Boolean deleted;

	@Schema(description = "LSS only: whether the person belongs to person category 1", examples = "true", nullable = true)
	private Boolean personCategory1;

	@Schema(description = "LSS only: whether the person belongs to person category 2", examples = "false", nullable = true)
	private Boolean personCategory2;

	@Schema(description = "LSS only: whether the person belongs to person category 3", examples = "false", nullable = true)
	private Boolean personCategory3;

	@Schema(description = "LSS only: whether the person belongs to person category 3P", examples = "false", nullable = true)
	private Boolean personCategory3P;

	@Schema(description = "LSS only: the increased hourly amount", examples = "150.0", nullable = true)
	private Double increasedHourlyAmount;

	@Schema(description = "LSS only: the standard amount", examples = "300.0", nullable = true)
	private Double standardAmount;

	@Schema(description = "LSS only: name of the SFB caseworker", examples = "Bo Bengtsson", nullable = true)
	private String sfbCaseworker;

	public static ElderlyCareDecisionDetails create() {
		return new ElderlyCareDecisionDetails();
	}

	public Integer getInvestigationId() {
		return investigationId;
	}

	public void setInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
	}

	public ElderlyCareDecisionDetails withInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
		return this;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public ElderlyCareDecisionDetails withCode(final String code) {
		this.code = code;
		return this;
	}

	public String getServiceCategory() {
		return serviceCategory;
	}

	public void setServiceCategory(final String serviceCategory) {
		this.serviceCategory = serviceCategory;
	}

	public ElderlyCareDecisionDetails withServiceCategory(final String serviceCategory) {
		this.serviceCategory = serviceCategory;
		return this;
	}

	public Double getHours() {
		return hours;
	}

	public void setHours(final Double hours) {
		this.hours = hours;
	}

	public ElderlyCareDecisionDetails withHours(final Double hours) {
		this.hours = hours;
		return this;
	}

	public String getHourType() {
		return hourType;
	}

	public void setHourType(final String hourType) {
		this.hourType = hourType;
	}

	public ElderlyCareDecisionDetails withHourType(final String hourType) {
		this.hourType = hourType;
		return this;
	}

	public String getAmountType() {
		return amountType;
	}

	public void setAmountType(final String amountType) {
		this.amountType = amountType;
	}

	public ElderlyCareDecisionDetails withAmountType(final String amountType) {
		this.amountType = amountType;
		return this;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(final Double quantity) {
		this.quantity = quantity;
	}

	public ElderlyCareDecisionDetails withQuantity(final Double quantity) {
		this.quantity = quantity;
		return this;
	}

	public String getQuantityType() {
		return quantityType;
	}

	public void setQuantityType(final String quantityType) {
		this.quantityType = quantityType;
	}

	public ElderlyCareDecisionDetails withQuantityType(final String quantityType) {
		this.quantityType = quantityType;
		return this;
	}

	public Double getVisits() {
		return visits;
	}

	public void setVisits(final Double visits) {
		this.visits = visits;
	}

	public ElderlyCareDecisionDetails withVisits(final Double visits) {
		this.visits = visits;
		return this;
	}

	public String getVisitType() {
		return visitType;
	}

	public void setVisitType(final String visitType) {
		this.visitType = visitType;
	}

	public ElderlyCareDecisionDetails withVisitType(final String visitType) {
		this.visitType = visitType;
		return this;
	}

	public Double getDays() {
		return days;
	}

	public void setDays(final Double days) {
		this.days = days;
	}

	public ElderlyCareDecisionDetails withDays(final Double days) {
		this.days = days;
		return this;
	}

	public String getDayType() {
		return dayType;
	}

	public void setDayType(final String dayType) {
		this.dayType = dayType;
	}

	public ElderlyCareDecisionDetails withDayType(final String dayType) {
		this.dayType = dayType;
		return this;
	}

	public String getDecisionLevel() {
		return decisionLevel;
	}

	public void setDecisionLevel(final String decisionLevel) {
		this.decisionLevel = decisionLevel;
	}

	public ElderlyCareDecisionDetails withDecisionLevel(final String decisionLevel) {
		this.decisionLevel = decisionLevel;
		return this;
	}

	public LocalDate getExecutionStartDate() {
		return executionStartDate;
	}

	public void setExecutionStartDate(final LocalDate executionStartDate) {
		this.executionStartDate = executionStartDate;
	}

	public ElderlyCareDecisionDetails withExecutionStartDate(final LocalDate executionStartDate) {
		this.executionStartDate = executionStartDate;
		return this;
	}

	public LocalDate getExecutionEndDate() {
		return executionEndDate;
	}

	public void setExecutionEndDate(final LocalDate executionEndDate) {
		this.executionEndDate = executionEndDate;
	}

	public ElderlyCareDecisionDetails withExecutionEndDate(final LocalDate executionEndDate) {
		this.executionEndDate = executionEndDate;
		return this;
	}

	public Integer getIterationNumber() {
		return iterationNumber;
	}

	public void setIterationNumber(final Integer iterationNumber) {
		this.iterationNumber = iterationNumber;
	}

	public ElderlyCareDecisionDetails withIterationNumber(final Integer iterationNumber) {
		this.iterationNumber = iterationNumber;
		return this;
	}

	public Double getDaysOfDecision() {
		return daysOfDecision;
	}

	public void setDaysOfDecision(final Double daysOfDecision) {
		this.daysOfDecision = daysOfDecision;
	}

	public ElderlyCareDecisionDetails withDaysOfDecision(final Double daysOfDecision) {
		this.daysOfDecision = daysOfDecision;
		return this;
	}

	public List<Integer> getOrderIds() {
		return orderIds;
	}

	public void setOrderIds(final List<Integer> orderIds) {
		this.orderIds = orderIds;
	}

	public ElderlyCareDecisionDetails withOrderIds(final List<Integer> orderIds) {
		this.orderIds = orderIds;
		return this;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final Boolean deleted) {
		this.deleted = deleted;
	}

	public ElderlyCareDecisionDetails withDeleted(final Boolean deleted) {
		this.deleted = deleted;
		return this;
	}

	public Boolean getPersonCategory1() {
		return personCategory1;
	}

	public void setPersonCategory1(final Boolean personCategory1) {
		this.personCategory1 = personCategory1;
	}

	public ElderlyCareDecisionDetails withPersonCategory1(final Boolean personCategory1) {
		this.personCategory1 = personCategory1;
		return this;
	}

	public Boolean getPersonCategory2() {
		return personCategory2;
	}

	public void setPersonCategory2(final Boolean personCategory2) {
		this.personCategory2 = personCategory2;
	}

	public ElderlyCareDecisionDetails withPersonCategory2(final Boolean personCategory2) {
		this.personCategory2 = personCategory2;
		return this;
	}

	public Boolean getPersonCategory3() {
		return personCategory3;
	}

	public void setPersonCategory3(final Boolean personCategory3) {
		this.personCategory3 = personCategory3;
	}

	public ElderlyCareDecisionDetails withPersonCategory3(final Boolean personCategory3) {
		this.personCategory3 = personCategory3;
		return this;
	}

	public Boolean getPersonCategory3P() {
		return personCategory3P;
	}

	public void setPersonCategory3P(final Boolean personCategory3P) {
		this.personCategory3P = personCategory3P;
	}

	public ElderlyCareDecisionDetails withPersonCategory3P(final Boolean personCategory3P) {
		this.personCategory3P = personCategory3P;
		return this;
	}

	public Double getIncreasedHourlyAmount() {
		return increasedHourlyAmount;
	}

	public void setIncreasedHourlyAmount(final Double increasedHourlyAmount) {
		this.increasedHourlyAmount = increasedHourlyAmount;
	}

	public ElderlyCareDecisionDetails withIncreasedHourlyAmount(final Double increasedHourlyAmount) {
		this.increasedHourlyAmount = increasedHourlyAmount;
		return this;
	}

	public Double getStandardAmount() {
		return standardAmount;
	}

	public void setStandardAmount(final Double standardAmount) {
		this.standardAmount = standardAmount;
	}

	public ElderlyCareDecisionDetails withStandardAmount(final Double standardAmount) {
		this.standardAmount = standardAmount;
		return this;
	}

	public String getSfbCaseworker() {
		return sfbCaseworker;
	}

	public void setSfbCaseworker(final String sfbCaseworker) {
		this.sfbCaseworker = sfbCaseworker;
	}

	public ElderlyCareDecisionDetails withSfbCaseworker(final String sfbCaseworker) {
		this.sfbCaseworker = sfbCaseworker;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ElderlyCareDecisionDetails that = (ElderlyCareDecisionDetails) o;
		return Objects.equals(investigationId, that.investigationId) && Objects.equals(code, that.code) && Objects.equals(serviceCategory, that.serviceCategory)
			&& Objects.equals(hours, that.hours) && Objects.equals(hourType, that.hourType) && Objects.equals(amountType, that.amountType)
			&& Objects.equals(quantity, that.quantity) && Objects.equals(quantityType, that.quantityType) && Objects.equals(visits, that.visits)
			&& Objects.equals(visitType, that.visitType) && Objects.equals(days, that.days) && Objects.equals(dayType, that.dayType)
			&& Objects.equals(decisionLevel, that.decisionLevel) && Objects.equals(executionStartDate, that.executionStartDate)
			&& Objects.equals(executionEndDate, that.executionEndDate) && Objects.equals(iterationNumber, that.iterationNumber)
			&& Objects.equals(daysOfDecision, that.daysOfDecision) && Objects.equals(orderIds, that.orderIds) && Objects.equals(deleted, that.deleted)
			&& Objects.equals(personCategory1, that.personCategory1) && Objects.equals(personCategory2, that.personCategory2)
			&& Objects.equals(personCategory3, that.personCategory3) && Objects.equals(personCategory3P, that.personCategory3P)
			&& Objects.equals(increasedHourlyAmount, that.increasedHourlyAmount) && Objects.equals(standardAmount, that.standardAmount)
			&& Objects.equals(sfbCaseworker, that.sfbCaseworker);
	}

	@Override
	public int hashCode() {
		return Objects.hash(investigationId, code, serviceCategory, hours, hourType, amountType, quantity, quantityType, visits, visitType, days, dayType,
			decisionLevel, executionStartDate, executionEndDate, iterationNumber, daysOfDecision, orderIds, deleted, personCategory1, personCategory2,
			personCategory3, personCategory3P, increasedHourlyAmount, standardAmount, sfbCaseworker);
	}

	@Override
	public String toString() {
		return "ElderlyCareDecisionDetails{" +
			"investigationId=" + investigationId +
			", code='" + code + '\'' +
			", serviceCategory='" + serviceCategory + '\'' +
			", hours=" + hours +
			", hourType='" + hourType + '\'' +
			", amountType='" + amountType + '\'' +
			", quantity=" + quantity +
			", quantityType='" + quantityType + '\'' +
			", visits=" + visits +
			", visitType='" + visitType + '\'' +
			", days=" + days +
			", dayType='" + dayType + '\'' +
			", decisionLevel='" + decisionLevel + '\'' +
			", executionStartDate=" + executionStartDate +
			", executionEndDate=" + executionEndDate +
			", iterationNumber=" + iterationNumber +
			", daysOfDecision=" + daysOfDecision +
			", orderIds=" + orderIds +
			", deleted=" + deleted +
			", personCategory1=" + personCategory1 +
			", personCategory2=" + personCategory2 +
			", personCategory3=" + personCategory3 +
			", personCategory3P=" + personCategory3P +
			", increasedHourlyAmount=" + increasedHourlyAmount +
			", standardAmount=" + standardAmount +
			", sfbCaseworker='" + sfbCaseworker + '\'' +
			'}';
	}
}
