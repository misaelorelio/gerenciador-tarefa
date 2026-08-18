package com.misael.gerenciador_tarefa.domain.repository;

import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import com.misael.gerenciador_tarefa.domain.model.Tarefa;
import com.misael.gerenciador_tarefa.dto.relatorio.PrioridadeCountDTO;
import com.misael.gerenciador_tarefa.dto.relatorio.StatusCountDTO;
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

    @Query("SELECT new com.misael.gerenciador_tarefa.dto.relatorio.StatusCountDTO(t.status, COUNT(t)) FROM Tarefa t WHERE t.projeto.id = :projetoId GROUP BY t.status")
    List<StatusCountDTO> countTarefasPorStatus(@Param("projetoId") Long projetoId);

    @Query("SELECT new com.misael.gerenciador_tarefa.dto.relatorio.PrioridadeCountDTO(t.prioridade, COUNT(t)) FROM Tarefa t WHERE t.projeto.id = :projetoId GROUP BY t.prioridade")
    List<PrioridadeCountDTO> countTarefasPorPrioridade(@Param("projetoId") Long projetoId);
}
