package digibinder.database;

import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Album;
import digibinder.model.Grupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * responsável por todas as operações de persistência de álbuns no banco de dados
 */
public class AlbumDAO {

    private final DatabaseConnection db;

    public AlbumDAO() throws DatabaseException {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * salva um novo álbum no banco de dados
     * @param album álbum a ser salvo (deve ter grupo associado)
     * @throws DatabaseException  se ocorrer erro de persistência
     * @throws ValidationException se dados obrigatórios estiverem faltando
     */
    public void salvar(Album album) throws DatabaseException, ValidationException {
        if (album.getNome() == null || album.getNome().isBlank()) {
            throw new ValidationException("O nome do álbum é obrigatório.");
        }
        if (album.getGrupo() == null || album.getGrupo().getId() == null) {
            throw new ValidationException("O álbum precisa estar associado a um grupo.");
        }

        String sql = "INSERT INTO album (nome, versao, ano_lancamento, grupo_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, album.getNome());
            stmt.setString(2, album.getVersao());
            stmt.setObject(3, album.getAnoLancamento());
            stmt.setInt(4, album.getGrupo().getId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    album.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar álbum: " + e.getMessage(), e);
        }
    }

    /**
     * atualiza um álbum existente
     * @param album álbum com dados atualizados (deve ter ID)
     * @throws DatabaseException  se ocorrer erro de persistência
     * @throws ValidationException se dados obrigatórios estiverem faltando
     */
    public void atualizar(Album album) throws DatabaseException, ValidationException {
        if (album.getNome() == null || album.getNome().isBlank()) {
            throw new ValidationException("O nome do álbum é obrigatório.");
        }

        String sql = "UPDATE album SET nome = ?, versao = ?, ano_lancamento = ? WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setString(1, album.getNome());
            stmt.setString(2, album.getVersao());
            stmt.setObject(3, album.getAnoLancamento());
            stmt.setInt(4, album.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar álbum: " + e.getMessage(), e);
        }
    }

    /**
     * remove um álbum pelo ID. remove também os photocards associados (CASCADE).
     * @param id ID do álbum a remover
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void deletar(int id) throws DatabaseException {
        String sql = "DELETE FROM album WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar álbum: " + e.getMessage(), e);
        }
    }

    /**
     * busca todos os álbuns de um grupo específico
     * @param grupoId ID do grupo
     * @return lista de álbuns do grupo
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Album> buscarPorGrupo(int grupoId) throws DatabaseException {
        String sql = """
            SELECT a.*, g.id AS g_id, g.nome AS g_nome, g.data_debut AS g_debut
            FROM album a
            JOIN grupo g ON a.grupo_id = g.id
            WHERE a.grupo_id = ?
            ORDER BY a.ano_lancamento DESC, a.nome
        """;

        List<Album> albuns = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, grupoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    albuns.add(mapearResultado(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar álbuns: " + e.getMessage(), e);
        }

        return albuns;
    }

    /**
     * busca todos os álbuns cadastrados, com seus grupos
     * @return lista completa de álbuns
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Album> buscarTodos() throws DatabaseException {
        String sql = """
            SELECT a.*, g.id AS g_id, g.nome AS g_nome, g.data_debut AS g_debut
            FROM album a
            JOIN grupo g ON a.grupo_id = g.id
            ORDER BY g.nome, a.nome
        """;

        List<Album> albuns = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                albuns.add(mapearResultado(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar álbuns: " + e.getMessage(), e);
        }
        return albuns;
    }

    /**
     * busca um álbum pelo ID
     * @param id ID do álbum
     * @return álbum encontrado ou null
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public Album buscarPorId(int id) throws DatabaseException {
        String sql = """
            SELECT a.*, g.id AS g_id, g.nome AS g_nome, g.data_debut AS g_debut
            FROM album a
            JOIN grupo g ON a.grupo_id = g.id
            WHERE a.id = ?
        """;

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar álbum: " + e.getMessage(), e);
        }

        return null;
    }

    /** converte uma linha do ResultSet em um objeto Album com Grupo embutido. */
    private Album mapearResultado(ResultSet rs) throws SQLException {
        Grupo grupo = new Grupo();
        grupo.setId(rs.getInt("g_id"));
        grupo.setNome(rs.getString("g_nome"));

        Album album = new Album();
        album.setId(rs.getInt("id"));
        album.setNome(rs.getString("nome"));
        album.setVersao(rs.getString("versao"));

        int ano = rs.getInt("ano_lancamento");
        if (!rs.wasNull()) {
            album.setAnoLancamento(ano);
        }

        album.setGrupo(grupo);
        return album;
    }
}
