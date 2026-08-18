package com.misael.gerenciador_tarefa.domain.repository.specification;

import com.misael.gerenciador_tarefa.domain.model.Tarefa;
import com.misael.gerenciador_tarefa.dto.tarefa.TarefaFiltroDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TarefaSpecification {

    public static Specification<Tarefa> comFiltros(Long projetoId, TarefaFiltroDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("projeto").get("id"), projetoId));

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (filtro.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filtro.status()));
            }

            if (filtro.prioridade() != null) {
                predicates.add(criteriaBuilder.equal(root.get("prioridade"), filtro.prioridade()));
            }

            if (filtro.responsavelId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("responsavel").get("id"), filtro.responsavelId()));
            }

            if (filtro.prazoInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("prazo"), filtro.prazoInicio()));
            }
            if (filtro.prazoFim() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("prazo"), filtro.prazoFim()));
            }

            if (filtro.busca() != null && !filtro.busca().trim().isEmpty()) {
                String termo = "%" + filtro.busca().trim().toLowerCase() + "%";
                Predicate tituloMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), termo);
                Predicate descricaoMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), termo);
                predicates.add(criteriaBuilder.or(tituloMatch, descricaoMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
