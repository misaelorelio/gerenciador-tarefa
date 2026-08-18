package com.misael.gerenciador_tarefa.domain.repository;

import com.misael.gerenciador_tarefa.domain.model.Projeto;
import com.misael.gerenciador_tarefa.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query("SELECT DISTINCT p FROM Projeto p LEFT JOIN FETCH p.dono LEFT JOIN FETCH p.membros WHERE p.id = :id")
    Optional<Projeto> findByIdWithDetalhes(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Projeto p LEFT JOIN p.membros m WHERE p.dono = :usuario OR m = :usuario")
    List<Projeto> findProjetosByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(p) > 0 FROM Projeto p LEFT JOIN p.membros m WHERE p.id = :projetoId AND (p.dono = :usuario OR m = :usuario)")
    boolean usuarioTemAcessoAoProjeto(@Param("projetoId") Long projetoId, @Param("usuario") Usuario usuario);
}
