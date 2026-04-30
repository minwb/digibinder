package digibinder.view;

import digibinder.model.Album;
import digibinder.view.components.AppTheme;
import digibinder.view.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * janela de formulário usada para cadastrar um novo álbum ou editar os dados de um álbum que já existe na coleção
 * reaproveita a mesma interface para ambas as funções dependendo dos parâmetros recebidos
 */
public class AlbumFormDialog extends JDialog {

    private JTextField campoNome;
    private JTextField campoVersao;
    private JSpinner spinnerAno;
    private JCheckBox checkSemAno;
    private boolean confirmado = false;

    /**
     * constrói a interface visual do formulário e preenche os campos automaticamente caso o usuário esteja editando um álbum
     * @param parent a janela principal do sistema que chamou este formulário
     * @param album o álbum que será editado ou nulo caso seja o cadastro de um novo
     */
    public AlbumFormDialog(Frame parent, Album album) {
        super(parent, album == null ? "Novo Álbum" : "Editar Álbum", true);

        setSize(420, 340);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BG_APP);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(AppTheme.BG_APP);
        painel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel(album == null ? "Adicionar Álbum" : "Editar Álbum");
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // nome
        JLabel lblNome = criarLabel("Nome do Álbum *");
        campoNome = criarCampo();
        if (album != null) campoNome.setText(album.getNome());

        // versão
        JLabel lblVersao = criarLabel("Versão (opcional)");
        campoVersao = criarCampo();
        if (album != null && album.getVersao() != null) campoVersao.setText(album.getVersao());

        // ano
        JLabel lblAno = criarLabel("Ano de Lançamento");

        SpinnerNumberModel model = new SpinnerNumberModel(
            album != null && album.getAnoLancamento() != null ? album.getAnoLancamento() : 2024,
            1990, 2100, 1
        );
        spinnerAno = new JSpinner(model);
        spinnerAno.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spinnerAno.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerAno.setFont(AppTheme.FONT_BODY);

        checkSemAno = new JCheckBox("Sem ano definido");
        checkSemAno.setOpaque(false);
        checkSemAno.setFont(AppTheme.FONT_BODY);
        checkSemAno.setForeground(AppTheme.TEXT_SECONDARY);
        checkSemAno.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkSemAno.setSelected(album != null && album.getAnoLancamento() == null);
        spinnerAno.setEnabled(!checkSemAno.isSelected());
        checkSemAno.addActionListener(e -> spinnerAno.setEnabled(!checkSemAno.isSelected()));

        // botões
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton btnCancelar = new StyledButton("Cancelar", StyledButton.Style.SECONDARY);
        StyledButton btnSalvar   = new StyledButton(album == null ? "Criar" : "Salvar");

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> { confirmado = true; dispose(); });

        botoes.add(btnCancelar);
        botoes.add(btnSalvar);

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(18));
        painel.add(lblNome);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campoNome);
        painel.add(Box.createVerticalStrut(12));
        painel.add(lblVersao);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campoVersao);
        painel.add(Box.createVerticalStrut(12));
        painel.add(lblAno);
        painel.add(Box.createVerticalStrut(4));
        painel.add(spinnerAno);
        painel.add(Box.createVerticalStrut(4));
        painel.add(checkSemAno);
        painel.add(Box.createVerticalStrut(20));
        painel.add(botoes);

        setContentPane(painel);
        getRootPane().setDefaultButton(btnSalvar);
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppTheme.FONT_LABEL);
        label.setForeground(AppTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField criarCampo() {
        JTextField campo = new JTextField();
        campo.setFont(AppTheme.FONT_BODY);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return campo;
    }

    /**
     * verifica se o usuário clicou no botão de salvar ou se apenas fechou a janela 
     * @return verdadeiro se os dados devem ser salvos
     */
    public boolean foiConfirmado(){ 
    	return confirmado; 
    }
    
    /**
     * recupera o texto que o usuário digitou no campo de nome já removendo espaços em branco extras
     * @return o nome do álbum formatado
     */
    public String  getNome(){ 
    	return campoNome.getText().trim(); 
    }
    
    /**
     * recupera o texto digitado no campo de versão
     * @return a versão do álbum ou texto vazio se não houver
     */
    public String  getVersao(){ 
    	return campoVersao.getText().trim(); 
    }
    
    /**
     * avalia se a caixa de seleção de ano indefinido foi marcada antes de retornar um valor
     * @return o ano numérico selecionado ou nulo se a caixa de ignorar ano estiver ativada
     */
    public Integer getAnoLancamento() {
        return checkSemAno.isSelected() ? null : (Integer) spinnerAno.getValue();
    }
}
