package digibinder.controller;

import digibinder.database.BinderDAO;
import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Binder;
import digibinder.model.PaginaBinder;
import digibinder.model.TipoLayout;

import java.awt.Color;
import java.util.List;

/**
 * responsável pelas regras e validações antes de salvar os binders e suas páginas no banco de dados
 * garante que a cor seja válida e que os cards não fiquem perdidos se uma página for apagada
 */
public class BinderController {

    private final BinderDAO dao;

    public BinderController() throws DatabaseException {
        this.dao = new BinderDAO();
    }

    /**
     * cria um novo binder e salva no sistema
     * @param nome o nome escolhido para o binder
     * @param corHex a cor da capa em formato hexadecimal tipo #C4A882 ou vazio pra usar a cor padrão
     * @return o binder novinho já com o id gerado
     * @throws ValidationException se o nome tiver vazio ou a cor for num formato bizarro
     * @throws DatabaseException se der ruim na hora de salvar no banco
     */
    public Binder criarBinder(String nome, String corHex)
            throws ValidationException, DatabaseException {

        if (nome == null || nome.isBlank()) {
            throw new ValidationException("O nome do binder não pode ser vazio.");
        }

        Color cor = null;
        if (corHex != null && !corHex.isBlank()) {
            try {
                cor = Color.decode(corHex.trim());
            } catch (NumberFormatException e) {
                throw new ValidationException("Cor inválida. Use formato hexadecimal (#RRGGBB).");
            }
        }

        Binder binder = new Binder(nome.trim(), cor);
        dao.salvar(binder);
        return binder;
    }

    /**
     * pega um binder que já existe e atualiza as informações dele
     * @param id código do binder que a gente quer mudar
     * @param nome o novo nome digitado
     * @param corHex a nova cor escolhida
     * @throws ValidationException se as novas informações não fizerem sentido
     * @throws DatabaseException se ocorrer algum erro pra gravar a mudança
     */
    public void atualizarBinder(int id, String nome, String corHex)
            throws ValidationException, DatabaseException {

        if (nome == null || nome.isBlank()) {
            throw new ValidationException("O nome do binder não pode ser vazio.");
        }

        Color cor = null;
        if (corHex != null && !corHex.isBlank()) {
            try {
                cor = Color.decode(corHex.trim());
            } catch (NumberFormatException e) {
                throw new ValidationException("Cor inválida. Use formato hexadecimal (#RRGGBB).");
            }
        }

        Binder binder = new Binder(id, nome.trim(), cor != null ? cor : new Color(180, 150, 130));
        dao.atualizar(binder);
    }

    /**
     * apaga o binder e todas as páginas dele de vez
     * @param id código numérico do binder
     * @throws DatabaseException se não der pra apagar no banco
     */
    public void deletarBinder(int id) throws DatabaseException {
        dao.deletar(id);
    }

    /**
     * cria uma página nova dentro de um binder que já existe
     * @param binderId código do binder dono da página
     * @param layout formato da página indicando se cabe 4 ou 9 cards
     * @return a página criada e numerada certinho
     * @throws DatabaseException se o banco recusar salvar
     * @throws ValidationException se ele tentar adicionar em um binder que não existe mais
     */
    public PaginaBinder adicionarPagina(int binderId, TipoLayout layout)
            throws DatabaseException, ValidationException {

        Binder binder = dao.buscarPorIdComPaginas(binderId);
        if (binder == null) {
            throw new ValidationException("Binder não encontrado.");
        }

        int numeroPagina = binder.getPaginas().size() + 1;
        PaginaBinder pagina = new PaginaBinder(numeroPagina, layout);
        pagina.setBinder(binder);

        dao.salvarPagina(pagina);
        return pagina;
    }

    /**
     * exclui uma página do binder
     * @param paginaId código da página que vai ser removida
     * @throws DatabaseException se acontecer um erro no meio do processo
     */
    public void removerPagina(int paginaId) throws DatabaseException {
        dao.deletarPagina(paginaId);
    }

    /**
     * busca e traz todos os binders cadastrados 
     * @return uma lista com os binders mas ainda sem carregar as páginas pesadas deles
     * @throws DatabaseException se falhar ao ler os dados
     */
    public List<Binder> listarBinders() throws DatabaseException {
        return dao.buscarTodos();
    }

    /**
     * procura um binder específico e já traz ele com todas as páginas dele juntas
     * @param id código de busca do binder
     * @return o binder completão pra abrir na tela ou nulo se não achar
     * @throws DatabaseException caso de erro na comunicação de leitura
     */
    public Binder buscarComPaginas(int id) throws DatabaseException {
        return dao.buscarPorIdComPaginas(id);
    }
}