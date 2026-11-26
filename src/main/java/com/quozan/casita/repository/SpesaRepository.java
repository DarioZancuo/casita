package com.quozan.casita.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quozan.casita.model.SpesaCondominiale;

public interface SpesaRepository extends JpaRepository<SpesaCondominiale, Long> {
    Optional<SpesaCondominiale> findByIdAndDeletedAtIsNull(Long id);
    List<SpesaCondominiale> findByDeletedAtIsNullAndDataSpesaBetweenOrderByDataSpesaDesc(LocalDate dal, LocalDate al);
}
