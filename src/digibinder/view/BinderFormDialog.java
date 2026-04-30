package digibinder.view;

import digibinder.view.components.AppTheme;
import digibinder.view.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * diálogo para criar um novo binder 
 */
public class BinderFormDialog extends JDialog {

    private JTextField campoNome;
    private JButton btnCorEscolhida;
    private Color corSelecionada;
    private boolean confirmado = false;

    public BinderFormDialog(Frame parent) {
        super(parent, "Novo Binder", true);

        setSize(400, 280);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BG_APP);

        corSelecionada = AppTheme.PRIMARY; 

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(AppTheme.BG_APP);
        painel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Criar Binder");
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNome = criarLabel("Nome do Binder *");
        campoNome = criarCampo();

        JLabel lblCor = criarLabel("Cor da Capa");

        // seletor de cor
        JPanel linhaCor = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaCor.setOpaque(false);
        linhaCor.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCorEscolhida = new JButton("      ");
        btnCorEscolhida.setBackground(corSelecionada);
        btnCorEscolhida.setPreferredSize(new Dimension(36, 28));
        btnCorEscolhida.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        btnCorEscolhida.setToolTipText("Clique para escolher a cor");
        btnCorEscolhida.addActionListener(e -> {
            Color nova = JColorChooser.showDialog(BinderFormDialog.this, "Escolher Cor da Capa", corSelecionada);
            if (nova != null) {
                corSelecionada = nova;
                btnCorEscolhida.setBackground(nova);
            }
        });

        JLabel lblCorHint = new JLabel("Clique para escolher");
        lblCorHint.setFont(AppTheme.FONT_SMALL);
        lblCorHint.setForeground(AppTheme.TEXT_SECONDARY);

        linhaCor.add(btnCorEscolhida);
        linhaCor.add(lblCorHint);

        // botões
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton btnCancelar = new StyledButton("Cancelar", StyledButton.Style.SECONDARY);
        StyledButton btnCriar    = new StyledButton("Criar");

        btnCancelar.addActionListener(e -> dispose());
        btnCriar.addActionListener(e -> { confirmado = true; dispose(); });

        botoes.add(btnCancelar);
        botoes.add(btnCriar);

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(18));
        painel.add(lblNome);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campoNome);
        painel.add(Box.createVerticalStrut(14));
        painel.add(lblCor);
        painel.add(Box.createVerticalStrut(4));
        painel.add(linhaCor);
        painel.add(Box.createVerticalStrut(22));
        painel.add(botoes);

        setContentPane(painel);
        getRootPane().setDefaultButton(btnCriar);
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

    public boolean foiConfirmado() { 
    	return confirmado; 
    }
    public String  getNome() { 
    	return campoNome.getText().trim(); 
    }

    /** retorna a cor em hexadecimal para o controller. */
    public String  getCorHex() {
        return String.format("#%02X%02X%02X",
            corSelecionada.getRed(),
            corSelecionada.getGreen(),
            corSelecionada.getBlue());
    }
}
