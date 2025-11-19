package br.com.fiap.dao;

import br.com.fiap.to.ProjetoTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjetoDAO {

    private final String TABLE_NAME = "PROJETO";

    private ProjetoTO mapResultSetToTO(ResultSet rs) throws SQLException {
        ProjetoTO p = new ProjetoTO();

        p.setId(rs.getLong("id_projeto"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setComplexidade(rs.getInt("complexidade"));
        p.setMinMembros(rs.getInt("min_membros"));
        p.setSkillPrincipalRequerida(rs.getString("skill_principal_requerida"));
        p.setQuantidadeIdeal(rs.getInt("quantidade_ideal"));
        p.setPersonalidadeDesejada(rs.getString("personalidade_desejada"));

        // Carregar lista de descrições da tabela REQUISITO
        p.setRequisitos(findRequisitos(p.getId()));

        return p;
    }

    public List<String> findRequisitos(Long idProjeto) {
        List<String> lista = new ArrayList<>();

        String sql = """
            SELECT descricao
            FROM REQUISITO
            WHERE projeto_id = ?
        """;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idProjeto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("descricao"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar requisitos: " + e.getMessage());
        }

        return lista;
    }

    public ProjetoTO findById(Long id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id_projeto = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToTO(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erro no findById: " + e.getMessage());
        }

        return null;
    }

    public List<ProjetoTO> findAll() {
        List<ProjetoTO> lista = new ArrayList<>();

        String sql = "SELECT * FROM PROJETO ORDER BY nome";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // 1) Primeiro lê tudo do ResultSet
            while (rs.next()) {
                ProjetoTO p = new ProjetoTO();

                p.setId(rs.getLong("id_projeto"));
                p.setNome(rs.getString("nome"));
                p.setDescricao(rs.getString("descricao"));
                p.setComplexidade(rs.getInt("complexidade"));
                p.setMinMembros(rs.getInt("min_membros"));
                p.setSkillPrincipalRequerida(rs.getString("skill_principal_requerida"));
                p.setQuantidadeIdeal(rs.getInt("quantidade_ideal"));
                p.setPersonalidadeDesejada(rs.getString("personalidade_desejada"));

                lista.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Erro no findAll: " + e.getMessage());
        }

        // 2) Depois, para cada projeto, consultar os requisitos (outra conexão)
        for (ProjetoTO p : lista) {
            p.setRequisitos(findRequisitos(p.getId()));
        }

        return lista;
    }


    public ProjetoTO save(ProjetoTO p) {
        String sql = """
            INSERT INTO PROJETO
            (nome, descricao, complexidade, min_membros, skill_principal_requerida,
             quantidade_ideal, personalidade_desejada)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_projeto"})) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setInt(3, p.getComplexidade());
            ps.setInt(4, p.getMinMembros());
            ps.setString(5, p.getSkillPrincipalRequerida());
            ps.setInt(6, p.getQuantidadeIdeal());
            ps.setString(7, p.getPersonalidadeDesejada());

            if (ps.executeUpdate() > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getLong(1));
                }

                saveRequisitos(p);
                return p;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao salvar projeto: " + e.getMessage());
        }

        return null;
    }

    private void saveRequisitos(ProjetoTO p) throws SQLException {

        // Se não tiver requisitos, não faz nada
        if (p.getRequisitos() == null) return;

        String delete = "DELETE FROM REQUISITO WHERE projeto_id = ?";
        String insert = """
            INSERT INTO REQUISITO (projeto_id, descricao)
            VALUES (?, ?)
        """;

        try (Connection con = ConnectionFactory.getConnection()) {

            // Apagar requisitos anteriores
            try (PreparedStatement ps = con.prepareStatement(delete)) {
                ps.setLong(1, p.getId());
                ps.executeUpdate();
            }

            // Inserir novos requisitos (descrições)
            for (String descricao : p.getRequisitos()) {
                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    ps.setLong(1, p.getId());
                    ps.setString(2, descricao);
                    ps.executeUpdate();
                }
            }
        }
    }

    public ProjetoTO update(ProjetoTO p) {
        String sql = """
            UPDATE PROJETO
            SET nome=?, descricao=?, complexidade=?, min_membros=?,
                skill_principal_requerida=?, quantidade_ideal=?, personalidade_desejada=?
            WHERE id_projeto=?
        """;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setInt(3, p.getComplexidade());
            ps.setInt(4, p.getMinMembros());
            ps.setString(5, p.getSkillPrincipalRequerida());
            ps.setInt(6, p.getQuantidadeIdeal());
            ps.setString(7, p.getPersonalidadeDesejada());
            ps.setLong(8, p.getId());

            if (ps.executeUpdate() > 0) {
                saveRequisitos(p);
                return p;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar projeto: " + e.getMessage());
        }

        return null;
    }

    public boolean delete(Long id) {

        String sqlDeleteReq = "DELETE FROM REQUISITO WHERE projeto_id = ?";
        String sqlDeleteProj = "DELETE FROM PROJETO WHERE id_projeto = ?";

        try (Connection con = ConnectionFactory.getConnection()) {

            // 1) apaga requisitos
            try (PreparedStatement ps = con.prepareStatement(sqlDeleteReq)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            // 2) apaga projeto
            try (PreparedStatement ps = con.prepareStatement(sqlDeleteProj)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao excluir projeto: " + e.getMessage());
            return false;
        }
    }

}
