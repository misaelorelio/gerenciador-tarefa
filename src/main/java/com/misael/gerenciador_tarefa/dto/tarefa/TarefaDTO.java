package com.misael.gerenciador_tarefa.dto.tarefa;

import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;
import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import com.misael.gerenciador_tarefa.domain.model.Tarefa;
import com.misael.gerenciador_tarefa.dto.usuario.UsuarioResumoDTO;

import java.time.LocalDateTime;

public record TarefaDTO(
    Long id,
    String titulo,
    String descricao,
    StatusTarefa status,
    PrioridadeTarefa prioridade,
    LocalDateTime prazo,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao,
    Long projetoId,
    String projetoNome,
    UsuarioResumoDTO responsavel
) {
    public static TarefaDTO fromEntity(Tarefa tarefa) {
        if (tarefa == null) return null;

        return new TarefaDTO(
            tarefa.getId(),
            tarefa.getTitulo(),
            tarefa.getDescricao(),
            tarefa.getStatus(),
            tarefa.getPrioridade(),
            tarefa.getPrazo(),
            tarefa.getDataCriacao(),
            tarefa.getDataAtualizacao(),
            tarefa.getProjeto() != null ? tarefa.getProjeto().getId() : null,
            tarefa.getProjeto() != null ? tarefa.getProjeto().getNome() : null,
            UsuarioResumoDTO.fromEntity(tarefa.getResponsavel())
        );
    }
}
