package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarCreateAndEditResponse {

	private Integer cc;

	private String image;

	@JsonProperty("is_active")
	private Boolean isActive;

	private Integer year;

	private String description;

	private Integer discount;

	private Integer tax;

	private Integer capacity;

	private Integer luggage;

	private String transmission;

	private Double price;

	private String name;

	private String id;

	private String brand;

	@JsonProperty("image_detail")
	private List<ImageDetailItem> imageDetail;
}