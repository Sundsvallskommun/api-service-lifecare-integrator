package se.sundsvall.lifecareintegrator.api.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "Paged response wrapper, mirroring the pagination of the underlying Lifecare family care API")
public class PagedResponse<T> {

	@Schema(description = "The current page (one-based)", examples = "1")
	private Integer page;

	@Schema(description = "The number of records per page", examples = "20")
	private Integer pageSize;

	@Schema(description = "The total number of pages", examples = "5")
	private Integer totalPages;

	@Schema(description = "The total number of records", examples = "98")
	private Integer totalRecords;

	@Schema(description = "The records on the current page")
	private List<T> results;

	public static <T> PagedResponse<T> create() {
		return new PagedResponse<>();
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(final Integer page) {
		this.page = page;
	}

	public PagedResponse<T> withPage(final Integer page) {
		this.page = page;
		return this;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(final Integer pageSize) {
		this.pageSize = pageSize;
	}

	public PagedResponse<T> withPageSize(final Integer pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(final Integer totalPages) {
		this.totalPages = totalPages;
	}

	public PagedResponse<T> withTotalPages(final Integer totalPages) {
		this.totalPages = totalPages;
		return this;
	}

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(final Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public PagedResponse<T> withTotalRecords(final Integer totalRecords) {
		this.totalRecords = totalRecords;
		return this;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(final List<T> results) {
		this.results = results;
	}

	public PagedResponse<T> withResults(final List<T> results) {
		this.results = results;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedResponse<?> that = (PagedResponse<?>) o;
		return Objects.equals(page, that.page) && Objects.equals(pageSize, that.pageSize) && Objects.equals(totalPages, that.totalPages)
			&& Objects.equals(totalRecords, that.totalRecords) && Objects.equals(results, that.results);
	}

	@Override
	public int hashCode() {
		return Objects.hash(page, pageSize, totalPages, totalRecords, results);
	}

	@Override
	public String toString() {
		return "PagedResponse{" +
			"page=" + page +
			", pageSize=" + pageSize +
			", totalPages=" + totalPages +
			", totalRecords=" + totalRecords +
			", results=" + results +
			'}';
	}
}
