package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A payment from the Lifecare family care system")
public class Payment {

	@Schema(description = "The payment id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The payment amount", examples = "5000.0")
	private Double amount;

	@Schema(description = "The payment method", examples = "Bankkonto")
	private String paymentMethod;

	@Schema(description = "The date the payment is paid out", examples = "2026-05-25")
	@DateTimeFormat(iso = DATE)
	private LocalDate payDate;

	@Schema(description = "The clearing number of the receiving account", examples = "8420")
	private String clearing;

	@Schema(description = "The account number of the receiving account", examples = "1234567")
	private String accountNumber;

	@Schema(description = "The name of the payment receiver", examples = "Anna Andersson")
	private String name;

	@Schema(description = "The street address of the payment receiver", examples = "Storgatan 1")
	private String streetAddress;

	@Schema(description = "The care-of address of the payment receiver", examples = "c/o Andersson")
	private String careOfAddress;

	@Schema(description = "The postal code of the payment receiver", examples = "85230")
	private String postalCode;

	@Schema(description = "The postal address of the payment receiver", examples = "Sundsvall")
	private String postalAddress;

	@Schema(description = "The billing number", examples = "123-4567")
	private String billingNumber;

	@Schema(description = "The local number", examples = "42")
	private String localNumber;

	@Schema(description = "The voucher number", examples = "V12345")
	private String voucherNumber;

	@Schema(description = "The payment message", examples = "Ekonomiskt bistånd maj")
	private String message;

	@Schema(description = "The id of the investigation execution the payment belongs to", examples = "23456")
	private Integer investigationExecutionId;

	@Schema(description = "The id of the service the payment belongs to", examples = "34567")
	private Integer serviceId;

	@Schema(description = "The id of the connected application", examples = "45678")
	private Integer connectedApplication;

	@Schema(description = "The month the payment concerns", examples = "2026-05")
	private String concernedMonth;

	@Schema(description = "Names of the persons the payment concerns", examples = "[\"Anna Andersson\"]")
	private List<String> persons;

	public static Payment create() {
		return new Payment();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Payment withId(final Integer id) {
		this.id = id;
		return this;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(final Double amount) {
		this.amount = amount;
	}

	public Payment withAmount(final Double amount) {
		this.amount = amount;
		return this;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(final String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public Payment withPaymentMethod(final String paymentMethod) {
		this.paymentMethod = paymentMethod;
		return this;
	}

	public LocalDate getPayDate() {
		return payDate;
	}

	public void setPayDate(final LocalDate payDate) {
		this.payDate = payDate;
	}

	public Payment withPayDate(final LocalDate payDate) {
		this.payDate = payDate;
		return this;
	}

	public String getClearing() {
		return clearing;
	}

	public void setClearing(final String clearing) {
		this.clearing = clearing;
	}

	public Payment withClearing(final String clearing) {
		this.clearing = clearing;
		return this;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(final String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Payment withAccountNumber(final String accountNumber) {
		this.accountNumber = accountNumber;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Payment withName(final String name) {
		this.name = name;
		return this;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(final String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public Payment withStreetAddress(final String streetAddress) {
		this.streetAddress = streetAddress;
		return this;
	}

	public String getCareOfAddress() {
		return careOfAddress;
	}

	public void setCareOfAddress(final String careOfAddress) {
		this.careOfAddress = careOfAddress;
	}

	public Payment withCareOfAddress(final String careOfAddress) {
		this.careOfAddress = careOfAddress;
		return this;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	public Payment withPostalCode(final String postalCode) {
		this.postalCode = postalCode;
		return this;
	}

	public String getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
	}

	public Payment withPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
		return this;
	}

	public String getBillingNumber() {
		return billingNumber;
	}

	public void setBillingNumber(final String billingNumber) {
		this.billingNumber = billingNumber;
	}

	public Payment withBillingNumber(final String billingNumber) {
		this.billingNumber = billingNumber;
		return this;
	}

	public String getLocalNumber() {
		return localNumber;
	}

	public void setLocalNumber(final String localNumber) {
		this.localNumber = localNumber;
	}

	public Payment withLocalNumber(final String localNumber) {
		this.localNumber = localNumber;
		return this;
	}

	public String getVoucherNumber() {
		return voucherNumber;
	}

	public void setVoucherNumber(final String voucherNumber) {
		this.voucherNumber = voucherNumber;
	}

	public Payment withVoucherNumber(final String voucherNumber) {
		this.voucherNumber = voucherNumber;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public Payment withMessage(final String message) {
		this.message = message;
		return this;
	}

	public Integer getInvestigationExecutionId() {
		return investigationExecutionId;
	}

	public void setInvestigationExecutionId(final Integer investigationExecutionId) {
		this.investigationExecutionId = investigationExecutionId;
	}

	public Payment withInvestigationExecutionId(final Integer investigationExecutionId) {
		this.investigationExecutionId = investigationExecutionId;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public Payment withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public Integer getConnectedApplication() {
		return connectedApplication;
	}

	public void setConnectedApplication(final Integer connectedApplication) {
		this.connectedApplication = connectedApplication;
	}

	public Payment withConnectedApplication(final Integer connectedApplication) {
		this.connectedApplication = connectedApplication;
		return this;
	}

	public String getConcernedMonth() {
		return concernedMonth;
	}

	public void setConcernedMonth(final String concernedMonth) {
		this.concernedMonth = concernedMonth;
	}

	public Payment withConcernedMonth(final String concernedMonth) {
		this.concernedMonth = concernedMonth;
		return this;
	}

	public List<String> getPersons() {
		return persons;
	}

	public void setPersons(final List<String> persons) {
		this.persons = persons;
	}

	public Payment withPersons(final List<String> persons) {
		this.persons = persons;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Payment that = (Payment) o;
		return Objects.equals(id, that.id) && Objects.equals(amount, that.amount) && Objects.equals(paymentMethod, that.paymentMethod) && Objects.equals(payDate, that.payDate)
			&& Objects.equals(clearing, that.clearing) && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(name, that.name)
			&& Objects.equals(streetAddress, that.streetAddress) && Objects.equals(careOfAddress, that.careOfAddress) && Objects.equals(postalCode, that.postalCode)
			&& Objects.equals(postalAddress, that.postalAddress) && Objects.equals(billingNumber, that.billingNumber) && Objects.equals(localNumber, that.localNumber)
			&& Objects.equals(voucherNumber, that.voucherNumber) && Objects.equals(message, that.message) && Objects.equals(investigationExecutionId, that.investigationExecutionId)
			&& Objects.equals(serviceId, that.serviceId) && Objects.equals(connectedApplication, that.connectedApplication) && Objects.equals(concernedMonth, that.concernedMonth)
			&& Objects.equals(persons, that.persons);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, amount, paymentMethod, payDate, clearing, accountNumber, name, streetAddress, careOfAddress, postalCode, postalAddress, billingNumber,
			localNumber, voucherNumber, message, investigationExecutionId, serviceId, connectedApplication, concernedMonth, persons);
	}

	@Override
	public String toString() {
		return "Payment{" +
			"id=" + id +
			", amount=" + amount +
			", paymentMethod='" + paymentMethod + '\'' +
			", payDate=" + payDate +
			", clearing='" + clearing + '\'' +
			", accountNumber='" + accountNumber + '\'' +
			", name='" + name + '\'' +
			", streetAddress='" + streetAddress + '\'' +
			", careOfAddress='" + careOfAddress + '\'' +
			", postalCode='" + postalCode + '\'' +
			", postalAddress='" + postalAddress + '\'' +
			", billingNumber='" + billingNumber + '\'' +
			", localNumber='" + localNumber + '\'' +
			", voucherNumber='" + voucherNumber + '\'' +
			", message='" + message + '\'' +
			", investigationExecutionId=" + investigationExecutionId +
			", serviceId=" + serviceId +
			", connectedApplication=" + connectedApplication +
			", concernedMonth='" + concernedMonth + '\'' +
			", persons=" + persons +
			'}';
	}
}
