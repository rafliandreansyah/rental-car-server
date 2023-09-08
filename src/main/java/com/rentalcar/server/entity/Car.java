package com.rentalcar.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class})
public class Car {

    @Id
    private String id;

    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private CarBrandEnum brand;

    private Integer year;

    private Integer capacity;

    private Integer cc;

    @Column(name = "price_per_day")
    private Double pricePerDay;

    private Integer tax;

    private Integer discount;

    private String description;

    @Enumerated(EnumType.STRING)
    private CarTransmissionEnum transmission;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "car")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "car")
    private List<CarAuthorization> carAuthorizations;

    @OneToMany(mappedBy = "car")
    private List<CarImageDetail> carImageDetails;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
