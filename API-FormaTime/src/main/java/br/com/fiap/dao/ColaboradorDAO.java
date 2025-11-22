package br.com.fiap.dao;

import br.com.fiap.to.ColaboradorTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColaboradorDAO {

    private final String TABLE_NAME = "COLABORADOR";

    /**
     * Converte o ResultSet em um objeto ColaboradorTO.
     * Não precisa mais receber a conexão, pois o carregamento das habilidades
     * será feito em uma conexão separada.
     */
    private ColaboradorTO mapResultSetToTO(ResultSet rs) throws SQLException {
        ColaboradorTO colab = new ColaboradorTO();

        colab.setId(rs.getLong("id_colaborador"));
        colab.setNome(rs.getString("nome"));
        colab.setIdade(rs.getInt("idade"));
        colab.setPersonalidade(rs.getString("personalidade"));
        colab.setExperiencia(rs.getInt("experiencia"));

        // Carregar habilidades chamando o método que agora abre sua própria conexão
        colab.setHabilidades(findHabilidadesByColaborador(colab.getId()));

        return colab;
    }

    /**
     * Carrega habilidades do colaborador.
     * ⚠️ Abre e fecha sua própria conexão para isolar a transação.
     */
    public List<String> findHabilidadesByColaborador(Long idColaborador) {
        List<String> habilidades = new ArrayList<>();

        String sql = """
            SELECT h.nome 
            FROM HABILIDADE h
            JOIN COLABORADOR_HABILIDADE ch ON ch.id_habilidade = h.id_habilidade
            WHERE ch.id_colaborador = ?
        """;

        try (
            Connection con = ConnectionFactory.getConnection(); // Abre nova conexão
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setLong(1, idColaborador);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    habilidades.add(rs.getString("nome"));
                }
            }
        } catch (SQLException e) {
            // Este erro não deve mais interromper o findAll
            System.out.println("Erro ao buscar habilidades: " + e.getMessage()); 
        }

        return habilidades;
    }

    /**
     * Buscar colaborador por ID
     */
    public ColaboradorTO findById(Long id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id_colaborador = ?";

        try (
                Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // mapResultSetToTO não recebe mais a conexão
                    return mapResultSetToTO(rs); 
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro no findById: " + e.getMessage());
        }

        return null;
    }

    /**
     * Listar todos os colaboradores
     */
    public List<ColaboradorTO> findAll() {
        List<ColaboradorTO> lista = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY nome";

        try (
                Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            // O loop agora só depende da conexão 'con' do findAll
            while (rs.next()) { 
                lista.add(mapResultSetToTO(rs)); // mapResultSetToTO cuida das habilidades internamente
            }

        } catch (SQLException e) {
            System.out.println("Erro no findAll: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Salvar colaborador
     */
    public ColaboradorTO save(ColaboradorTO colab) {

        String sql = """
            INSERT INTO COLABORADOR 
            (nome, idade, personalidade, experiencia)
            VALUES (?, ?, ?, ?)
        """;

        try (
                Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_colaborador"})
        ) {

            ps.setString(1, colab.getNome());
            ps.setInt(2, colab.getIdade());
            ps.setString(3, colab.getPersonalidade());
            ps.setInt(4, colab.getExperiencia());

            if (ps.executeUpdate() > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        colab.setId(rs.getLong(1));
                    }
                }

                saveHabilidades(colab, con);
                return colab;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao salvar colaborador: " + e.getMessage());
        }

        return null;
    }

    /**
     * Salva habilidades do colaborador usando a MESMA CONEXÃO recebida
     */
    private void saveHabilidades(ColaboradorTO colab, Connection con) throws SQLException {

        if (colab.getHabilidades() == null) return;

        String delete = "DELETE FROM COLABORADOR_HABILIDADE WHERE id_colaborador = ?";
        String insert = """
            INSERT INTO COLABORADOR_HABILIDADE (id_colaborador, id_habilidade)
            SELECT ?, id_habilidade FROM HABILIDADE WHERE nome = ?
        """;

        // Remover habilidades antigas
        try (PreparedStatement ps = con.prepareStatement(delete)) {
            ps.setLong(1, colab.getId());
            ps.executeUpdate();
        }

        // Inserir novas habilidades
        for (String hab : colab.getHabilidades()) {
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setLong(1, colab.getId());
                ps.setString(2, hab);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Atualiza colaborador
     */
    public ColaboradorTO update(ColaboradorTO colab) {
        String sql = """
            UPDATE COLABORADOR
            SET nome=?, idade=?, personalidade=?, experiencia=?
            WHERE id_colaborador=?
        """;

        try (
                Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, colab.getNome());
            ps.setInt(2, colab.getIdade());
            ps.setString(3, colab.getPersonalidade());
            ps.setInt(4, colab.getExperiencia());
            ps.setLong(5, colab.getId());

            if (ps.executeUpdate() > 0) {
                saveHabilidades(colab, con);
                return colab;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar colaborador: " + e.getMessage());
        }

        return null;
    }

    /**
     * Excluir colaborador
     */
    public boolean delete(Long id) {

        String sqlHab = "DELETE FROM COLABORADOR_HABILIDADE WHERE id_colaborador = ?";
        String sql = "DELETE FROM COLABORADOR WHERE id_colaborador = ?";

        try (Connection con = ConnectionFactory.getConnection()) {

            try (PreparedStatement psHab = con.prepareStatement(sqlHab)) {
                psHab.setLong(1, id);
                psHab.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao excluir colaborador: " + e.getMessage());
        }

        return false;
    }
}
