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

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class})
public class Transaction {

    @Id
    private String id;

    @Column(name = "no_invoice")
    private String noInvoice;

    @Column(name = "car_name")
    private String carName;

    @Column(name = "car_image_url")
    private String carImageUrl;

    @Column(name = "car_brand")
    @Enumerated(EnumType.STRING)
    private CarBrandEnum carBrand;

    @Column(name = "car_year")
    private Integer carYear;

    @Column(name = "car_capacity")
    private String carCapacity;

    @Column(name = "car_cc")
    private String carCc;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    private Integer duration;

    @Column(name = "car_price")
    private Double carPrice;

    @Column(name = "car_tax")
    private Integer carTax;

    @Column(name = "car_discount")
    private Integer carDiscount;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "user_approved")
    private String userApproved;

    @Enumerated(EnumType.STRING)
    private TransactionStatusEnum status;

    @Column(name = "payment_image")
    private String paymentImage;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
    )
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "car_id",
            referencedColumnName = "id"
    )
    private Car car;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
