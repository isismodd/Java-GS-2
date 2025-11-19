package br.com.fiap.bo;

import br.com.fiap.dao.ProjetoDAO;
import br.com.fiap.to.ProjetoTO;

import java.util.List;

public class ProjetoBO {

    private ProjetoDAO projetoDAO = new ProjetoDAO();

    /**
     * Buscar projeto por ID
     */
    public ProjetoTO findById(Long id) {
        if (id == null || id <= 0) return null;
        return projetoDAO.findById(id);
    }

    /**
     * Listar todos os projetos
     */
    public List<ProjetoTO> findAll() {
        return projetoDAO.findAll();
    }

    /**
     * Validar e salvar projeto
     */
    public ProjetoTO save(ProjetoTO projeto) {

        // Nome
        if (projeto.getNome() == null || projeto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do projeto inválido.");
        }

        // Complexidade (1 a 5)
        if (projeto.getComplexidade() < 1 || projeto.getComplexidade() > 5) {
            throw new IllegalArgumentException("Complexidade deve estar entre 1 e 5.");
        }

        // Habilidades requeridas
        if (projeto.getRequisitos() == null || projeto.getRequisitos().isEmpty()) {
            throw new IllegalArgumentException("Projeto deve possuir ao menos um requisito.");
        }

        // Membros ideais
        if (projeto.getQuantidadeIdeal() <= 0) {
            throw new IllegalArgumentException("Quantidade ideal inválida.");
        }

        return projetoDAO.save(projeto);
    }


    /**
     * Atualizar projeto
     */
    public ProjetoTO update(ProjetoTO projeto) {
        if (projeto.getId() == null || projeto.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        return save(projeto);
    }

    /**
     * Excluir projeto
     */
    public boolean delete(Long id) {
        if (id == null || id <= 0) return false;
        return projetoDAO.delete(id);
    }

}
