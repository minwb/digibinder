package digibinder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * representa um álbum físico na coleção, que pertence a um grupo e guarda vários photocards
 * funciona como uma pasta para organizar os itens colecionáveis de um lançamento 
 */
public class Album {
    // Integer permite valores nulos 
    private Integer id; 
    private Integer anoLancamento;
    
    private String nome;
    private String versao;
    
    private Grupo grupo;
    private List<Photocard> photocards;

    /**
     * cria um álbum do zero e prepara a lista na memória pra não dar erro na hora de receber os photocards
     */
    public Album() {
        this.photocards = new ArrayList<>();
    }

    /**
     * cria um álbum com os dados principais e reaproveita o construtor vazio pra garantir que a lista exista
     * @param nome nome do álbum
     * @param versao versão dele tipo A ou B
     * @param anoLancamento ano que ele foi lançado
     */
    public Album(String nome, String versao, Integer anoLancamento) {
        this(); // chama o construtor vazio
        this.nome = nome;
        this.versao = versao;
        this.anoLancamento = anoLancamento;
    }

    /**
     * construtor completo que amarra o álbum no grupo pai e define o número de ID do banco
     * @param id código do álbum
     * @param nome nome do álbum
     * @param versao versão dele
     * @param anoLancamento ano de lançamento
     * @param grupo grupo dono desse álbum
     */
    public Album(Integer id, String nome, String versao, Integer anoLancamento, Grupo grupo) {
        this(nome, versao, anoLancamento); // chama o construtor de cima
        this.id = id;
        this.grupo = grupo;
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

    public String getVersao() {
        return versao;
    }
    public void setVersao(String versao) {
        this.versao = versao;
    }

    public Integer getAnoLancamento() {
        return anoLancamento;
    }
    public void setAnoLancamento(Integer anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public Grupo getGrupo() {
        return grupo;
    }
    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public List<Photocard> getPhotocards() {
        return photocards;
    }
    public void setPhotocards(List<Photocard> photocards) {
        this.photocards = photocards;
    }

    /**
     * guarda um photocard dentro do álbum e garante que ele não seja adicionado duas vezes por engano
     * @param photocard o card que tá sendo guardado
     */
    public void adicionarPhotocard(Photocard photocard) {
        if (!this.photocards.contains(photocard)) { 
            this.photocards.add(photocard); 
            photocard.setAlbum(this); 
        }
    }

    /**
     * junta o nome do álbum com a versão dele pra ficar coerente na tela do usuário
     * @return o nome completo formatado
     */
    public String getNomeCompleto() {
        if (versao != null && !versao.isEmpty()) { 
            return nome + " - " + versao;
        } else {
            return nome;
        }
    }
    
    /**
     * java compara dois álbuns nas listas
     * são exatamente o mesmo álbum se tiverem o mesmo id do banco de dados, evitando duplicatas
     * @param obj o outro objeto que a gente quer comparar
     * @return verdadeiro se for o mesmo álbum
     */
    @Override
    public boolean equals(Object obj) {  
        if (this == obj) { 
        	return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
        	return false;
        }
        Album album = (Album) obj;  
        return Objects.equals(id, album.id); 
    }

    /**
     * cria um valor único baseada no id pra otimizar a velocidade das buscas
     * @return o código numérico gerado
     */
    @Override
    public int hashCode() {  
        return Objects.hash(id); 
    }

    /**
     * define como o álbum vai aparecer escrito caso o sistema tente printar ele diretamente na tela
     * @return o nome completo do álbum
     */
    @Override
    public String toString() {
        return getNomeCompleto();
    }
}