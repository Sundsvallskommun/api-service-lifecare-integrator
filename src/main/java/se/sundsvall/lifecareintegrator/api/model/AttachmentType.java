package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "An attachment type with its valid sender types")
public class AttachmentType {

	@Schema(description = "The id of the attachment type", examples = "1")
	private Integer id;

	@Schema(description = "The name of the attachment type", examples = "Ansökan")
	private String name;

	@Schema(description = "The valid sender types for this attachment type")
	private List<CodeItem> senderTypes;

	public static AttachmentType create() {
		return new AttachmentType();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public AttachmentType withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public AttachmentType withName(final String name) {
		this.name = name;
		return this;
	}

	public List<CodeItem> getSenderTypes() {
		return senderTypes;
	}

	public void setSenderTypes(final List<CodeItem> senderTypes) {
		this.senderTypes = senderTypes;
	}

	public AttachmentType withSenderTypes(final List<CodeItem> senderTypes) {
		this.senderTypes = senderTypes;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final AttachmentType that = (AttachmentType) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(senderTypes, that.senderTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, senderTypes);
	}

	@Override
	public String toString() {
		return "AttachmentType{" +
			"id=" + id +
			", name='" + name + '\'' +
			", senderTypes=" + senderTypes +
			'}';
	}
}
