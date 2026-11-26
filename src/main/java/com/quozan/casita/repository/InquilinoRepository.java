package com.quozan.casita.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quozan.casita.model.Inquilino;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {
    List<Inquilino> findByDeletedAtIsNullOrderByCognomeAscNomeAsc();
    Optional<Inquilino> findByIdAndDeletedAtIsNull(Long id);
    Optional<Inquilino> findByCodiceFiscaleIgnoreCaseAndDeletedAtIsNull(String codiceFiscale);
    boolean existsByCodiceFiscaleIgnoreCaseAndDeletedAtIsNull(String codiceFiscale);
}
