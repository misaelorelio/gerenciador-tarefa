package com.misael.gerenciador_tarefa.controller;

import com.misael.gerenciador_tarefa.dto.auth.LoginDTO;
import com.misael.gerenciador_tarefa.dto.auth.TokenDTO;
import com.misael.gerenciador_tarefa.dto.usuario.CriarUsuarioDTO;
import com.misael.gerenciador_tarefa.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<TokenDTO> registrar(@RequestBody @Valid CriarUsuarioDTO dto) {
        TokenDTO tokenDTO = authService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        TokenDTO tokenDTO = authService.login(dto);
        return ResponseEntity.ok(tokenDTO);
    }
}
