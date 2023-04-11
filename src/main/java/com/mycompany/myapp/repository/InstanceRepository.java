package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Instance;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Instance entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {}
