package com.rentalcar.server.repository;

import com.rentalcar.server.entity.Car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {

    @Query(value = "SELECT c FROM Car c WHERE c.carId IN " +
            "(SELECT cr.carId FROM CarRented cr WHERE " +
            "(cr.endDate >= :startDate AND cr.startDate <= :endDate) " +
            "OR (cr.startDate <= :endDate AND cr.endDate >= :startDate))")
    List<Car> findCarsInDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

}
