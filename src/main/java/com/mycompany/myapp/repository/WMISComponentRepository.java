package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.WMISComponent;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WMISComponent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WMISComponentRepository extends JpaRepository<WMISComponent, Long> {}
