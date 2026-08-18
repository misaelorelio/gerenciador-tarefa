package com.misael.gerenciador_tarefa.controller;

import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.dto.relatorio.RelatorioProjetoDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarStatusTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.CriarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaFiltroDTO;
import com.misael.gerenciador_tarefa.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projetos/{projetoId}")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping("/tarefas")
    public ResponseEntity<TarefaDTO> criarTarefa(
            @PathVariable Long projetoId,
            @RequestBody @Valid CriarTarefaDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        TarefaDTO tarefaDTO = tarefaService.criarTarefa(projetoId, dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaDTO);
    }

    @GetMapping("/tarefas")
    public ResponseEntity<Page<TarefaDTO>> listarTarefas(
            @PathVariable Long projetoId,
            TarefaFiltroDTO filtro,
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        Page<TarefaDTO> tarefas = tarefaService.listarTarefas(projetoId, filtro, pageable, usuarioLogado);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/tarefas/{tarefaId}")
    public ResponseEntity<TarefaDTO> buscarPorId(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        TarefaDTO tarefaDTO = tarefaService.buscarPorId(projetoId, tarefaId, usuarioLogado);
        return ResponseEntity.ok(tarefaDTO);
    }

    @PutMapping("/tarefas/{tarefaId}")
    public ResponseEntity<TarefaDTO> atualizarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid AtualizarTarefaDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        TarefaDTO tarefaDTO = tarefaService.atualizarTarefa(projetoId, tarefaId, dto, usuarioLogado);
        return ResponseEntity.ok(tarefaDTO);
    }

    @PatchMapping("/tarefas/{tarefaId}/status")
    public ResponseEntity<TarefaDTO> atualizarStatus(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid AtualizarStatusTarefaDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        TarefaDTO tarefaDTO = tarefaService.atualizarStatus(projetoId, tarefaId, dto, usuarioLogado);
        return ResponseEntity.ok(tarefaDTO);
    }

    @DeleteMapping("/tarefas/{tarefaId}")
    public ResponseEntity<Void> deletarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        tarefaService.deletarTarefa(projetoId, tarefaId, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorio")
    public ResponseEntity<RelatorioProjetoDTO> gerarRelatorio(
            @PathVariable Long projetoId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        RelatorioProjetoDTO relatorio = tarefaService.gerarRelatorioResumo(projetoId, usuarioLogado);
        return ResponseEntity.ok(relatorio);
    }
}
