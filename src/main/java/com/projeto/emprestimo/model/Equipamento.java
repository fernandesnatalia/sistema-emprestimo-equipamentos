package com.projeto.emprestimo.model;

import com.projeto.emprestimo.exception.ConflitoException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O patrimônio é obrigatório")
    @Column(nullable = false, unique = true)
    private String patrimonio;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "O tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEquipamento tipo;

    @Column(nullable = false)
    private boolean disponivel = true;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getPatrimonio() {return patrimonio; }

    public void setPatrimonio(String patrimonio) { this.patrimonio = patrimonio; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public TipoEquipamento getTipo() { return tipo; }

    public void setTipo(TipoEquipamento tipo) { this.tipo = tipo; }

    public boolean isDisponivel() { return disponivel; }

    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    public void devolver() { this.disponivel = true; }

    public void emprestar() {
        if (!disponivel) {
            throw new ConflitoException("Equipamento indisponível");
        }

        this.disponivel = false;
    }

    public Equipamento() {
    }
}