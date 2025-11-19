package br.com.fiap.to;

import jakarta.validation.constraints.*;
import java.util.List;

public class ProjetoTO {

    private Long id;

    @NotBlank(message = "O nome do projeto é obrigatório.")
    private String nome;

    @NotBlank(message = "A descrição do projeto é obrigatória.")
    private String descricao;


    @NotNull(message = "A complexidade é obrigatória.")
    @Min(1)
    @Max(5)
    private Integer complexidade;

    @NotNull(message = "O número mínimo de membros é obrigatório.")
    @Min(1)
    private Integer minMembros;

    @NotBlank(message = "A skill principal requerida é obrigatória.")
    private String skillPrincipalRequerida;

    
    @NotNull(message = "A quantidade ideal é obrigatória.")
    @Min(value = 1, message = "A equipe deve ter pelo menos um membro.")
    private Integer quantidadeIdeal;


    private String personalidadeDesejada;

    @NotNull(message = "A lista de requisitos é obrigatória.")
    private List<String> requisitos;

    // Construtores
    public ProjetoTO() {}

    public ProjetoTO(Long id, String nome, String descricao,
                     Integer complexidade, Integer minMembros,
                     String skillPrincipalRequerida, Integer quantidadeIdeal,
                     String personalidadeDesejada, List<String> requisitos) {

        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.complexidade = complexidade;
        this.minMembros = minMembros;
        this.skillPrincipalRequerida = skillPrincipalRequerida;
        this.quantidadeIdeal = quantidadeIdeal;
        this.personalidadeDesejada = personalidadeDesejada;
        this.requisitos = requisitos;
    }

    // ---- GETTERS & SETTERS ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getComplexidade() { return complexidade; }
    public void setComplexidade(Integer complexidade) { this.complexidade = complexidade; }

    public Integer getMinMembros() { return minMembros; }
    public void setMinMembros(Integer minMembros) { this.minMembros = minMembros; }

    public String getSkillPrincipalRequerida() { return skillPrincipalRequerida; }
    public void setSkillPrincipalRequerida(String skillPrincipalRequerida) {
        this.skillPrincipalRequerida = skillPrincipalRequerida;
    }

    public Integer getQuantidadeIdeal() { return quantidadeIdeal; }
    public void setQuantidadeIdeal(Integer quantidadeIdeal) { this.quantidadeIdeal = quantidadeIdeal; }

    public String getPersonalidadeDesejada() { return personalidadeDesejada; }
    public void setPersonalidadeDesejada(String personalidadeDesejada) {
        this.personalidadeDesejada = personalidadeDesejada;
    }

    public List<String> getRequisitos() { return requisitos; }
    public void setRequisitos(List<String> requisitos) { this.requisitos = requisitos; }
}
