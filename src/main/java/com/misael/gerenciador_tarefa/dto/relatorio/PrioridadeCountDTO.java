package com.misael.gerenciador_tarefa.dto.relatorio;

import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;

public record PrioridadeCountDTO(
    PrioridadeTarefa prioridade,
    Long total
) {}
