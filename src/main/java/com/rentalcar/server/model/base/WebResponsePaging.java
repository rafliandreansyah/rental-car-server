package com.rentalcar.server.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebResponsePaging<T> {

    @JsonProperty("total_item")
    private Long totalItem;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("last_page")
    private Integer lastPage;

    @JsonProperty("prev_page_url")
    private String prevPageUrl;

    @JsonProperty("next_page_url")
    private String nextPageUrl;

    private Integer status;

    private String error;

    private T data;

}
