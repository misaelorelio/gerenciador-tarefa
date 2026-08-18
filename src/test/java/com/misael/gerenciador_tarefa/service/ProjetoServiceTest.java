package com.misael.gerenciador_tarefa.service;

import com.misael.gerenciador_tarefa.domain.enums.PerfilUsuario;
import com.misael.gerenciador_tarefa.domain.model.Projeto;
import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.domain.repository.ProjetoRepository;
import com.misael.gerenciador_tarefa.domain.repository.UsuarioRepository;
import com.misael.gerenciador_tarefa.dto.projeto.AtualizarProjetoDTO;
import com.misael.gerenciador_tarefa.dto.projeto.CriarProjetoDTO;
import com.misael.gerenciador_tarefa.dto.projeto.GerenciarMembrosDTO;
import com.misael.gerenciador_tarefa.dto.projeto.ProjetoDTO;
import com.misael.gerenciador_tarefa.exception.AcessoNegadoException;
import com.misael.gerenciador_tarefa.exception.RecursoNaoEncontradoException;
import com.misael.gerenciador_tarefa.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProjetoServiceTest {

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario dono;
    private Usuario membro;
    private Usuario estranho;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        projetoRepository.deleteAll();
        usuarioRepository.deleteAll();

        dono = usuarioRepository.save(Usuario.builder()
                .nome("Dono Silva")
                .email("dono@email.com")
                .senha("$2a$10$hashFicticio123456789012345678901234567890")
                .perfil(PerfilUsuario.ADMIN)
                .build());

        membro = usuarioRepository.save(Usuario.builder()
                .nome("Membro Souza")
                .email("membro@email.com")
                .senha("$2a$10$hashFicticio123456789012345678901234567890")
                .perfil(PerfilUsuario.MEMBRO)
                .build());

        estranho = usuarioRepository.save(Usuario.builder()
                .nome("Estranho Lima")
                .email("estranho@email.com")
                .senha("$2a$10$hashFicticio123456789012345678901234567890")
                .perfil(PerfilUsuario.MEMBRO)
                .build());

        admin = usuarioRepository.save(Usuario.builder()
                .nome("Admin Geral")
                .email("admin@email.com")
                .senha("$2a$10$hashFicticio123456789012345678901234567890")
                .perfil(PerfilUsuario.ADMIN)
                .build());
    }

    @Test
    @DisplayName("Deve lançar AcessoNegadoException ao tentar criar projeto com perfil MEMBRO")
    void criarProjeto_lancaAcessoNegado_quandoMembro() {
        CriarProjetoDTO dto = new CriarProjetoDTO("Projeto Não Permitido", "Desc", null);

        assertThrows(AcessoNegadoException.class, () -> projetoService.criarProjeto(dto, membro));
    }

    @Test
    @DisplayName("Deve criar projeto com sucesso sem membros iniciais")
    void criarProjeto_comSucesso_semMembros() {
        CriarProjetoDTO dto = new CriarProjetoDTO("Novo Projeto", "Desc", null);

        ProjetoDTO resultado = projetoService.criarProjeto(dto, dono);

        assertNotNull(resultado);
        assertNotNull(resultado.id());
        assertEquals("Novo Projeto", resultado.nome());
        assertEquals("Desc", resultado.descricao());
        assertEquals("dono@email.com", resultado.dono().email());
        assertTrue(resultado.membros().isEmpty());
    }

    @Test
    @DisplayName("Deve criar projeto com sucesso com membros iniciais")
    void criarProjeto_comSucesso_comMembros() {
        CriarProjetoDTO dto = new CriarProjetoDTO("Projeto Com Membros", "Desc", Set.of(membro.getId()));

        ProjetoDTO resultado = projetoService.criarProjeto(dto, dono);

        assertNotNull(resultado);
        assertEquals(1, resultado.membros().size());
        assertTrue(resultado.membros().stream().anyMatch(m -> m.email().equals("membro@email.com")));
    }

    @Test
    @DisplayName("Deve listar projetos onde o usuário é dono")
    void listarProjetos_retornaProjetosDoUsuarioDono() {
        projetoService.criarProjeto(new CriarProjetoDTO("Projeto do Dono", "Desc", null), dono);
        projetoService.criarProjeto(new CriarProjetoDTO("Projeto de Outro Admin", "Desc", null), admin);

        List<ProjetoDTO> resultado = projetoService.listarProjetos(dono);

        assertEquals(1, resultado.size());
        assertEquals("Projeto do Dono", resultado.get(0).nome());
    }

    @Test
    @DisplayName("Deve lançar AcessoNegadoException ao tentar listar projetos com perfil MEMBRO")
    void listarProjetos_lancaAcessoNegado_quandoUsuarioEhMembro() {
        assertThrows(AcessoNegadoException.class, () -> projetoService.listarProjetos(membro));
    }

    @Test
    @DisplayName("Deve buscar projeto por ID com sucesso quando o usuário é ADMIN")
    void buscarPorId_comSucesso_quandoAdmin() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(new CriarProjetoDTO("Projeto Teste", "Desc", null), dono);

        ProjetoDTO resultado = projetoService.buscarPorId(projetoCriado.id(), admin);

        assertNotNull(resultado);
        assertEquals(projetoCriado.id(), resultado.id());
    }

    @Test
    @DisplayName("Deve lançar AcessoNegadoException ao tentar buscar projeto por ID com perfil MEMBRO")
    void buscarPorId_lancaAcessoNegado_quandoMembro() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(
                new CriarProjetoDTO("Projeto Teste", "Desc", Set.of(membro.getId())), dono);

        assertThrows(AcessoNegadoException.class, () -> projetoService.buscarPorId(projetoCriado.id(), membro));
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando ID do projeto não existir")
    void buscarPorId_lancaExcecao_quandoProjetoNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> projetoService.buscarPorId(999L, dono));
    }

    @Test
    @DisplayName("Deve atualizar projeto com sucesso quando usuário for o dono")
    void atualizarProjeto_comSucesso_quandoDono() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(new CriarProjetoDTO("Projeto Original", "Desc Original", null), dono);
        AtualizarProjetoDTO dto = new AtualizarProjetoDTO("Projeto Atualizado", "Nova Desc");

        ProjetoDTO resultado = projetoService.atualizarProjeto(projetoCriado.id(), dto, dono);

        assertEquals("Projeto Atualizado", resultado.nome());
        assertEquals("Nova Desc", resultado.descricao());
    }

    @Test
    @DisplayName("Deve atualizar projeto com sucesso quando usuário for ADMIN")
    void atualizarProjeto_comSucesso_quandoAdmin() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(new CriarProjetoDTO("Projeto Original", "Desc", null), dono);
        AtualizarProjetoDTO dto = new AtualizarProjetoDTO("Atualizado pelo Admin", "Desc Admin");

        ProjetoDTO resultado = projetoService.atualizarProjeto(projetoCriado.id(), dto, admin);

        assertEquals("Atualizado pelo Admin", resultado.nome());
    }

    @Test
    @DisplayName("Deve lançar AcessoNegadoException ao atualizar se o usuário não for o dono nem ADMIN")
    void atualizarProjeto_lancaAcessoNegado_quandoNaoForDonoNemAdmin() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(
                new CriarProjetoDTO("Projeto Teste", "Desc", Set.of(membro.getId())), dono);
        AtualizarProjetoDTO dto = new AtualizarProjetoDTO("Tentativa", "Desc");

        assertThrows(AcessoNegadoException.class,
                () -> projetoService.atualizarProjeto(projetoCriado.id(), dto, membro));
    }

    @Test
    @DisplayName("Deve adicionar novos membros ao projeto")
    void adicionarMembros_comSucesso() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(
                new CriarProjetoDTO("Projeto Teste", "Desc", Set.of(membro.getId())), dono);
        GerenciarMembrosDTO dto = new GerenciarMembrosDTO(Set.of(estranho.getId()));

        ProjetoDTO resultado = projetoService.adicionarMembros(projetoCriado.id(), dto, dono);

        assertEquals(2, resultado.membros().size());
        assertTrue(resultado.membros().stream().anyMatch(m -> m.email().equals("estranho@email.com")));
    }

    @Test
    @DisplayName("Deve remover membro do projeto com sucesso")
    void removerMembro_comSucesso() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(
                new CriarProjetoDTO("Projeto Teste", "Desc", Set.of(membro.getId())), dono);

        ProjetoDTO resultado = projetoService.removerMembro(projetoCriado.id(), membro.getId(), dono);

        assertFalse(resultado.membros().stream().anyMatch(m -> m.email().equals("membro@email.com")));
    }

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException ao tentar remover o próprio dono da lista de membros")
    void removerMembro_lancaRegraDeNegocio_quandoTentarRemoverDono() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(new CriarProjetoDTO("Projeto Teste", "Desc", null), dono);

        assertThrows(RegraDeNegocioException.class,
                () -> projetoService.removerMembro(projetoCriado.id(), dono.getId(), dono));
    }

    @Test
    @DisplayName("Deve deletar projeto com sucesso quando usuário for o dono")
    void deletarProjeto_comSucesso_quandoDono() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(new CriarProjetoDTO("Projeto Teste", "Desc", null), dono);

        projetoService.deletarProjeto(projetoCriado.id(), dono);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> projetoService.buscarPorId(projetoCriado.id(), dono));
    }

    @Test
    @DisplayName("Deve lançar AcessoNegadoException ao tentar deletar projeto sem ser o dono nem ADMIN")
    void deletarProjeto_lancaAcessoNegado_quandoNaoForDonoNemAdmin() {
        ProjetoDTO projetoCriado = projetoService.criarProjeto(
                new CriarProjetoDTO("Projeto Teste", "Desc", Set.of(membro.getId())), dono);

        assertThrows(AcessoNegadoException.class,
                () -> projetoService.deletarProjeto(projetoCriado.id(), membro));
    }
}
