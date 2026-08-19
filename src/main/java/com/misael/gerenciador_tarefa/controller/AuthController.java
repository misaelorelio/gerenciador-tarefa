package com.misael.gerenciador_tarefa.controller;

import com.misael.gerenciador_tarefa.dto.auth.LoginDTO;
import com.misael.gerenciador_tarefa.dto.auth.TokenDTO;
import com.misael.gerenciador_tarefa.dto.usuario.CriarUsuarioDTO;
import com.misael.gerenciador_tarefa.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registro e autenticação de usuários na plataforma")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @Operation(summary = "Cadastrar novo usuário", description = "Cria um novo usuário com perfil ADMIN ou MEMBRO e retorna o token JWT para autenticação imediata.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "422", description = "E-mail já cadastrado no sistema")
    })
    public ResponseEntity<TokenDTO> registrar(@RequestBody @Valid CriarUsuarioDTO dto) {
        TokenDTO tokenDTO = authService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida as credenciais (e-mail e senha) e retorna o token JWT Bearer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        TokenDTO tokenDTO = authService.login(dto);
        return ResponseEntity.ok(tokenDTO);
    }
}
