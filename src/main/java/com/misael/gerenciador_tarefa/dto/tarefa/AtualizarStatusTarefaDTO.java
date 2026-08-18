package com.misael.gerenciador_tarefa.dto.tarefa;

import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusTarefaDTO(
    @NotNull(message = "O status é obrigatório")
    StatusTarefa status
) {}
