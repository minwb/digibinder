package digibinder.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * representa um grupo musical ou solista no sistema
 * serve como o ponto de partida e a categoria principal para organizar todos os álbuns e cards
 */
public class Grupo {
    private Integer id; 
    
    private String nome;
  
    private LocalDate dataDebut; 
    
    private List<Album> albuns;

    /**
     * cria um grupo do zero e já prepara a lista de álbuns na memória pra não dar erro depois
     */
    public Grupo() {
        this.albuns = new ArrayList<>();
    }

    /**
     * cria um grupo informando o nome e a data de debut, aproveitando o construtor vazio pra garantir a lista de álbuns
     * @param nome o nome do artista
     * @param dataDebut quando eles estrearam
     */
    public Grupo(String nome, LocalDate dataDebut) {
        this(); 
        this.nome = nome;
        this.dataDebut = dataDebut;
    }

    /**
     * construtor que traz a ID do banco junto com os dados do grupo
     * @param id código do grupo no banco de dados
     * @param nome nome do artista
     * @param dataDebut data de estreia
     */
    public Grupo(Integer id, String nome, LocalDate dataDebut) {
        this(nome, dataDebut);
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

    public LocalDate getDataDebut() {
        return dataDebut;
    }
    public void setDataDebut(LocalDate dataDebut) {
        this.dataDebut = dataDebut;
    }

    public List<Album> getAlbuns() {
        return albuns;
    }
    public void setAlbuns(List<Album> albuns) {
        this.albuns = albuns;
    }

    /**
     * guarda um álbum dentro da lista do grupo e avisa pro álbum quem é o grupo dono dele
     * @param album o álbum que tá sendo adicionado
     */
    public void adicionarAlbum(Album album) {
        if (!this.albuns.contains(album)) { 
            this.albuns.add(album); 
            album.setGrupo(this); 
        }
    }

    /**
     * avalia se dois grupos são iguais na lista
     * são o mesmo grupo só se tiverem o mesmo id e o mesmo nome juntos
     * @param obj o outro objeto que queremos comparar
     * @return verdadeiro se for o exato mesmo grupo
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
        	return false;
        }
        Grupo grupo = (Grupo) obj;
        return Objects.equals(id, grupo.id) && Objects.equals(nome, grupo.nome);
    }

    /**
     * cria um valoro único usando o id e o nome pra otimizar buscas
     * @return o identificador gerado
     */
    @Override
    public int hashCode() { 
        return Objects.hash(id, nome); 
    }

    /**
     * definição de impressão do grupo
     * @return o nome do grupo
     */
    @Override
    public String toString() {
        return nome; 
    }
}