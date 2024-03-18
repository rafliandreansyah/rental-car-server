package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDetailResponse{

	private String id;

	private String name;

	private Integer cc;

	private String image;

	@JsonProperty("is_active")
	private Boolean isActive;

	private Integer year;

	private Integer discount;

	private String description;

	private Integer tax;

	private Integer capacity;

	private String transmission;

	@JsonProperty("price_per_day")
	private Double pricePerDay;

	private String brand;

	private Double rating;

	private Integer totalReview;

	@JsonProperty("has_authorization")
	private List<UserResponse> hasAuthorization;

}