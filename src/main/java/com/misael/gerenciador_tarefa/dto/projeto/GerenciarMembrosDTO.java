package com.misael.gerenciador_tarefa.dto.projeto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record GerenciarMembrosDTO(
    @NotNull(message = "A lista de membros não pode ser nula")
    @NotEmpty(message = "Informe ao menos um ID de usuário")
    Set<Long> membrosIds
) {}
