package digibinder.controller;

import digibinder.database.AlbumDAO;
import digibinder.database.PhotocardDAO;
import digibinder.exception.DatabaseException;
import digibinder.exception.ImageNotFoundException;
import digibinder.exception.ValidationException;
import digibinder.model.Album;
import digibinder.model.Photocard;
import digibinder.model.StatusPhotocard;

import java.io.File;
import java.util.List;

/**
 * responsável toda a lógica e das regras de negócio envolvendo os photocards
 * validador principal pra garantir que nenhuma carta entre no sistema sem membro ou com imagem indisponível
 */
public class PhotocardController {

    private final PhotocardDAO photocardDAO;
    private final AlbumDAO albumDAO;

    public PhotocardController() throws DatabaseException {
        this.photocardDAO = new PhotocardDAO();
        this.albumDAO = new AlbumDAO();
    }

    /**
     * cria um novo photocard e vincula ele a um álbum que já existe
     * verifica se o nome foi preenchido e se o arquivo da imagem existe no computador
     * @param membro o nome do integrante que tá na foto
     * @param caminhoImagem onde a foto tá salva no seu pc (pode deixar vazio se não tiver)
     * @param albumId o código do álbum dono desse card
     * @return o photocard montado e com o id gerado pelo banco
     * @throws ValidationException se faltar o nome do membro ou o álbum não for encontrado
     * @throws DatabaseException se der algum erro na hora de gravar no banco
     * @throws ImageNotFoundException se o caminho da imagem for inválido ou o arquivo não estiver lá
     */
    public Photocard criarPhotocard(String membro, String caminhoImagem, int albumId)
            throws ValidationException, DatabaseException, ImageNotFoundException {

        if (membro == null || membro.isBlank()) {
            throw new ValidationException("O nome do membro não pode ser vazio.");
        }

        Album album = albumDAO.buscarPorId(albumId);
        if (album == null) {
            throw new ValidationException("Álbum não encontrado com o ID informado.");
        }

        if (caminhoImagem != null && !caminhoImagem.isBlank()) {
            File arquivo = new File(caminhoImagem.trim());
            if (!arquivo.exists() || !arquivo.isFile()) {
                throw new ImageNotFoundException(caminhoImagem);
            }
        }

        Photocard pc = new Photocard(membro.trim(), caminhoImagem, album);
        photocardDAO.salvar(pc);
        return pc;
    }

    /**
     * muda a etiqueta do card pra indicar se já tem na coleção, deseja ou está trocando
     * @param photocardId código de identificação do photocard
     * @param novoStatus o novo status que o usuário escolheu
     * @throws DatabaseException se o banco não conseguir salvar a alteração
     */
    public void atualizarStatus(int photocardId, StatusPhotocard novoStatus)
            throws DatabaseException {
        photocardDAO.atualizarStatus(photocardId, novoStatus);
    }

    /**
     * pega um card solto e guarda ele num espaço específico de uma página dentro do binder virtual
     * @param photocardId código do photocard que vai ser guardado
     * @param paginaId código da página de destino
     * @param posX a coluna da grade onde o card vai ficar posicionado
     * @param posY a linha da grade onde o card vai ficar
     * @throws DatabaseException se o acesso ao banco falhar no meio do caminho
     * @throws ValidationException se a posição informada for negativa e quebrar o layout
     */
    public void alocarNoPagina(int photocardId, int paginaId, int posX, int posY)
            throws DatabaseException, ValidationException {

        if (posX < 0 || posY < 0) {
            throw new ValidationException("Posição inválida para o slot.");
        }

        Photocard pc = buscarPorIdInterno(photocardId);
        pc.setPaginaId(paginaId);
        pc.setPosicaoX(posX);
        pc.setPosicaoY(posY);
        photocardDAO.atualizar(pc);
    }

    /**
     * tira o card do fichário virtual e devolve ele pro catálogo geral
     * ele não é deletado do sistema, só perde a informação de gaveta dele
     * @param photocardId código do photocard
     * @throws DatabaseException se não der pra atualizar o banco de dados
     */
    public void removerDoPagina(int photocardId) throws DatabaseException {
        Photocard pc = buscarPorIdInterno(photocardId);
        pc.setPaginaId(null);
        pc.setPosicaoX(null);
        pc.setPosicaoY(null);
        photocardDAO.atualizar(pc);
    }

    /**
     * apaga o photocard do sistema pra sempre
     * @param id código numérico do photocard
     * @throws DatabaseException caso o banco impeça de deletar o item
     */
    public void deletarPhotocard(int id) throws DatabaseException {
        photocardDAO.deletar(id);
    }

    /**
     * pega todos os photocards que pertencem a um álbum específico
     * @param albumId código do álbum
     * @return a lista cheia com os photocards daquele álbum
     * @throws DatabaseException se der problema na hora de ler os dados
     */
    public List<Photocard> listarPorAlbum(int albumId) throws DatabaseException {
        return photocardDAO.buscarPorAlbum(albumId);
    }

    /**
     * junta e traz todos os photocards misturados de todos os álbuns de um mesmo grupo
     * @param grupoId código do grupo
     * @return lista completa com todos os cards desse artista
     * @throws DatabaseException se a busca falhar no banco de dados
     */
    public List<Photocard> listarPorGrupo(int grupoId) throws DatabaseException {
        return photocardDAO.buscarPorGrupo(grupoId);
    }

    /**
     * traz só os photocards que tão guardados em uma mesma página do binder
     * @param paginaId código da página
     * @return lista dos photocards que tão ocupando os slots lá
     * @throws DatabaseException se ocorrer algum erro de leitura
     */
    public List<Photocard> listarPorPagina(int paginaId) throws DatabaseException {
        return photocardDAO.buscarPorPagina(paginaId);
    }

    /**
     * método pra buscar um photocard direto na base de dados e jogar um erro se não achar nada
     * @throws DatabaseException se não encontrar o card ou o banco falhar
     */
    private Photocard buscarPorIdInterno(int id) throws DatabaseException {
        Photocard pc = photocardDAO.buscarPorId(id);
        if (pc == null) {
            throw new DatabaseException("Photocard não encontrado com ID: " + id);
        }
        return pc;
    }
}