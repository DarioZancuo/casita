package com.quozan.casita.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.quozan.casita.model.Contratto;

public interface ContrattoRepository extends JpaRepository<Contratto, Long> {
    List<Contratto> findByDeletedAtIsNullOrderByDataFineAsc();
    Optional<Contratto> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
        select c from Contratto c
        where c.deletedAt is null
          and c.dataFine between :dal and :al
        order by c.dataFine
        """)
    List<Contratto> findInScadenza(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    @Query("""
        select count(c) from Contratto c
        where c.deletedAt is null
          and c.immobile.id = :immobileId
          and (:contrattoId is null or c.id <> :contrattoId)
          and c.dataInizio <= :dataFine
          and c.dataFine >= :dataInizio
        """)
    long countSovrapposti(@Param("immobileId") Long immobileId,
                          @Param("contrattoId") Long contrattoId,
                          @Param("dataInizio") LocalDate dataInizio,
                          @Param("dataFine") LocalDate dataFine);

    @Query("""
        select count(c) from Contratto c
        where c.deletedAt is null and c.inquilino.id = :inquilinoId and c.dataFine >= :oggi
        """)
    long countAttiviOFuturiPerInquilino(@Param("inquilinoId") Long inquilinoId, @Param("oggi") LocalDate oggi);
}
