package com.misael.gerenciador_tarefa.dto.projeto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CriarProjetoDTO(
    @NotBlank(message = "O nome do projeto é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    String nome,

    String descricao,

    Set<Long> membrosIds
) {}
