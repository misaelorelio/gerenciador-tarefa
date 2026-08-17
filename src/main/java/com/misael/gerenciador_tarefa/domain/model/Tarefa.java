package com.misael.gerenciador_tarefa.domain.model;

import com.misael.gerenciador_tarefa.domain.enums.PrioridadeTarefa;
import com.misael.gerenciador_tarefa.domain.enums.StatusTarefa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_tarefas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTarefa status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeTarefa prioridade;

    @Column(name = "prazo")
    private LocalDateTime prazo;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (this.dataCriacao == null) {
            this.dataCriacao = agora;
        }
        this.dataAtualizacao = agora;
        if (this.status == null) {
            this.status = StatusTarefa.A_FAZER;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
