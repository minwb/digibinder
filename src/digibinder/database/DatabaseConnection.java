package digibinder.database;

import digibinder.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * gerencia a conexão com o PostgreSQL
 * implementa o padrão Singleton para garantir uma única instância de conexão
 * as credenciais são lidas do arquivo db.properties no classpath
 * é responsável por criar o schema completo na primeira execução
 */
public class DatabaseConnection {

    // Singleton
    private static DatabaseConnection instancia;

    // conexão persistente durante a execução
    private Connection conexao;

    /** lê as configurações, conecta e cria o schema */
    private DatabaseConnection() throws DatabaseException {
        Properties props = carregarPropriedades();

        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "5432");
        String name = props.getProperty("db.name", "digibinder");
        String user = props.getProperty("db.user", "postgres");
        String password = props.getProperty("db.password", "postgres");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;

        try {
            // carrega o driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            this.conexao = DriverManager.getConnection(url, user, password);
            criarSchema();
        } catch (ClassNotFoundException e) {
            throw new DatabaseException(
                "Driver PostgreSQL não encontrado. Verifique se postgresql.jar está no classpath.", e);
        } catch (SQLException e) {
            throw new DatabaseException(
                "Erro ao conectar com o banco de dados.\n" +
                "Verifique se o PostgreSQL está rodando e as credenciais em db.properties.\n" +
                "Detalhe: " + e.getMessage(), e);
        }
    }

    /**
     * lê as configurações de conexão do arquivo db.properties.
     */
    private Properties carregarPropriedades() throws DatabaseException {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("db.properties não encontrado. Usando configurações padrão.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler db.properties: " + e.getMessage());
        }
        return props;
    }

    /**
     * retorna a instância única do gerenciador de conexão
     * cria a instância na primeira chamada
     * @return instância de DatabaseConnection
     * @throws DatabaseException se não for possível conectar
     */
    public static DatabaseConnection getInstance() throws DatabaseException {
        if (instancia == null || !instancia.isConexaoValida()) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    /**
     * retorna a conexão ativa com o banco de dados
     * @return Connection JDBC ativa
     * @throws DatabaseException se a conexão estiver inválida
     */
    public Connection getConexao() throws DatabaseException {
        if (!isConexaoValida()) {
            throw new DatabaseException("Conexão com banco de dados está fechada ou inválida.");
        }
        return conexao;
    }

    /** verifica se a conexão atual está ativa e válida. */
    private boolean isConexaoValida() {
        try {
            return conexao != null && !conexao.isClosed() && conexao.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * cria todas as tabelas do schema se ainda não existirem
     */
    private void criarSchema() throws SQLException {
        try (Statement stmt = conexao.createStatement()) {

            // tabela de grupos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS grupo (
                    id          SERIAL       PRIMARY KEY,
                    nome        VARCHAR(100) NOT NULL UNIQUE,
                    data_debut  DATE
                )
            """);

            // tabela de álbuns
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS album (
                    id              SERIAL       PRIMARY KEY,
                    nome            VARCHAR(150) NOT NULL,
                    versao          VARCHAR(80),
                    ano_lancamento  INTEGER,
                    grupo_id        INTEGER      NOT NULL,
                    FOREIGN KEY (grupo_id) REFERENCES grupo(id) ON DELETE CASCADE
                )
            """);

            // tabela de photocards
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS photocard (
                    id              SERIAL       PRIMARY KEY,
                    membro          VARCHAR(100) NOT NULL,
                    caminho_imagem  TEXT,
                    status          VARCHAR(20)  NOT NULL DEFAULT 'DESEJADO',
                    album_id        INTEGER      NOT NULL,
                    posicao_x       INTEGER,
                    posicao_y       INTEGER,
                    pagina_id       INTEGER,
                    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
                )
            """);

            // tabela de binders
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS binder (
                    id      SERIAL       PRIMARY KEY,
                    nome    VARCHAR(100) NOT NULL,
                    cor_r   INTEGER      NOT NULL DEFAULT 180,
                    cor_g   INTEGER      NOT NULL DEFAULT 150,
                    cor_b   INTEGER      NOT NULL DEFAULT 130
                )
            """);

            // Tabela de páginas do binder
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pagina_binder (
                    id             SERIAL      PRIMARY KEY,
                    numero_pagina  INTEGER     NOT NULL,
                    layout         VARCHAR(20) NOT NULL DEFAULT 'GRID_3X3',
                    binder_id      INTEGER     NOT NULL,
                    FOREIGN KEY (binder_id) REFERENCES binder(id) ON DELETE CASCADE
                )
            """);
        }
    }

    /** encerra a conexão e aplicação */
    public void fechar() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
