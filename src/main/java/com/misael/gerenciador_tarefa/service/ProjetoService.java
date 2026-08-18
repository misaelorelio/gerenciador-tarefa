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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ProjetoDTO criarProjeto(CriarProjetoDTO dto, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AcessoNegadoException("Apenas administradores podem criar projetos");
        }

        Set<Usuario> membros = new HashSet<>();
        if (dto.membrosIds() != null && !dto.membrosIds().isEmpty()) {
            List<Usuario> usuariosEncontrados = usuarioRepository.findAllById(dto.membrosIds());
            membros.addAll(usuariosEncontrados);
        }

        Projeto projeto = Projeto.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .dono(usuarioLogado)
                .membros(membros)
                .build();

        Projeto projetoSalvo = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(projetoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProjetoDTO> listarProjetos(Usuario usuarioLogado) {
        List<Projeto> projetos = projetoRepository.findProjetosByUsuario(usuarioLogado);
        return projetos.stream()
                .map(ProjetoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjetoDTO buscarPorId(Long id, Usuario usuarioLogado) {
        Projeto projeto = buscarEntidadeProjetoPorId(id, usuarioLogado);
        return ProjetoDTO.fromEntity(projeto);
    }

    @Transactional
    public ProjetoDTO atualizarProjeto(Long id, AtualizarProjetoDTO dto, Usuario usuarioLogado) {
        Projeto projeto = buscarEntidadeProjetoPorId(id, usuarioLogado);
        validarPermissaoDonoOuAdmin(projeto, usuarioLogado, "Apenas o dono do projeto pode editá-lo");

        projeto.setNome(dto.nome());
        projeto.setDescricao(dto.descricao());

        Projeto projetoAtualizado = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(projetoAtualizado);
    }

    @Transactional
    public ProjetoDTO adicionarMembros(Long id, GerenciarMembrosDTO dto, Usuario usuarioLogado) {
        Projeto projeto = buscarEntidadeProjetoPorId(id, usuarioLogado);
        validarPermissaoDonoOuAdmin(projeto, usuarioLogado, "Apenas o dono do projeto pode gerenciar membros");

        List<Usuario> novosMembros = usuarioRepository.findAllById(dto.membrosIds());
        novosMembros.forEach(projeto::adicionarMembro);

        Projeto projetoAtualizado = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(projetoAtualizado);
    }

    @Transactional
    public ProjetoDTO removerMembro(Long id, Long usuarioId, Usuario usuarioLogado) {
        Projeto projeto = buscarEntidadeProjetoPorId(id, usuarioLogado);
        validarPermissaoDonoOuAdmin(projeto, usuarioLogado, "Apenas o dono do projeto pode gerenciar membros");

        if (projeto.getDono().getId().equals(usuarioId)) {
            throw new RegraDeNegocioException("Não é permitido remover o dono do projeto da lista de membros");
        }

        projeto.getMembros().removeIf(membro -> membro.getId().equals(usuarioId));

        Projeto projetoAtualizado = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(projetoAtualizado);
    }

    @Transactional
    public void deletarProjeto(Long id, Usuario usuarioLogado) {
        Projeto projeto = buscarEntidadeProjetoPorId(id, usuarioLogado);
        validarPermissaoDonoOuAdmin(projeto, usuarioLogado, "Apenas o dono do projeto pode excluí-lo");

        projetoRepository.delete(projeto);
    }

    @Transactional(readOnly = true)
    public Projeto buscarEntidadeProjetoPorId(Long id, Usuario usuarioLogado) {
        Projeto projeto = projetoRepository.findByIdWithDetalhes(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado com ID: " + id));

        boolean ehAdmin = usuarioLogado.getPerfil() == PerfilUsuario.ADMIN;
        if (!ehAdmin && !projeto.ehMembroOuDono(usuarioLogado)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar este projeto");
        }

        return projeto;
    }

    private void validarPermissaoDonoOuAdmin(Projeto projeto, Usuario usuarioLogado, String mensagemErro) {
        if (usuarioLogado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AcessoNegadoException("Apenas administradores podem gerenciar projetos e membros");
        }
    }
}
