package com.misael.gerenciador_tarefa.service;

import com.misael.gerenciador_tarefa.domain.enums.PerfilUsuario;
import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;
import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import com.misael.gerenciador_tarefa.domain.model.Projeto;
import com.misael.gerenciador_tarefa.domain.model.Tarefa;
import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.domain.repository.ProjetoRepository;
import com.misael.gerenciador_tarefa.domain.repository.TarefaRepository;
import com.misael.gerenciador_tarefa.domain.repository.UsuarioRepository;
import com.misael.gerenciador_tarefa.dto.relatorio.RelatorioProjetoDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarStatusTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.CriarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaFiltroDTO;
import com.misael.gerenciador_tarefa.exception.AcessoNegadoException;
import com.misael.gerenciador_tarefa.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TarefaServiceTest {

        @Autowired
        private TarefaService tarefaService;

        @Autowired
        private TarefaRepository tarefaRepository;

        @Autowired
        private ProjetoRepository projetoRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        private Usuario admin;
        private Usuario membro;
        private Usuario estranho;
        private Projeto projeto;

        @BeforeEach
        void setUp() {
                tarefaRepository.deleteAll();
                projetoRepository.deleteAll();
                usuarioRepository.deleteAll();

                admin = usuarioRepository.save(Usuario.builder()
                                .nome("Admin Chefe")
                                .email("admin@email.com")
                                .senha("senha123")
                                .perfil(PerfilUsuario.ADMIN)
                                .build());

                membro = usuarioRepository.save(Usuario.builder()
                                .nome("Membro Dev")
                                .email("membro@email.com")
                                .senha("senha123")
                                .perfil(PerfilUsuario.MEMBRO)
                                .build());

                estranho = usuarioRepository.save(Usuario.builder()
                                .nome("Estranho Outro")
                                .email("estranho@email.com")
                                .senha("senha123")
                                .perfil(PerfilUsuario.MEMBRO)
                                .build());

                Set<Usuario> membros = new HashSet<>();
                membros.add(membro);

                projeto = projetoRepository.save(Projeto.builder()
                                .nome("Projeto Alpha")
                                .descricao("Descrição Alpha")
                                .dono(admin)
                                .membros(membros)
                                .build());
        }

        @Test
        @DisplayName("Deve criar tarefa com sucesso quando usuário é membro do projeto")
        void criarTarefa_comSucesso_quandoMembro() {
                CriarTarefaDTO dto = new CriarTarefaDTO(
                                "Implementar Login",
                                "Login JWT com Spring Security",
                                PrioridadeTarefa.ALTA,
                                LocalDateTime.now().plusDays(5),
                                membro.getId());

                TarefaDTO resultado = tarefaService.criarTarefa(projeto.getId(), dto, membro);

                assertNotNull(resultado);
                assertNotNull(resultado.id());
                assertEquals("Implementar Login", resultado.titulo());
                assertEquals(StatusTarefa.A_FAZER, resultado.status());
                assertEquals(PrioridadeTarefa.ALTA, resultado.prioridade());
                assertEquals("membro@email.com", resultado.responsavel().email());
        }

        @Test
        @DisplayName("Deve lançar AcessoNegadoException quando usuário não pertence ao projeto")
        void criarTarefa_lancaAcessoNegado_quandoNaoPertenceAoProjeto() {
                CriarTarefaDTO dto = new CriarTarefaDTO("Tarefa Teste", "Desc", PrioridadeTarefa.BAIXA, null, null);

                assertThrows(AcessoNegadoException.class,
                                () -> tarefaService.criarTarefa(projeto.getId(), dto, estranho));
        }

        @Test
        @DisplayName("Deve lançar RegraDeNegocioException ao atribuir responsável que não pertence ao projeto")
        void criarTarefa_lancaRegraDeNegocio_quandoResponsavelNaoPertenceAoProjeto() {
                CriarTarefaDTO dto = new CriarTarefaDTO("Tarefa Teste", "Desc", PrioridadeTarefa.BAIXA, null,
                                estranho.getId());

                assertThrows(RegraDeNegocioException.class,
                                () -> tarefaService.criarTarefa(projeto.getId(), dto, membro));
        }

        @Test
        @DisplayName("Deve atualizar status com sucesso de A_FAZER para EM_ANDAMENTO")
        void atualizarStatus_comSucesso_paraEmAndamento() {
                Tarefa tarefa = tarefaRepository.save(Tarefa.builder()
                                .titulo("Tarefa 1")
                                .status(StatusTarefa.A_FAZER)
                                .prioridade(PrioridadeTarefa.MEDIA)
                                .projeto(projeto)
                                .responsavel(membro)
                                .build());

                TarefaDTO resultado = tarefaService.atualizarStatus(
                                projeto.getId(),
                                tarefa.getId(),
                                new AtualizarStatusTarefaDTO(StatusTarefa.EM_ANDAMENTO),
                                membro);

                assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.status());
        }

        @Test
        @DisplayName("Deve lançar RegraDeNegocioException ao tentar mover tarefa de CONCLUIDA para A_FAZER")
        void atualizarStatus_lancaExcecao_quandoConcluidaParaAFazer() {
                Tarefa tarefa = tarefaRepository.save(Tarefa.builder()
                                .titulo("Tarefa Concluida")
                                .status(StatusTarefa.CONCLUIDA)
                                .prioridade(PrioridadeTarefa.MEDIA)
                                .projeto(projeto)
                                .responsavel(membro)
                                .build());

                assertThrows(RegraDeNegocioException.class, () -> tarefaService.atualizarStatus(
                                projeto.getId(),
                                tarefa.getId(),
                                new AtualizarStatusTarefaDTO(StatusTarefa.A_FAZER),
                                membro));
        }

        @Test
        @DisplayName("Deve permitir mover tarefa de CONCLUIDA para EM_ANDAMENTO (reabertura)")
        void atualizarStatus_comSucesso_deConcluidaParaEmAndamento() {
                Tarefa tarefa = tarefaRepository.save(Tarefa.builder()
                                .titulo("Tarefa Reaberta")
                                .status(StatusTarefa.CONCLUIDA)
                                .prioridade(PrioridadeTarefa.MEDIA)
                                .projeto(projeto)
                                .responsavel(membro)
                                .build());

                TarefaDTO resultado = tarefaService.atualizarStatus(
                                projeto.getId(),
                                tarefa.getId(),
                                new AtualizarStatusTarefaDTO(StatusTarefa.EM_ANDAMENTO),
                                membro);

                assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.status());
        }

        @Test
        @DisplayName("Deve permitir concluir tarefa CRITICA quando usuário é ADMIN")
        void atualizarStatus_comSucesso_tarefaCriticaPorAdmin() {
                Tarefa tarefa = tarefaRepository.save(Tarefa.builder()
                                .titulo("Bug Crítico em Produção")
                                .status(StatusTarefa.EM_ANDAMENTO)
                                .prioridade(PrioridadeTarefa.CRITICA)
                                .projeto(projeto)
                                .responsavel(membro)
                                .build());

                TarefaDTO resultado = tarefaService.atualizarStatus(
                                projeto.getId(),
                                tarefa.getId(),
                                new AtualizarStatusTarefaDTO(StatusTarefa.CONCLUIDA),
                                admin);

                assertEquals(StatusTarefa.CONCLUIDA, resultado.status());
        }

        @Test
        @DisplayName("Deve lançar RegraDeNegocioException ao tentar concluir tarefa CRITICA com perfil MEMBRO")
        void atualizarStatus_lancaExcecao_quandoMembroTentaConcluirTarefaCritica() {
                Tarefa tarefa = tarefaRepository.save(Tarefa.builder()
                                .titulo("Bug Crítico em Produção")
                                .status(StatusTarefa.EM_ANDAMENTO)
                                .prioridade(PrioridadeTarefa.CRITICA)
                                .projeto(projeto)
                                .responsavel(membro)
                                .build());

                assertThrows(RegraDeNegocioException.class, () -> tarefaService.atualizarStatus(
                                projeto.getId(),
                                tarefa.getId(),
                                new AtualizarStatusTarefaDTO(StatusTarefa.CONCLUIDA),
                                membro));
        }

        @Test
        @DisplayName("Deve lançar RegraDeNegocioException ao atingir o limite WIP de 5 tarefas EM_ANDAMENTO")
        void atualizarStatus_lancaExcecao_quandoAtingeLimiteWip() {
                // Criar 5 tarefas EM_ANDAMENTO para o membro
                for (int i = 1; i <= 5; i++) {
                        tarefaRepository.save(Tarefa.builder()
                                        .titulo("Tarefa Andamento " + i)
                                        .status(StatusTarefa.EM_ANDAMENTO)
                                        .prioridade(PrioridadeTarefa.MEDIA)
                                        .projeto(projeto)
                                        .responsavel(membro)
                                        .build());
                }

                // Criar a 6ª tarefa como A_FAZER
                Tarefa tarefa6 = tarefaRepository.save(Tarefa.builder()
                                .titulo("Tarefa 6")
                                .status(StatusTarefa.A_FAZER)
                                .prioridade(PrioridadeTarefa.MEDIA)
                                .projeto(projeto)
                                .responsavel(membro)
                                .build());

                // Tentar mover a 6ª tarefa para EM_ANDAMENTO deve falhar por limite de WIP
                assertThrows(RegraDeNegocioException.class, () -> tarefaService.atualizarStatus(
                                projeto.getId(),
                                tarefa6.getId(),
                                new AtualizarStatusTarefaDTO(StatusTarefa.EM_ANDAMENTO),
                                membro));
        }

        @Test
        @DisplayName("Deve filtrar tarefas por status e prioridade com paginação")
        void listarTarefas_comFiltros() {
                tarefaRepository.save(Tarefa.builder().titulo("Tarefa 1").status(StatusTarefa.A_FAZER)
                                .prioridade(PrioridadeTarefa.ALTA).projeto(projeto).build());
                tarefaRepository.save(Tarefa.builder().titulo("Tarefa 2").status(StatusTarefa.EM_ANDAMENTO)
                                .prioridade(PrioridadeTarefa.ALTA).projeto(projeto).build());
                tarefaRepository.save(Tarefa.builder().titulo("Tarefa 3").status(StatusTarefa.CONCLUIDA)
                                .prioridade(PrioridadeTarefa.BAIXA).projeto(projeto).build());

                TarefaFiltroDTO filtro = new TarefaFiltroDTO(StatusTarefa.A_FAZER, PrioridadeTarefa.ALTA, null, null,
                                null, null);
                Page<TarefaDTO> resultado = tarefaService.listarTarefas(projeto.getId(), filtro, PageRequest.of(0, 10),
                                membro);

                assertEquals(1, resultado.getTotalElements());
                assertEquals("Tarefa 1", resultado.getContent().get(0).titulo());
        }

        @Test
        @DisplayName("Deve gerar relatório de resumo de projeto com contadores corretos")
        void gerarRelatorioResumo_comSucesso() {
                tarefaRepository.save(Tarefa.builder().titulo("T1").status(StatusTarefa.A_FAZER)
                                .prioridade(PrioridadeTarefa.ALTA).projeto(projeto).build());
                tarefaRepository.save(Tarefa.builder().titulo("T2").status(StatusTarefa.A_FAZER)
                                .prioridade(PrioridadeTarefa.MEDIA).projeto(projeto).build());
                tarefaRepository.save(Tarefa.builder().titulo("T3").status(StatusTarefa.EM_ANDAMENTO)
                                .prioridade(PrioridadeTarefa.CRITICA).projeto(projeto).build());

                RelatorioProjetoDTO relatorio = tarefaService.gerarRelatorioResumo(projeto.getId(), membro);

                assertNotNull(relatorio);
                assertEquals(3L, relatorio.totalTarefas());
                assertEquals(2L, relatorio.porStatus().get(StatusTarefa.A_FAZER));
                assertEquals(1L, relatorio.porStatus().get(StatusTarefa.EM_ANDAMENTO));
                assertEquals(0L, relatorio.porStatus().get(StatusTarefa.CONCLUIDA));
                assertEquals(1L, relatorio.porPrioridade().get(PrioridadeTarefa.CRITICA));
                assertEquals(1L, relatorio.porPrioridade().get(PrioridadeTarefa.ALTA));
                assertEquals(1L, relatorio.porPrioridade().get(PrioridadeTarefa.MEDIA));
                assertEquals(0L, relatorio.porPrioridade().get(PrioridadeTarefa.BAIXA));
        }
}
