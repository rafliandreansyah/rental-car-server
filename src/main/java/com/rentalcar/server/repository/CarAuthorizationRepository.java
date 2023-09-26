package com.rentalcar.server.repository;

import com.rentalcar.server.entity.CarAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarAuthorizationRepository extends JpaRepository<CarAuthorization, UUID>, JpaSpecificationExecutor<CarAuthorization> {


}
