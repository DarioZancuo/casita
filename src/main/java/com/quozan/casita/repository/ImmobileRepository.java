package com.quozan.casita.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quozan.casita.model.Immobile;

public interface ImmobileRepository extends JpaRepository<Immobile, Long> {
    List<Immobile> findByDeletedAtIsNullOrderByCodiceAsc();
    Optional<Immobile> findByIdAndDeletedAtIsNull(Long id);
}
