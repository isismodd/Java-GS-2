package br.com.fiap.dao;

import br.com.fiap.to.EquipeTO;
import br.com.fiap.to.ColaboradorTO;
import br.com.fiap.to.ProjetoTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeDAO {

    private final String TABLE_NAME = "EQUIPE";

    private ColaboradorDAO colaboradorDAO = new ColaboradorDAO();
    private ProjetoDAO projetoDAO = new ProjetoDAO();

    /**
     * Busca equipe pelo ID, incluindo Projeto e Integrantes
     */
    public EquipeTO findById(Long idEquipe) {
        String sql = "SELECT id_equipe, nome, id_projeto, quantidade_membros FROM EQUIPE WHERE id_equipe = ?";
        EquipeTO equipe = null;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idEquipe);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    equipe = new EquipeTO();
                    equipe.setId(rs.getLong("id_equipe"));
                    equipe.setNome(rs.getString("nome"));
                    Long idProjeto = rs.getLong("id_projeto");
                    ProjetoTO projeto = projetoDAO.findById(idProjeto); // requer ProjetoDAO.findById
                    equipe.setProjeto(projeto);
                    equipe.setQuantidadeMembros(rs.getInt("quantidade_membros"));

                    // carregar integrantes
                    equipe.setIntegrantes(findIntegrantesByEquipe(equipe.getId()));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro no findById (EquipeDAO): " + e.getMessage());
        }

        return equipe;
    }

    /**
     * Lista todas as equipes (sem povoar detalhes pesados de integrantes, mas popula projeto)
     */
    public List<EquipeTO> findAll() {
        List<EquipeTO> lista = new ArrayList<>();
        String sql = "SELECT id_equipe, nome, id_projeto, quantidade_membros FROM EQUIPE ORDER BY id_equipe";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EquipeTO equipe = new EquipeTO();
                equipe.setId(rs.getLong("id_equipe"));
                equipe.setNome(rs.getString("nome"));
                Long idProjeto = rs.getLong("id_projeto");
                ProjetoTO projeto = projetoDAO.findById(idProjeto);
                equipe.setProjeto(projeto);
                equipe.setQuantidadeMembros(rs.getInt("quantidade_membros"));

                // opcional: carregar integrantes se quiser (comentado para performance)
                // equipe.setIntegrantes(findIntegrantesByEquipe(equipe.getId()));

                lista.add(equipe);
            }

        } catch (SQLException e) {
            System.out.println("Erro no findAll (EquipeDAO): " + e.getMessage());
        }

        return lista;
    }

    /**
     * Salva equipe e seus integrantes (espera EquipeTO com projeto e integrantes preenchidos)
     */
    public EquipeTO save(EquipeTO equipe) {
        String sql = "INSERT INTO EQUIPE (nome, id_projeto, quantidade_membros) VALUES (?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_equipe"})) {

            ps.setString(1, equipe.getNome());
            ps.setLong(2, equipe.getProjeto().getId());
            ps.setInt(3, equipe.getQuantidadeMembros());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        equipe.setId(rs.getLong(1));
                    }
                }

                // salvar integrantes (tabela associativa)
                saveIntegrantes(equipe);
                return equipe;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao salvar equipe: " + e.getMessage());
        }
        return null;
    }

    /**
     * Atualiza dados básicos da equipe e substitui integrantes
     */
    public EquipeTO update(EquipeTO equipe) {
        String sql = "UPDATE EQUIPE SET nome = ?, id_projeto = ?, quantidade_membros = ? WHERE id_equipe = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, equipe.getNome());
            ps.setLong(2, equipe.getProjeto().getId());
            ps.setInt(3, equipe.getQuantidadeMembros());
            ps.setLong(4, equipe.getId());

            if (ps.executeUpdate() > 0) {
                // substituir integrantes antigos
                deleteIntegrantesByEquipe(equipe.getId());
                saveIntegrantes(equipe);
                return equipe;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar equipe: " + e.getMessage());
        }

        return null;
    }

    /**
     * Exclui uma equipe (e integrantes pela FK/ou manualmente)
     */
    public boolean delete(Long idEquipe) {
        String sql = "DELETE FROM EQUIPE WHERE id_equipe = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idEquipe);
            boolean sucesso = ps.executeUpdate() > 0;
            // se integridade referencial não estiver com cascade, remover integrantes manualmente
            deleteIntegrantesByEquipe(idEquipe);
            return sucesso;

        } catch (SQLException e) {
            System.out.println("Erro ao excluir equipe: " + e.getMessage());
        }

        return false;
    }

    /**
     * Encontra equipes por projeto
     */
    public List<EquipeTO> findByProjeto(Long idProjeto) {
        List<EquipeTO> lista = new ArrayList<>();
        String sql = "SELECT id_equipe FROM EQUIPE WHERE id_projeto = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idProjeto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long idEq = rs.getLong("id_equipe");
                    EquipeTO e = findById(idEq); // reaproveita método para popular
                    if (e != null) lista.add(e);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro no findByProjeto: " + e.getMessage());
        }

        return lista;
    }

    // -------------------------
    // Métodos auxiliares
    // -------------------------

    private void saveIntegrantes(EquipeTO equipe) throws SQLException {
        if (equipe.getIntegrantes() == null || equipe.getIntegrantes().isEmpty()) return;

        String insert = "INSERT INTO EQUIPE_INTEGRANTE (id_equipe, id_colaborador) VALUES (?, ?)";

        try (Connection con = ConnectionFactory.getConnection()) {
            for (ColaboradorTO c : equipe.getIntegrantes()) {
                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    ps.setLong(1, equipe.getId());
                    ps.setLong(2, c.getId());
                    ps.executeUpdate();
                }
            }
        }
    }

    private void deleteIntegrantesByEquipe(Long idEquipe) {
        String delete = "DELETE FROM EQUIPE_INTEGRANTE WHERE id_equipe = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(delete)) {

            ps.setLong(1, idEquipe);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar integrantes: " + e.getMessage());
        }
    }

    private List<ColaboradorTO> findIntegrantesByEquipe(Long idEquipe) {
        List<ColaboradorTO> integrantes = new ArrayList<>();
        String sql = "SELECT id_colaborador FROM EQUIPE_INTEGRANTE WHERE id_equipe = ? ORDER BY id_colaborador";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idEquipe);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long idCol = rs.getLong("id_colaborador");
                    ColaboradorTO c = colaboradorDAO.findById(idCol); // requer ColaboradorDAO.findById
                    if (c != null) integrantes.add(c);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar integrantes por equipe: " + e.getMessage());
        }

        return integrantes;
    }
}
