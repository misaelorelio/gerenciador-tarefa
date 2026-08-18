package com.misael.gerenciador_tarefa.dto.auth;

import com.misael.gerenciador_tarefa.dto.usuario.UsuarioDTO;

public record TokenDTO(
    String token,
    String tipo,
    UsuarioDTO usuario
) {
    public TokenDTO(String token, UsuarioDTO usuario) {
        this(token, "Bearer", usuario);
    }
}
