package com.misael.gerenciador_tarefa.dto.relatorio;

import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;

public record StatusCountDTO(
    StatusTarefa status,
    Long total
) {}
