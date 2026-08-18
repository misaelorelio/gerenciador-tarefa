package com.misael.gerenciador_tarefa.dto.projeto;

import com.misael.gerenciador_tarefa.domain.model.Projeto;
import com.misael.gerenciador_tarefa.dto.usuario.UsuarioResumoDTO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ProjetoDTO(
    Long id,
    String nome,
    String descricao,
    UsuarioResumoDTO dono,
    Set<UsuarioResumoDTO> membros,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao
) {
    public static ProjetoDTO fromEntity(Projeto projeto) {
        if (projeto == null) return null;

        Set<UsuarioResumoDTO> membrosDTO = projeto.getMembros() != null
                ? projeto.getMembros().stream().map(UsuarioResumoDTO::fromEntity).collect(Collectors.toSet())
                : Set.of();

        return new ProjetoDTO(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDescricao(),
                UsuarioResumoDTO.fromEntity(projeto.getDono()),
                membrosDTO,
                projeto.getDataCriacao(),
                projeto.getDataAtualizacao()
        );
    }
}
