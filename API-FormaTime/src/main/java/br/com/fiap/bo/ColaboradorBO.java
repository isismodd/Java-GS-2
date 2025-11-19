package br.com.fiap.bo;

import br.com.fiap.dao.ColaboradorDAO;
import br.com.fiap.to.ColaboradorTO;

import java.util.List;

public class ColaboradorBO {

    private ColaboradorDAO colaboradorDAO = new ColaboradorDAO();

    /**
     * Busca colaborador por ID
     */
    public ColaboradorTO findById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return colaboradorDAO.findById(id);
    }

    /**
     * Lista todos os colaboradores
     */
    public List<ColaboradorTO> findAll() {
        return colaboradorDAO.findAll();
    }

    /**
     * Valida dados comuns (save/update)
     */
    private void validar(ColaboradorTO colab) {

        if (colab == null)
            throw new IllegalArgumentException("Colaborador inválido.");

        if (colab.getNome() == null || colab.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome inválido.");
        }

        if (colab.getIdade() < 16 || colab.getIdade() > 90) {
            throw new IllegalArgumentException("Idade fora do intervalo permitido.");
        }

        if (colab.getPersonalidade() == null || colab.getPersonalidade().isEmpty()) {
            throw new IllegalArgumentException("Personalidade não informada.");
        }

        if (colab.getHabilidades() == null || colab.getHabilidades().isEmpty()) {
            throw new IllegalArgumentException("Habilidades não podem ser vazias.");
        }

        // Remover duplicadas
        colab.setHabilidades(colab.getHabilidades().stream().distinct().toList());

        if (colab.getExperiencia() < 0) {
            throw new IllegalArgumentException("Experiência inválida.");
        }
    }

    /**
     * Salvar colaborador
     */
    public ColaboradorTO save(ColaboradorTO colab) {
        validar(colab);
        return colaboradorDAO.save(colab);
    }

    /**
     * Atualizar colaborador
     */
    public ColaboradorTO update(ColaboradorTO colab) {

        if (colab.getId() == null || colab.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido para atualização.");
        }

        validar(colab); // mesma validação do save()

        return colaboradorDAO.update(colab);
    }

    /**
     * Excluir colaborador
     */
    public boolean delete(Long id) {
        if (id == null || id <= 0) return false;
        return colaboradorDAO.delete(id);
    }

}
