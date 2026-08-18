package com.misael.gerenciador_tarefa.service;

import com.misael.gerenciador_tarefa.config.security.JwtService;
import com.misael.gerenciador_tarefa.domain.model.Usuario;
import com.misael.gerenciador_tarefa.domain.repository.UsuarioRepository;
import com.misael.gerenciador_tarefa.dto.auth.LoginDTO;
import com.misael.gerenciador_tarefa.dto.auth.TokenDTO;
import com.misael.gerenciador_tarefa.dto.usuario.CriarUsuarioDTO;
import com.misael.gerenciador_tarefa.dto.usuario.UsuarioDTO;
import com.misael.gerenciador_tarefa.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public TokenDTO registrar(CriarUsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraDeNegocioException("Já existe um usuário cadastrado com o e-mail: " + dto.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil())
                .build();

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        String token = jwtService.gerarToken(usuarioSalvo);

        return new TokenDTO(token, UsuarioDTO.fromEntity(usuarioSalvo));
    }

    public TokenDTO login(LoginDTO dto) {
        var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        Authentication authentication = authenticationManager.authenticate(authToken);

        Usuario usuario = (Usuario) authentication.getPrincipal();
        String token = jwtService.gerarToken(usuario);

        return new TokenDTO(token, UsuarioDTO.fromEntity(usuario));
    }
}
