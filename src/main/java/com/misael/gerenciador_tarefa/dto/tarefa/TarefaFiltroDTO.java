package com.misael.gerenciador_tarefa.dto.tarefa;

import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;
import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record TarefaFiltroDTO(
    StatusTarefa status,
    PrioridadeTarefa prioridade,
    Long responsavelId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime prazoInicio,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime prazoFim,
    String busca
) {}
