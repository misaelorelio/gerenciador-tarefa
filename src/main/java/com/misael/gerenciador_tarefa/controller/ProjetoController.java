package com.misael.gerenciador_tarefa.controller;

import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.dto.projeto.AtualizarProjetoDTO;
import com.misael.gerenciador_tarefa.dto.projeto.CriarProjetoDTO;
import com.misael.gerenciador_tarefa.dto.projeto.GerenciarMembrosDTO;
import com.misael.gerenciador_tarefa.dto.projeto.ProjetoDTO;
import com.misael.gerenciador_tarefa.service.ProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    public ResponseEntity<ProjetoDTO> criarProjeto(
            @RequestBody @Valid CriarProjetoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoCriado);
    }

    @GetMapping
    public ResponseEntity<List<ProjetoDTO>> listarProjetos(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        List<ProjetoDTO> projetos = projetoService.listarProjetos(usuarioLogado);
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projeto = projetoService.buscarPorId(id, usuarioLogado);
        return ResponseEntity.ok(projeto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoDTO> atualizarProjeto(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarProjetoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoAtualizado = projetoService.atualizarProjeto(id, dto, usuarioLogado);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<ProjetoDTO> adicionarMembros(
            @PathVariable Long id,
            @RequestBody @Valid GerenciarMembrosDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoAtualizado = projetoService.adicionarMembros(id, dto, usuarioLogado);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @DeleteMapping("/{id}/membros/{usuarioId}")
    public ResponseEntity<ProjetoDTO> removerMembro(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoAtualizado = projetoService.removerMembro(id, usuarioId, usuarioLogado);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProjeto(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        projetoService.deletarProjeto(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
