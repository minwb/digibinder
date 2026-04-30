package digibinder.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * representa o fichário virtual onde o usuário organiza suas páginas e os photocards
 * é o equivalente digital daquelas pastas com folhas de plástico que os colecionadores usam
 */
public class Binder {
    private Integer id;
    private String nome;
    private Color corCapa; // classe do java para guardar cores 
    
    private List<PaginaBinder> paginas;

    /**
     * cria um binder vazio já com a lista de páginas pronta na memória e uma cor de capa padrão
     */
    public Binder() {
        this.paginas = new ArrayList<>();
        // define uma cor padrao
        this.corCapa = new Color(180, 150, 130); 
    }

    /**
     * cria um binder informando o nome e a cor da capa, e reaproveita o construtor vazio pra inicializar a lista
     * @param nome o título que vai ficar na capa do binder
     * @param corCapa a cor escolhida pelo usuário
     */
    public Binder(String nome, Color corCapa) {
        this(); // chama o construtor vazio
        this.nome = nome;
        if (corCapa != null) {
            this.corCapa = corCapa;
        }
    }

    /**
     * construtor completo que traz o código do banco de dados junto com o nome e a cor
     * @param id código do binder no banco
     * @param nome o título do binder
     * @param corCapa a cor da capa
     */
    public Binder(Integer id, String nome, Color corCapa) {
        this(nome, corCapa); 
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public Color getCorCapa() {
        return corCapa;
    }
    public void setCorCapa(Color corCapa) {
        this.corCapa = corCapa;
    }

    public List<PaginaBinder> getPaginas() {
        return paginas;
    }
    public void setPaginas(List<PaginaBinder> paginas) {
        this.paginas = paginas;
    }

    /**
     * guarda uma página nova dentro do binder e avisa pra página quem é o dono dela
     * @param pagina a folha virtual que tá sendo colocada no fichário
     */
    public void adicionarPagina(PaginaBinder pagina) {
        if (!this.paginas.contains(pagina)) { 
            this.paginas.add(pagina); 
            pagina.setBinder(this); 
        }
    }

    /**
     * exclui uma página específica de dentro do binder
     * @param pagina a folha que vai ser removida
     */
    public void removerPagina(PaginaBinder pagina) {
        this.paginas.remove(pagina); 
    }

    /**
     * comparação entre dois binders nas listas e diz que eles são o mesmo binder se tiverem o mesmo id do banco
     * @param obj o outro objeto que tá sendo comparado
     * @return verdadeiro se for o mesmo binder
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
        	return false;
        }
        Binder binder = (Binder) obj;
        return Objects.equals(id, binder.id);
    }

    /**
     * cria um valor unico baseado no id pra otimizaçãod e buscas
     * * @return o identificador gerado
     */
    @Override
    public int hashCode() { 
        return Objects.hash(id); 
    }

    /**
     * definição de escrita do binder
     * @return o nome dele seguido da quantidade de páginas que tem dentro
     */
    @Override
    public String toString() {
        return nome + " (" + paginas.size() + " páginas)";
    }
}