package com.misael.gerenciador_tarefa.dto.tarefa;

import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AtualizarTarefaDTO(
    @NotBlank(message = "O título da tarefa é obrigatório")
    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    String titulo,

    String descricao,

    @NotNull(message = "A prioridade da tarefa é obrigatória")
    PrioridadeTarefa prioridade,

    LocalDateTime prazo,

    Long responsavelId
) {}
