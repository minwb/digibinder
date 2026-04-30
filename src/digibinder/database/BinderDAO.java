package digibinder.database;

import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Binder;
import digibinder.model.PaginaBinder;
import digibinder.model.TipoLayout;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * responsável por todas as operações de persistência de fichários e suas páginas
 */
public class BinderDAO {

    private final DatabaseConnection db;

    public BinderDAO() throws DatabaseException {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * salva um novo binder no banco de dados
     * @param binder binder a ser salvo (sem ID)
     * @throws DatabaseException   se ocorrer erro de persistência
     * @throws ValidationException se o nome estiver vazio
     */
    public void salvar(Binder binder) throws DatabaseException, ValidationException {
        if (binder.getNome() == null || binder.getNome().isBlank()) {
            throw new ValidationException("O nome do binder é obrigatório.");
        }

        String sql = "INSERT INTO binder (nome, cor_r, cor_g, cor_b) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            Color cor = binder.getCorCapa();
            stmt.setString(1, binder.getNome());
            stmt.setInt(2, cor.getRed());
            stmt.setInt(3, cor.getGreen());
            stmt.setInt(4, cor.getBlue());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    binder.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar binder: " + e.getMessage(), e);
        }
    }

    /**
     * atualiza um binder existente
     * @param binder binder com dados atualizados (deve ter ID)
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void atualizar(Binder binder) throws DatabaseException, ValidationException {
        if (binder.getNome() == null || binder.getNome().isBlank()) {
            throw new ValidationException("O nome do binder é obrigatório.");
        }

        String sql = "UPDATE binder SET nome = ?, cor_r = ?, cor_g = ?, cor_b = ? WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            Color cor = binder.getCorCapa();
            stmt.setString(1, binder.getNome());
            stmt.setInt(2, cor.getRed());
            stmt.setInt(3, cor.getGreen());
            stmt.setInt(4, cor.getBlue());
            stmt.setInt(5, binder.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar binder: " + e.getMessage(), e);
        }
    }

    /**
     * remove um binder pelo ID. remove também as páginas associadas (CASCADE).
     * @param id ID do binder a remover
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void deletar(int id) throws DatabaseException {
        String sql = "DELETE FROM binder WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar binder: " + e.getMessage(), e);
        }
    }

    /**
     * busca todos os binders cadastrados
     * @return lista de binders (sem suas páginas carregadas)
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<Binder> buscarTodos() throws DatabaseException {
        String sql = "SELECT * FROM binder ORDER BY nome";
        List<Binder> lista = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearBinder(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar binders: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * busca um binder pelo ID, incluindo suas páginas
     *
     * @param id ID do binder
     * @return binder completo com páginas, ou null se não encontrado
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public Binder buscarPorIdComPaginas(int id) throws DatabaseException {
        String sql = "SELECT * FROM binder WHERE id = ?";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Binder binder = mapearBinder(rs);
                    List<PaginaBinder> paginas = buscarPaginasPorBinder(id);
                    paginas.forEach(binder::adicionarPagina);
                    return binder;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar binder: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * salva uma nova página de binder no banco de dados
     * @param pagina página a ser salva (deve ter binder associado)
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void salvarPagina(PaginaBinder pagina) throws DatabaseException {
        if (pagina.getBinder() == null || pagina.getBinder().getId() == null) {
            throw new DatabaseException("A página precisa estar associada a um binder.");
        }

        String sql = "INSERT INTO pagina_binder (numero_pagina, layout, binder_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pagina.getNumeroPagina());
            stmt.setString(2, pagina.getLayout().name());
            stmt.setInt(3, pagina.getBinder().getId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    pagina.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar página: " + e.getMessage(), e);
        }
    }

    /**
     * remove uma página pelo ID
     * @param id ID da página a remover
     * @throws DatabaseException se ocorrer erro de persistência
     */
    public void deletarPagina(int id) throws DatabaseException {
        // desaloca os photocards que estavam nesta página
        String desalocar = "UPDATE photocard SET pagina_id = NULL, posicao_x = NULL, posicao_y = NULL WHERE pagina_id = ?";
        String deletar    = "DELETE FROM pagina_binder WHERE id = ?";

        try (PreparedStatement stmt1 = db.getConexao().prepareStatement(desalocar);
             PreparedStatement stmt2 = db.getConexao().prepareStatement(deletar)) {

            stmt1.setInt(1, id);
            stmt1.executeUpdate();

            stmt2.setInt(1, id);
            stmt2.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar página: " + e.getMessage(), e);
        }
    }

    /**
     * busca todas as páginas de um binder
     * @param binderId ID do binder
     * @return lista de páginas
     * @throws DatabaseException se ocorrer erro de leitura
     */
    public List<PaginaBinder> buscarPaginasPorBinder(int binderId) throws DatabaseException {
        String sql = "SELECT * FROM pagina_binder WHERE binder_id = ? ORDER BY numero_pagina";
        List<PaginaBinder> lista = new ArrayList<>();

        try (PreparedStatement stmt = db.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, binderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPagina(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar páginas: " + e.getMessage(), e);
        }

        return lista;
    }
    
    private Binder mapearBinder(ResultSet rs) throws SQLException {
        Color cor = new Color(rs.getInt("cor_r"), rs.getInt("cor_g"), rs.getInt("cor_b"));
        return new Binder(rs.getInt("id"), rs.getString("nome"), cor);
    }

    private PaginaBinder mapearPagina(ResultSet rs) throws SQLException {
        TipoLayout layout;
        try {
            layout = TipoLayout.valueOf(rs.getString("layout"));
        } catch (IllegalArgumentException e) {
            layout = TipoLayout.GRID_3X3; // fallback seguro
        }
        return new PaginaBinder(rs.getInt("id"), rs.getInt("numero_pagina"), layout, null);
    }
}
