package digibinder.controller;

import digibinder.database.GrupoDAO;
import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Grupo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * faz a ponte entre a tela e o banco de dados para os grupos
 * aplica as verificações pra não acontecer cadastro de um grupo sem nome ou com data inválida
 */
public class GrupoController {

    private final GrupoDAO dao;

    public GrupoController() throws DatabaseException {
        this.dao = new GrupoDAO();
    }

    /**
     * cria um grupo e joga pro banco de dados
     * @param nome o nome escolhido pro grupo
     * @param dataDebut a data que eles estrearam no formato ano-mes-dia
     * @return o grupo já montado e com o id que o banco gerou
     * @throws ValidationException se o nome for vazio ou o usuário errar o formato da data
     * @throws DatabaseException se der erro na gravação
     */
    public Grupo criarGrupo(String nome, String dataDebut)
            throws ValidationException, DatabaseException {

        if (nome == null || nome.isBlank()) {
            throw new ValidationException("O nome do grupo não pode ser vazio.");
        }

        Grupo grupo = new Grupo();
        grupo.setNome(nome.trim());

        if (dataDebut != null && !dataDebut.isBlank()) {
            try {
                grupo.setDataDebut(LocalDate.parse(dataDebut.trim()));
            } catch (DateTimeParseException e) {
                throw new ValidationException("Data de debut inválida. Formato: AAAA-MM-DD");
            }
        }

        dao.salvar(grupo);
        return grupo;
    }

    /**
     * muda as informações de um grupo que já tá salvo no sistema
     * @param id código do grupo que vai ser editado
     * @param novoNome o novo nome digitado lá no formulário
     * @param dataDebut a nova data de debut escolhida
     * @throws ValidationException caso as novas infos violem alguma regra
     * @throws DatabaseException se o update falhar no banco
     */
    public void atualizarGrupo(int id, String novoNome, String dataDebut)
            throws ValidationException, DatabaseException {

        if (novoNome == null || novoNome.isBlank()) {
            throw new ValidationException("O nome do grupo não pode ser vazio.");
        }

        Grupo grupo = new Grupo();
        grupo.setId(id);
        grupo.setNome(novoNome.trim());

        if (dataDebut != null && !dataDebut.isBlank()) {
            try {
                grupo.setDataDebut(LocalDate.parse(dataDebut.trim()));
            } catch (DateTimeParseException e) {
                throw new ValidationException("Data de debut inválida. Use o formato: AAAA-MM-DD");
            }
        }

        dao.atualizar(grupo);
    }

    /**
     * apaga o grupo inteiro do sistema
     * apaga os álbuns e photocards desse grupo
     * @param id código do grupo a ser apagado
     * @throws DatabaseException se alguma trava impedir de deletar
     */
    public void deletarGrupo(int id) throws DatabaseException {
        dao.deletar(id);
    }

    /**
     * puxa a lista completa de todos os grupos em ordem alfabética pra exibir na tela principal
     * @return a lista de grupos prontinha pra usar
     * @throws DatabaseException caso falhe ao ler os grupos
     */
    public List<Grupo> listarGrupos() throws DatabaseException {
        return dao.buscarTodos();
    }

    /**
     * pesquisa um grupo usando o número de registro dele
     * @param id código numérico do grupo
     * @return o grupo encontrado ou nulo se ele nunca existiu
     * @throws DatabaseException se ocorrer falha na comunicação com o banco
     */
    public Grupo buscarPorId(int id) throws DatabaseException {
        return dao.buscarPorId(id);
    }
}