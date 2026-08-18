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
import com.misael.gerenciador_tarefa.domain.repository.specification.TarefaSpecification;
import com.misael.gerenciador_tarefa.dto.relatorio.PrioridadeCountDTO;
import com.misael.gerenciador_tarefa.dto.relatorio.RelatorioProjetoDTO;
import com.misael.gerenciador_tarefa.dto.relatorio.StatusCountDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarStatusTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.AtualizarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.CriarTarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaDTO;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaFiltroDTO;
import com.misael.gerenciador_tarefa.exception.AcessoNegadoException;
import com.misael.gerenciador_tarefa.exception.RecursoNaoEncontradoException;
import com.misael.gerenciador_tarefa.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TarefaService {

    public static final int LIMITE_WIP_EM_ANDAMENTO = 5;

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public TarefaDTO criarTarefa(Long projetoId, CriarTarefaDTO dto, Usuario usuarioLogado) {
        Projeto projeto = buscarValidarProjeto(projetoId, usuarioLogado);

        Usuario responsavel = null;
        if (dto.responsavelId() != null) {
            responsavel = buscarValidarResponsavelNoProjeto(dto.responsavelId(), projeto);
        }

        Tarefa tarefa = Tarefa.builder()
                .titulo(dto.titulo())
                .descricao(dto.descricao())
                .prioridade(dto.prioridade())
                .status(StatusTarefa.A_FAZER)
                .prazo(dto.prazo())
                .projeto(projeto)
                .responsavel(responsavel)
                .build();

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);
        return TarefaDTO.fromEntity(tarefaSalva);
    }

    @Transactional(readOnly = true)
    public Page<TarefaDTO> listarTarefas(Long projetoId, TarefaFiltroDTO filtro, Pageable pageable,
            Usuario usuarioLogado) {
        buscarValidarProjeto(projetoId, usuarioLogado);

        Specification<Tarefa> spec = TarefaSpecification.comFiltros(projetoId, filtro);
        Page<Tarefa> tarefas = tarefaRepository.findAll(spec, pageable);

        return tarefas.map(TarefaDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public TarefaDTO buscarPorId(Long projetoId, Long tarefaId, Usuario usuarioLogado) {
        Tarefa tarefa = buscarValidarTarefa(projetoId, tarefaId, usuarioLogado);
        return TarefaDTO.fromEntity(tarefa);
    }

    @Transactional
    public TarefaDTO atualizarTarefa(Long projetoId, Long tarefaId, AtualizarTarefaDTO dto, Usuario usuarioLogado) {
        Tarefa tarefa = buscarValidarTarefa(projetoId, tarefaId, usuarioLogado);

        Usuario novoResponsavel = null;
        if (dto.responsavelId() != null) {
            novoResponsavel = buscarValidarResponsavelNoProjeto(dto.responsavelId(), tarefa.getProjeto());

            if (tarefa.getStatus() == StatusTarefa.EM_ANDAMENTO &&
                    (tarefa.getResponsavel() == null
                            || !tarefa.getResponsavel().getId().equals(novoResponsavel.getId()))) {
                validarLimiteWip(novoResponsavel);
            }
        }

        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setPrioridade(dto.prioridade());
        tarefa.setPrazo(dto.prazo());
        tarefa.setResponsavel(novoResponsavel);

        Tarefa tarefaAtualizada = tarefaRepository.save(tarefa);
        return TarefaDTO.fromEntity(tarefaAtualizada);
    }

    @Transactional
    public TarefaDTO atualizarStatus(Long projetoId, Long tarefaId, AtualizarStatusTarefaDTO dto,
            Usuario usuarioLogado) {
        Tarefa tarefa = buscarValidarTarefa(projetoId, tarefaId, usuarioLogado);
        StatusTarefa statusAtual = tarefa.getStatus();
        StatusTarefa novoStatus = dto.status();

        if (statusAtual == novoStatus) {
            return TarefaDTO.fromEntity(tarefa);
        }

        if (statusAtual == StatusTarefa.CONCLUIDA && novoStatus == StatusTarefa.A_FAZER) {
            throw new RegraDeNegocioException(
                    "Uma tarefa CONCLUIDA não pode voltar para A_FAZER (apenas para EM_ANDAMENTO)");
        }
        if (tarefa.getPrioridade() == PrioridadeTarefa.CRITICA && novoStatus == StatusTarefa.CONCLUIDA) {
            if (usuarioLogado.getPerfil() != PerfilUsuario.ADMIN) {
                throw new RegraDeNegocioException(
                        "Tarefas com prioridade CRITICA só podem ser concluídas por um usuário com perfil ADMIN");
            }
        }

        if (novoStatus == StatusTarefa.EM_ANDAMENTO && tarefa.getResponsavel() != null) {
            validarLimiteWip(tarefa.getResponsavel());
        }

        tarefa.setStatus(novoStatus);
        Tarefa tarefaAtualizada = tarefaRepository.save(tarefa);
        return TarefaDTO.fromEntity(tarefaAtualizada);
    }

    @Transactional
    public void deletarTarefa(Long projetoId, Long tarefaId, Usuario usuarioLogado) {
        Tarefa tarefa = buscarValidarTarefa(projetoId, tarefaId, usuarioLogado);
        tarefaRepository.delete(tarefa);
    }

    @Transactional(readOnly = true)
    public RelatorioProjetoDTO gerarRelatorioResumo(Long projetoId, Usuario usuarioLogado) {
        Projeto projeto = buscarValidarProjeto(projetoId, usuarioLogado);

        List<StatusCountDTO> statusCounts = tarefaRepository.countTarefasPorStatus(projetoId);
        List<PrioridadeCountDTO> prioridadeCounts = tarefaRepository.countTarefasPorPrioridade(projetoId);
        long totalTarefas = tarefaRepository.countByProjetoId(projetoId);

        Map<StatusTarefa, Long> porStatus = new EnumMap<>(StatusTarefa.class);
        for (StatusTarefa st : StatusTarefa.values()) {
            porStatus.put(st, 0L);
        }
        for (StatusCountDTO sc : statusCounts) {
            if (sc.status() != null) {
                porStatus.put(sc.status(), sc.total());
            }
        }

        Map<PrioridadeTarefa, Long> porPrioridade = new EnumMap<>(PrioridadeTarefa.class);
        for (PrioridadeTarefa pt : PrioridadeTarefa.values()) {
            porPrioridade.put(pt, 0L);
        }
        for (PrioridadeCountDTO pc : prioridadeCounts) {
            if (pc.prioridade() != null) {
                porPrioridade.put(pc.prioridade(), pc.total());
            }
        }

        return new RelatorioProjetoDTO(
                projeto.getId(),
                projeto.getNome(),
                porStatus,
                porPrioridade,
                totalTarefas);
    }

    private Projeto buscarValidarProjeto(Long projetoId, Usuario usuarioLogado) {
        Projeto projeto = projetoRepository.findByIdWithDetalhes(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId));

        boolean ehAdmin = usuarioLogado.getPerfil() == PerfilUsuario.ADMIN;
        if (!ehAdmin && !projeto.ehMembroOuDono(usuarioLogado)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar as tarefas deste projeto");
        }

        return projeto;
    }

    private Tarefa buscarValidarTarefa(Long projetoId, Long tarefaId, Usuario usuarioLogado) {
        buscarValidarProjeto(projetoId, usuarioLogado);

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não encontrada com ID: " + tarefaId));

        if (!tarefa.getProjeto().getId().equals(projetoId)) {
            throw new RegraDeNegocioException(
                    "A tarefa com ID " + tarefaId + " não pertence ao projeto com ID " + projetoId);
        }

        return tarefa;
    }

    private Usuario buscarValidarResponsavelNoProjeto(Long responsavelId, Projeto projeto) {
        Usuario responsavel = usuarioRepository.findById(responsavelId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Responsável não encontrado com ID: " + responsavelId));

        if (!projeto.ehMembroOuDono(responsavel)) {
            throw new RegraDeNegocioException(
                    "O usuário '" + responsavel.getNome() + "' não é membro do projeto '" + projeto.getNome() + "'");
        }

        return responsavel;
    }

    private void validarLimiteWip(Usuario responsavel) {
        long emAndamento = tarefaRepository.countByResponsavelIdAndStatus(responsavel.getId(),
                StatusTarefa.EM_ANDAMENTO);
        if (emAndamento >= LIMITE_WIP_EM_ANDAMENTO) {
            throw new RegraDeNegocioException(
                    "Limite de trabalho em progresso (WIP) atingido: o usuário '" + responsavel.getNome() +
                            "' já possui " + LIMITE_WIP_EM_ANDAMENTO + " tarefas com status EM_ANDAMENTO");
        }
    }
}
