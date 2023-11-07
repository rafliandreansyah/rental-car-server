package com.rentalcar.server.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentalcar.server.entity.CarRented;

public interface CarRentedRepository extends JpaRepository<CarRented, UUID> {

}
