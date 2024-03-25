package com.rentalcar.server.repository;

import com.rentalcar.server.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    List<Rating> findByUserIdAndCarId(UUID userId, UUID carId);

}
