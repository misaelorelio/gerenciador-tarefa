package com.misael.gerenciador_tarefa.service;

import com.misael.gerenciador_tarefa.domain.enums.PerfilUsuario;
import com.misael.gerenciador_tarefa.domain.repository.UsuarioRepository;
import com.misael.gerenciador_tarefa.dto.auth.LoginDTO;
import com.misael.gerenciador_tarefa.dto.auth.TokenDTO;
import com.misael.gerenciador_tarefa.dto.usuario.CriarUsuarioDTO;
import com.misael.gerenciador_tarefa.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso e retornar token JWT")
    void registrar_comSucesso() {
        CriarUsuarioDTO dto = new CriarUsuarioDTO("Carlos Admin", "carlos@email.com", "senha123", PerfilUsuario.ADMIN);

        TokenDTO resultado = authService.registrar(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.token());
        assertEquals("Bearer", resultado.tipo());
        assertEquals("carlos@email.com", resultado.usuario().email());
        assertEquals(PerfilUsuario.ADMIN, resultado.usuario().perfil());
        assertTrue(usuarioRepository.existsByEmail("carlos@email.com"));
    }

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException ao tentar registrar usuário com e-mail duplicado")
    void registrar_lancaExcecao_quandoEmailDuplicado() {
        CriarUsuarioDTO dto = new CriarUsuarioDTO("Carlos Admin", "carlos@email.com", "senha123", PerfilUsuario.ADMIN);
        authService.registrar(dto);

        CriarUsuarioDTO dtoDuplicado = new CriarUsuarioDTO("Outro Carlos", "carlos@email.com", "senha456", PerfilUsuario.MEMBRO);

        assertThrows(RegraDeNegocioException.class, () -> authService.registrar(dtoDuplicado));
    }

    @Test
    @DisplayName("Deve realizar login com credenciais corretas e retornar token")
    void login_comSucesso() {
        authService.registrar(new CriarUsuarioDTO("Maria Membro", "maria@email.com", "senha123", PerfilUsuario.MEMBRO));

        LoginDTO loginDTO = new LoginDTO("maria@email.com", "senha123");
        TokenDTO resultado = authService.login(loginDTO);

        assertNotNull(resultado);
        assertNotNull(resultado.token());
        assertEquals("maria@email.com", resultado.usuario().email());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException ao tentar login com senha incorreta")
    void login_lancaExcecao_quandoSenhaIncorreta() {
        authService.registrar(new CriarUsuarioDTO("Maria Membro", "maria@email.com", "senha123", PerfilUsuario.MEMBRO));

        LoginDTO loginDTO = new LoginDTO("maria@email.com", "senhaErrada");

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));
    }
}
