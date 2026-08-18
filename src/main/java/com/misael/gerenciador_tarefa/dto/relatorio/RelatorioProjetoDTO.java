package com.misael.gerenciador_tarefa.dto.relatorio;

import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;
import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;

import java.util.Map;

public record RelatorioProjetoDTO(
    Long projetoId,
    String projetoNome,
    Map<StatusTarefa, Long> porStatus,
    Map<PrioridadeTarefa, Long> porPrioridade,
    long totalTarefas
) {}
