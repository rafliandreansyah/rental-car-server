package com.rentalcar.server.repository;

import com.rentalcar.server.entity.CarImageDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CarImageDetailRepository extends JpaRepository<CarImageDetail, UUID> {
}
