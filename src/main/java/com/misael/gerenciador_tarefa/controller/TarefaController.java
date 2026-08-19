package com.misael.gerenciador_tarefa.controller;

import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.dto.relatorio.RelatorioProjetoDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarStatusTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.CriarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaFiltroDTO;
import com.misael.gerenciador_tarefa.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tarefas", description = "Endpoints para gerenciamento do ciclo de vida das tarefas e relatórios de projeto")
@SecurityRequirement(name = "bearerAuth")
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping("/tarefas")
    @Operation(summary = "Criar nova tarefa", description = "Cria uma nova tarefa dentro de um projeto. O responsável atribuído deve ser membro do projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - Usuário não pertence ao projeto"),
        @ApiResponse(responseCode = "422", description = "Violação de regra de negócio (ex: responsável não é membro)")
    })
    public ResponseEntity<TarefaDTO> criarTarefa(
            @PathVariable Long projetoId,
            @RequestBody @Valid CriarTarefaDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        TarefaDTO tarefaDTO = tarefaService.criarTarefa(projetoId, dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaDTO);
    }

    @GetMapping("/tarefas")
    @Operation(summary = "Listar tarefas com filtros e paginação", description = "Busca tarefas do projeto com suporte a filtros dinâmicos (status, prioridade, responsável, range de prazo e busca textual) e ordenação.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de tarefas retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
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
    @Operation(summary = "Buscar tarefa por ID", description = "Retorna os detalhes completos de uma tarefa específica do projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TarefaDTO> buscarPorId(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        TarefaDTO tarefaDTO = tarefaService.buscarPorId(projetoId, tarefaId, usuarioLogado);
        return ResponseEntity.ok(tarefaDTO);
    }

    @PutMapping("/tarefas/{tarefaId}")
    @Operation(summary = "Atualizar dados da tarefa", description = "Atualiza título, descrição, prioridade, prazo e responsável da tarefa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "422", description = "Violação de regras (ex: WIP limit ou responsável inválido)")
    })
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
    @Operation(summary = "Atualizar status da tarefa", description = "Move a tarefa de status respeitando as regras de negócio: CONCLUIDA não volta para A_FAZER, tarefas CRITICA só podem ser concluídas por ADMIN e limite de 5 tarefas EM_ANDAMENTO por responsável.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "422", description = "Transição inválida ou limite WIP atingido")
    })
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
    @Operation(summary = "Excluir tarefa", description = "Remove uma tarefa do projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<Void> deletarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        tarefaService.deletarTarefa(projetoId, tarefaId, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorio")
    @Operation(summary = "Relatório resumido do projeto", description = "Retorna a contagem agregada de tarefas agrupadas por status (A_FAZER, EM_ANDAMENTO, CONCLUIDA) e por prioridade (BAIXA, MEDIA, ALTA, CRITICA), além do total geral.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<RelatorioProjetoDTO> gerarRelatorio(
            @PathVariable Long projetoId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        RelatorioProjetoDTO relatorio = tarefaService.gerarRelatorioResumo(projetoId, usuarioLogado);
        return ResponseEntity.ok(relatorio);
    }
}
