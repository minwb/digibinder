package digibinder.controller;

import digibinder.database.AlbumDAO;
import digibinder.database.GrupoDAO;
import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Album;
import digibinder.model.Grupo;

import java.util.List;

/**
 * responsável pelas regras e validações antes de salvar ou alterar os álbuns no banco
 * 'escudo' entre as telas do usuário e o acesso direto aos dados
 */
public class AlbumController {

    private final AlbumDAO albumDAO;
    private final GrupoDAO grupoDAO;

    public AlbumController() throws DatabaseException {
        this.albumDAO = new AlbumDAO();
        this.grupoDAO = new GrupoDAO();
    }

    /**
     * cria um novo álbum e liga ele a um grupo que já existe
     * faz verificações importantes como conferir se o nome não tá vazio e se o ano de lançamento faz sentido
     * @param nome o nome que vai aparecer no álbum
     * @param versao a versão do álbum, tipo A ou B (pode ser vazio)
     * @param anoLancamento o ano que o álbum saiu (tem que ser entre 1990 e 2100)
     * @param grupoId o código do grupo dono desse álbum
     * @return o álbum novinho já com o id gerado pelo banco
     * @throws ValidationException se o usuário preencher algo errado na tela
     * @throws DatabaseException se der algum problema na hora de salvar
     */
    public Album criarAlbum(String nome, String versao, Integer anoLancamento, int grupoId)
            throws ValidationException, DatabaseException {

        if (nome == null || nome.isBlank()) {
            throw new ValidationException("O nome do álbum não pode ser vazio.");
        }

        Grupo grupo = grupoDAO.buscarPorId(grupoId);
        if (grupo == null) {
            throw new ValidationException("Grupo não encontrado com o ID informado.");
        }

        if (anoLancamento != null && (anoLancamento < 1990 || anoLancamento > 2100)) {
            throw new ValidationException("Ano de lançamento inválido.");
        }

        Album album = new Album(nome.trim(), versao != null ? versao.trim() : null, anoLancamento);
        album.setGrupo(grupo);

        albumDAO.salvar(album);
        return album;
    }
    
    /**
     * pega um álbum que já existe e altera as informações dele
     * confere de novo se o usuário não deixou o nome em branco sem querer
     * @param id código do álbum que a gente quer mudar
     * @param novoNome o novo nome digitado
     * @param novaVersao a nova versão escolhida
     * @param anoLancamento o ano de lançamento atualizado
     * @throws ValidationException se as novas informações forem inválidas
     * @throws DatabaseException se a comunicação com o banco falhar
     */
    public void atualizarAlbum(int id, String novoNome, String novaVersao, Integer anoLancamento)
            throws ValidationException, DatabaseException {

        if (novoNome == null || novoNome.isBlank()) {
            throw new ValidationException("O nome do álbum não pode ser vazio.");
        }

        Album album = albumDAO.buscarPorId(id);
        if (album == null) {
            throw new ValidationException("Álbum não encontrado.");
        }

        album.setNome(novoNome.trim());
        album.setVersao(novaVersao != null ? novaVersao.trim() : null);
        album.setAnoLancamento(anoLancamento);

        albumDAO.atualizar(album);
    }

    /**
     * apaga o álbum do sistema de vez
     * como o banco tá configurado em cascata, isso também apaga automaticamente todos os photocards que tavam dentro dele
     * @param id código do álbum que vai ser excluído
     * @throws DatabaseException se der pau na hora de deletar
     */
    public void deletarAlbum(int id) throws DatabaseException {
        albumDAO.deletar(id);
    }

    /**
     * busca e traz só os álbuns que pertencem a um grupo específico
     * @param grupoId o código do grupo que a gente quer ver
     * @return uma lista cheia com os álbuns daquele grupo
     * @throws DatabaseException se der erro ao tentar ler do banco
     */
    public List<Album> listarPorGrupo(int grupoId) throws DatabaseException {
        return albumDAO.buscarPorGrupo(grupoId);
    }

    /**
     * varre o banco e pega todos os álbuns cadastrados no sistema inteiro
     * @return a lista completa de álbuns misturados de todos os grupos
     * @throws DatabaseException caso o banco não consiga responder
     */
    public List<Album> listarTodos() throws DatabaseException {
        return albumDAO.buscarTodos();
    }

    /**
     * procura um álbum exato usando o número de identificação dele
     * @param id o código do álbum no banco
     * @return o álbum que a gente procurava ou nulo se ele não existir
     * @throws DatabaseException se acontecer um erro de leitura no banco
     */
    public Album buscarPorId(int id) throws DatabaseException {
        return albumDAO.buscarPorId(id);
    }
}