package com.misael.gerenciador_tarefa.domain.repository;

import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import com.misael.gerenciador_tarefa.domain.model.Tarefa;
import com.misael.gerenciador_tarefa.domain.repository.projection.PrioridadeCountProjection;
import com.misael.gerenciador_tarefa.domain.repository.projection.StatusCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

    long countByResponsavelIdAndStatus(Long responsavelId, StatusTarefa status);

    long countByProjetoId(Long projetoId);

    @Query("SELECT t.status AS status, COUNT(t) AS total FROM Tarefa t WHERE t.projeto.id = :projetoId GROUP BY t.status")
    List<StatusCountProjection> countTarefasPorStatus(@Param("projetoId") Long projetoId);

    @Query("SELECT t.prioridade AS prioridade, COUNT(t) AS total FROM Tarefa t WHERE t.projeto.id = :projetoId GROUP BY t.prioridade")
    List<PrioridadeCountProjection> countTarefasPorPrioridade(@Param("projetoId") Long projetoId);
}
