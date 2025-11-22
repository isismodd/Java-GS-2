package br.com.fiap.dao;

import br.com.fiap.to.ColaboradorTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColaboradorDAO {

    private final String TABLE_NAME = "COLABORADOR";


    private ColaboradorTO mapResultSetToTO(ResultSet rs) throws SQLException {
        ColaboradorTO colab = new ColaboradorTO();

        colab.setId(rs.getLong("id_colaborador"));
        colab.setNome(rs.getString("nome"));
        colab.setIdade(rs.getInt("idade"));
        colab.setPersonalidade(rs.getString("personalidade"));
        colab.setExperiencia(rs.getInt("experiencia"));

        colab.setHabilidades(findHabilidadesByColaborador(colab.getId()));

        return colab;
    }

    /**
     * Carrega habilidades do colaborador.
     * Gerencia e fecha sua própria conexão.
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
            Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setLong(1, idColaborador);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    habilidades.add(rs.getString("nome"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar habilidades: " + e.getMessage());
        }

        return habilidades;
    }
    
    // --- LÓGICA DE PERSISTÊNCIA DE HABILIDADES ---

    /**
     * Verifica se a habilidade existe na tabela HABILIDADE.
     * Se existir, retorna o ID. Se não, insere e retorna o ID gerado.
     * Usa a mesma conexão da transação principal (con).
     */
    private Long getOrCreateHabilidadeId(String nomeHabilidade, Connection con) throws SQLException {
        
        // 1. Tentar buscar ID (verificar se já existe)
        String selectSql = "SELECT id_habilidade FROM HABILIDADE WHERE nome = ?";
        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, nomeHabilidade);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id_habilidade"); // Habilidade encontrada
                }
            }
        }

        // 2. Se não existir, inserir e retornar o ID
        String insertSql = "INSERT INTO HABILIDADE (nome) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(insertSql, new String[]{"id_habilidade"})) {
            ps.setString(1, nomeHabilidade);
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1); // ID da nova habilidade
                    }
                }
            }
        }
        // Se chegar aqui, houve uma falha inesperada
        throw new SQLException("Falha grave ao criar ou buscar ID da habilidade: " + nomeHabilidade);
    }
    
    /**
     * Salva habilidades do colaborador. 
     */
    private void saveHabilidades(ColaboradorTO colab, Connection con) throws SQLException {

        if (colab.getHabilidades() == null) return;

        String delete = "DELETE FROM COLABORADOR_HABILIDADE WHERE id_colaborador = ?";
        String insert = "INSERT INTO COLABORADOR_HABILIDADE (id_colaborador, id_habilidade) VALUES (?, ?)";

        // Remover habilidades antigas
        try (PreparedStatement ps = con.prepareStatement(delete)) {
            ps.setLong(1, colab.getId());
            ps.executeUpdate();
        }

        // Inserir novas habilidades
        for (String hab : colab.getHabilidades()) {
            
     
            Long idHabilidade = getOrCreateHabilidadeId(hab, con); 

            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setLong(1, colab.getId());
                ps.setLong(2, idHabilidade); // Usa o ID garantido
                ps.executeUpdate();
            }
        }
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

            while (rs.next()) {
                lista.add(mapResultSetToTO(rs)); 
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
            con.setAutoCommit(false); // Inicia a transação
            
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

                saveHabilidades(colab, con); // Salva habilidades e cria se necessário
                
                con.commit();
                return colab;
            } else {
                 con.rollback();
            }

        } catch (SQLException e) {
            System.out.println("Erro ao salvar colaborador: " + e.getMessage());
        }

        return null;
    }

    /**
     * Atualiza colaborador (Com transação)
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
            con.setAutoCommit(false);
            
            ps.setString(1, colab.getNome());
            ps.setInt(2, colab.getIdade());
            ps.setString(3, colab.getPersonalidade());
            ps.setInt(4, colab.getExperiencia());
            ps.setLong(5, colab.getId());

            if (ps.executeUpdate() > 0) {
                saveHabilidades(colab, con);
                con.commit();
                return colab;
            } else {
                 con.rollback();
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar colaborador: " + e.getMessage());
        }

        return null;
    }

    /**
     * Excluir colaborador (Com transação)
     */
    public boolean delete(Long id) {

        String sqlHab = "DELETE FROM COLABORADOR_HABILIDADE WHERE id_colaborador = ?";
        String sql = "DELETE FROM COLABORADOR WHERE id_colaborador = ?";

        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            
            try (PreparedStatement psHab = con.prepareStatement(sqlHab)) {
                psHab.setLong(1, id);
                psHab.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, id);
                if (ps.executeUpdate() > 0) {
                    con.commit();
                    return true;
                }
            }
            
            con.rollback();

        } catch (SQLException e) {
            System.out.println("Erro ao excluir colaborador: " + e.getMessage());
        }

        return false;
    }
}
