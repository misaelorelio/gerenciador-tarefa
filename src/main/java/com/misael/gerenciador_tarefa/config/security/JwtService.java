package com.misael.gerenciador_tarefa.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.misael.gerenciador_tarefa.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration-hours:24}")
    private int expirationHours;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("gerenciador-tarefas-api")
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId())
                    .withClaim("perfil", usuario.getPerfil().name())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validarTokenERecuperarSubject(String token) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("gerenciador-tarefas-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant dataExpiracao() {
        return Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }
}
