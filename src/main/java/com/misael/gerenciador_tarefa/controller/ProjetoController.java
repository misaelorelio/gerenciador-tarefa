package com.misael.gerenciador_tarefa.controller;

import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.dto.projeto.AtualizarProjetoDTO;
import com.misael.gerenciador_tarefa.dto.projeto.CriarProjetoDTO;
import com.misael.gerenciador_tarefa.dto.projeto.GerenciarMembrosDTO;
import com.misael.gerenciador_tarefa.dto.projeto.ProjetoDTO;
import com.misael.gerenciador_tarefa.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Projetos", description = "Endpoints de gerenciamento de projetos e seus membros (Exclusivo para ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @Operation(summary = "Criar novo projeto", description = "Cria um novo projeto. O usuário autenticado (ADMIN) torna-se o dono do projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - Requer perfil ADMIN")
    })
    public ResponseEntity<ProjetoDTO> criarProjeto(
            @RequestBody @Valid CriarProjetoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoCriado);
    }

    @GetMapping
    @Operation(summary = "Listar projetos", description = "Retorna a lista de projetos que o usuário ADMIN gerencia ou tem acesso.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de projetos retornada"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - Requer perfil ADMIN")
    })
    public ResponseEntity<List<ProjetoDTO>> listarProjetos(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        List<ProjetoDTO> projetos = projetoService.listarProjetos(usuarioLogado);
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID", description = "Obtém detalhes do projeto pelo seu identificador único.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Projeto encontrado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<ProjetoDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projeto = projetoService.buscarPorId(id, usuarioLogado);
        return ResponseEntity.ok(projeto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto", description = "Atualiza o nome e descrição de um projeto existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<ProjetoDTO> atualizarProjeto(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarProjetoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoAtualizado = projetoService.atualizarProjeto(id, dto, usuarioLogado);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @PostMapping("/{id}/membros")
    @Operation(summary = "Adicionar membros ao projeto", description = "Adiciona novos usuários como membros da equipe do projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Membros adicionados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Lista de membros inválida"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<ProjetoDTO> adicionarMembros(
            @PathVariable Long id,
            @RequestBody @Valid GerenciarMembrosDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoAtualizado = projetoService.adicionarMembros(id, dto, usuarioLogado);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @DeleteMapping("/{id}/membros/{usuarioId}")
    @Operation(summary = "Remover membro do projeto", description = "Remove um usuário da equipe de membros do projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Membro removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto ou usuário não encontrado"),
        @ApiResponse(responseCode = "422", description = "Não é permitido remover o dono do projeto")
    })
    public ResponseEntity<ProjetoDTO> removerMembro(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        ProjetoDTO projetoAtualizado = projetoService.removerMembro(id, usuarioId, usuarioLogado);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir projeto", description = "Remove permanentemente um projeto e todas as suas tarefas associadas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<Void> deletarProjeto(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        projetoService.deletarProjeto(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
