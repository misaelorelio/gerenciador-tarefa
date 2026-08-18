package com.misael.gerenciador_tarefa.dto.usuario;

import com.misael.gerenciador_tarefa.domain.enums.PerfilUsuario;
import com.misael.gerenciador_tarefa.domain.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioDTO(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil,
    LocalDateTime dataCriacao
) {
    public static UsuarioDTO fromEntity(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil(),
            usuario.getDataCriacao()
        );
    }
}
