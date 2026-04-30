package digibinder.database;

import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Album;
import digibinder.model.Photocard;
import digibinder.model.StatusPhotocard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * responsável por todas as operações de persistência de photocards no banco de dados
 */
public class PhotocardDAO {

    private final DatabaseConnection db;

    public PhotocardDAO() throws DatabaseException {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * salva um novo photocard no banco de dados
     * @param photocard photocard a ser salvo (deve ter álbum associado)
     * @throws DatabaseException   se ocorrer erro de persistência
     * @throws ValidationException se dados obrigatórios estiverem faltando
     */
    public void salvar(Photocard photocard) throws DatabaseException, ValidationException {
        if (photocard.getMembro() == null || photocard.getMembro().isBlank()) {
            throw new ValidationException("O nome do membro é obrigatório.");
        }
        if (photocard.getAlbum() == null || photocard.getAlbum().getId() == null) {
            throw new ValidationException("O photocard precisa estar associado a um álbum.");
        }

        String sql = """
            INSERT INTO photocard (membro, caminho_imagem, status, album_id, posicao_x, posicao_y, pagina_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, photocard.getMembro());
            stmt.setString(2, photocard.getCaminhoImagem());
            stmt.setString(3, photocard.getStatus().name());
            stmt.setInt(4, photocard.getAlbum().getId());
            stmt.setObject(5, photocard.getPosicaoX());
            stmt.setObject(6, photocard.getPosicaoY());
            stmt.setObject(7, photocard.getPaginaId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    photocard.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar photocard: " + e.getMessage(), e);
        }
    }

    /**
     * atualiza os dados de um photocard existente
     * @param photocard photocard com dados atualizados (deve ter ID)
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void atualizar(Photocard photocard) throws DatabaseException {
        String sql = """
            UPDATE photocard
            SET membro = ?, caminho_imagem = ?, status = ?, posicao_x = ?, posicao_y = ?, pagina_id = ?
            WHERE id = ?
        """;

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setString(1, photocard.getMembro());
            stmt.setString(2, photocard.getCaminhoImagem());
            stmt.setString(3, photocard.getStatus().name());
            stmt.setObject(4, photocard.getPosicaoX());
            stmt.setObject(5, photocard.getPosicaoY());
            stmt.setObject(6, photocard.getPaginaId());
            stmt.setInt(7, photocard.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar photocard: " + e.getMessage(), e);
        }
    }

    /**
     * atualiza apenas o status de um photocard
     * @param id ID do photocard
     * @param status novo status
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void atualizarStatus(int id, StatusPhotocard status) throws DatabaseException {
        String sql = "UPDATE photocard SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar status: " + e.getMessage(), e);
        }
    }

    /**
     * remove um photocard pelo ID
     * @param id ID do photocard a remover
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void deletar(int id) throws DatabaseException {
        String sql = "DELETE FROM photocard WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar photocard: " + e.getMessage(), e);
        }
    }

    /**
     * busca todos os photocards de um álbum específico
     * @param albumId ID do álbum
     * @return lista de photocards do álbum
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Photocard> buscarPorAlbum(int albumId) throws DatabaseException {
        String sql = """
            SELECT p.*, a.id AS a_id, a.nome AS a_nome, a.versao AS a_versao, a.ano_lancamento
            FROM photocard p
            JOIN album a ON p.album_id = a.id
            WHERE p.album_id = ?
            ORDER BY p.membro
        """;

        List<Photocard> lista = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, albumId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultado(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar photocards: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * busca todos os photocards de um grupo
     * @param grupoId ID do grupo
     * @return lista de photocards do grupo
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Photocard> buscarPorGrupo(int grupoId) throws DatabaseException {
        String sql = """
            SELECT p.*, a.id AS a_id, a.nome AS a_nome, a.versao AS a_versao, a.ano_lancamento
            FROM photocard p
            JOIN album a ON p.album_id = a.id
            WHERE a.grupo_id = ?
            ORDER BY a.nome, p.membro
        """;

        List<Photocard> lista = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, grupoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultado(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar photocards: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * busca todos os photocards alocados em uma página de binder específica
     * @param paginaId ID da página
     * @return lista de photocards da página
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Photocard> buscarPorPagina(int paginaId) throws DatabaseException {
        String sql = """
            SELECT p.*, a.id AS a_id, a.nome AS a_nome, a.versao AS a_versao, a.ano_lancamento
            FROM photocard p
            JOIN album a ON p.album_id = a.id
            WHERE p.pagina_id = ?
        """;

        List<Photocard> lista = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, paginaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultado(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar photocards da página: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * busca um photocard pelo ID
     * @param id ID do photocard
     * @return photocard encontrado ou null
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public Photocard buscarPorId(int id) throws DatabaseException {
        String sql = """
            SELECT p.*, a.id AS a_id, a.nome AS a_nome, a.versao AS a_versao, a.ano_lancamento
            FROM photocard p
            JOIN album a ON p.album_id = a.id
            WHERE p.id = ?
        """;

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar photocard: " + e.getMessage(), e);
        }

        return null;
    }

    /** converte uma linha do ResultSet em um objeto Photocard com Album embutido */
    private Photocard mapearResultado(ResultSet rs) throws SQLException {
        Album album = new Album();
        album.setId(rs.getInt("a_id"));
        album.setNome(rs.getString("a_nome"));
        album.setVersao(rs.getString("a_versao"));

        int ano = rs.getInt("ano_lancamento");
        if (!rs.wasNull()) {
            album.setAnoLancamento(ano);
        }

        Photocard pc = new Photocard();
        pc.setId(rs.getInt("id"));
        pc.setMembro(rs.getString("membro"));
        pc.setCaminhoImagem(rs.getString("caminho_imagem"));
        pc.setAlbum(album);

        String statusStr = rs.getString("status");
        try {
            pc.setStatus(StatusPhotocard.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            pc.setStatus(StatusPhotocard.DESEJADO); // fallback seguro
        }

        int posX = rs.getInt("posicao_x");
        if (!rs.wasNull()) pc.setPosicaoX(posX);

        int posY = rs.getInt("posicao_y");
        if (!rs.wasNull()) pc.setPosicaoY(posY);

        int pagId = rs.getInt("pagina_id");
        if (!rs.wasNull()) pc.setPaginaId(pagId);

        return pc;
    }
}