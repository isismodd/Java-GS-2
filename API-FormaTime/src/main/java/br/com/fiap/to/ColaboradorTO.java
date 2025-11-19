package br.com.fiap.to;

import jakarta.validation.constraints.*;
import java.util.List;

public class ColaboradorTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotNull(message = "A idade é obrigatória.")
    @Min(value = 16, message = "Idade mínima é 16 anos.")
    private Integer idade;

    @NotBlank(message = "A personalidade é obrigatória.")
    private String personalidade;

    @NotNull(message = "A experiência é obrigatória.")
    @Min(value = 0, message = "Experiência não pode ser negativa.")
    private Integer experiencia;

    @NotNull(message = "A lista de habilidades não pode ser nula.")
    private List<String> habilidades;

    public ColaboradorTO() {}

    public ColaboradorTO(Long id, String nome, Integer idade, String personalidade, Integer experiencia, List<String> habilidades) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.personalidade = personalidade;
        this.experiencia = experiencia;
        this.habilidades = habilidades;
    }

    // GETTERS / SETTERS
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

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public String getPersonalidade() {
        return personalidade;
    }

    public void setPersonalidade(String personalidade) {
        this.personalidade = personalidade;
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(Integer experiencia) {
        this.experiencia = experiencia;
    }

    public List<String> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<String> habilidades) {
        this.habilidades = habilidades;
    }
}
