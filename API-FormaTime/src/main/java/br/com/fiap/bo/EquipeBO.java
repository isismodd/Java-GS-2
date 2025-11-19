package br.com.fiap.bo;

import br.com.fiap.dao.ColaboradorDAO;
import br.com.fiap.dao.ProjetoDAO;
import br.com.fiap.to.ColaboradorTO;
import br.com.fiap.to.ProjetoTO;
import br.com.fiap.to.EquipeTO;

import java.util.*;
import java.util.stream.Collectors;

public class EquipeBO {

    private ColaboradorDAO colaboradorDAO = new ColaboradorDAO();
    private ProjetoDAO projetoDAO = new ProjetoDAO();

 
    private double calcularScore(ColaboradorTO colab, ProjetoTO projeto) {
        double score = 0;

        // Pontuação por habilidades
        for (String req : projeto.getRequisitos()) {
            if (colab.getHabilidades().contains(req)) {
                score += 10;  // igual ao Python
            }
        }

        // Bônus por experiência
        score += colab.getExperiencia() * 2;

        // Bônus por personalidade
        if (colab.getPersonalidade().equalsIgnoreCase(projeto.getPersonalidadeDesejada())) {
            score += 8;
        }

        return score;
    }

    /**
     * Formar equipe igual ao Python
     */
    public EquipeTO formarEquipe(Long idProjeto) {

        ProjetoTO projeto = projetoDAO.findById(idProjeto);
        if (projeto == null) return null; // Retorna null se o projeto não existir (404)

        List<ColaboradorTO> colaboradores = colaboradorDAO.findAll();

        // Ranking
        List<ColaboradorTO> ordenados = colaboradores.stream()
                .sorted((a, b) -> Double.compare(
                        calcularScore(b, projeto),
                        calcularScore(a, projeto)
                ))
                .collect(Collectors.toList());

        // Seleção final: Limita à quantidade ideal
        List<ColaboradorTO> escolhidos = ordenados.stream()
                .limit(projeto.getQuantidadeIdeal())
                .collect(Collectors.toList());

        // Verifica se a quantidade de membros escolhidos atende ao mínimo exigido pelo projeto.
        if (escolhidos.size() < projeto.getMinMembros()) {
            // Se a quantidade for insuficiente, retorna null.
            // O EquipeResource transformará isso em status 404
            return null;
        }

        // Montar objeto final 
        EquipeTO equipe = new EquipeTO();
        equipe.setProjeto(projeto);
        equipe.setIntegrantes(escolhidos);

        return equipe;
    }
}
