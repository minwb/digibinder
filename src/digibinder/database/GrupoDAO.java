package digibinder.database;

import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Grupo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * responsável por todas as operações de persistência de grupos no banco de dados
 */
public class GrupoDAO {

    private final DatabaseConnection db;

    public GrupoDAO() throws DatabaseException {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * salva um novo grupo no banco de dados
     * @param grupo grupo a ser salvo (sem ID)
     * @throws DatabaseException se ocorrer erro de persistência
     * @throws ValidationException se o nome estiver vazio
     */
    public void salvar(Grupo grupo) throws DatabaseException, ValidationException {
        if (grupo.getNome() == null || grupo.getNome().isBlank()) {
            throw new ValidationException("O nome do grupo é obrigatório.");
        }

        String sql = "INSERT INTO grupo (nome, data_debut) VALUES (?, ?)";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, grupo.getNome());
            
            if (grupo.getDataDebut() != null) {
                stmt.setDate(2, java.sql.Date.valueOf(grupo.getDataDebut()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.executeUpdate();

            // recupera o ID gerado automaticamente e atribui ao objeto
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    grupo.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar grupo: " + e.getMessage(), e);
        }
    }

    /**
     * atualiza os dados de um grupo existente
     * @param grupo grupo com dados atualizados (deve ter ID)
     * @throws DatabaseException se ocorrer erro de persistência
     * @throws ValidationException se o nome estiver vazio
     */
    public void atualizar(Grupo grupo) throws DatabaseException, ValidationException {
        if (grupo.getNome() == null || grupo.getNome().isBlank()) {
            throw new ValidationException("O nome do grupo é obrigatório.");
        }

        String sql = "UPDATE grupo SET nome = ?, data_debut = ? WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setString(1, grupo.getNome());

            if (grupo.getDataDebut() != null) {
                stmt.setDate(2, java.sql.Date.valueOf(grupo.getDataDebut()));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            stmt.setInt(3, grupo.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar grupo: " + e.getMessage(), e);
        }
    }

    /**
     * remove um grupo pelo ID. remove também álbuns e photocards associados (CASCADE)
     * @param id ID do grupo a remover
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void deletar(int id) throws DatabaseException {
        String sql = "DELETE FROM grupo WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar grupo: " + e.getMessage(), e);
        }
    }

    /**
     * busca todos os grupos cadastrados, ordenados por nome
     * @return lista de grupos
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Grupo> buscarTodos() throws DatabaseException {
        String sql = "SELECT * FROM grupo ORDER BY nome";
        List<Grupo> grupos = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                grupos.add(mapearResultado(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar grupos: " + e.getMessage(), e);
        }

        return grupos;
    }

    /**
     * busca um grupo pelo ID
     * @param id ID do grupo
     * @return grupo encontrado ou null
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public Grupo buscarPorId(int id) throws DatabaseException {
        String sql = "SELECT * FROM grupo WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar grupo: " + e.getMessage(), e);
        }

        return null;
    }

    /** converte uma linha do ResultSet em um objeto Grupo. */
    private Grupo mapearResultado(ResultSet rs) throws SQLException {
        Grupo grupo = new Grupo();
        grupo.setId(rs.getInt("id"));
        grupo.setNome(rs.getString("nome"));

        java.sql.Date dataDebut = rs.getDate("data_debut");
        if (dataDebut != null) {
            grupo.setDataDebut(dataDebut.toLocalDate());
        }

        return grupo;
    }
}