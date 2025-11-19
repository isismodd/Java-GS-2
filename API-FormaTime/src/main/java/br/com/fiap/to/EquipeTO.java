package br.com.fiap.to;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeTO {

    private Long id;

    @NotBlank(message = "O nome da equipe é obrigatório.")
    private String nome;

    // Projeto completo associado (não só id)
    @NotNull(message = "O projeto da equipe é obrigatório.")
    private ProjetoTO projeto;

    @NotNull(message = "A quantidade de membros é obrigatória.")
    @Min(value = 1, message = "A equipe deve ter pelo menos 1 membro.")
    private Integer quantidadeMembros;

    // Lista de integrantes (ColaboradorTO)
    private List<ColaboradorTO> integrantes = new ArrayList<>();

    // Construtores
    public EquipeTO() {
    }

    public EquipeTO(Long id, String nome, ProjetoTO projeto, Integer quantidadeMembros, List<ColaboradorTO> integrantes) {
        this.id = id;
        this.nome = nome;
        this.projeto = projeto;
        this.quantidadeMembros = quantidadeMembros;
        this.integrantes = integrantes;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public ProjetoTO getProjeto() {
        return projeto;
    }

    public void setProjeto(ProjetoTO projeto) {
        this.projeto = projeto;
    }

    public Integer getQuantidadeMembros() {
        return quantidadeMembros;
    }

    public void setQuantidadeMembros(Integer quantidadeMembros) {
        this.quantidadeMembros = quantidadeMembros;
    }

    public List<ColaboradorTO> getIntegrantes() {
        return integrantes;
    }

    public void setIntegrantes(List<ColaboradorTO> integrantes) {
        this.integrantes = integrantes;
    }
}
