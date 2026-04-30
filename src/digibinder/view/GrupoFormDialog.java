package digibinder.view;

import digibinder.model.Grupo;
import digibinder.view.components.AppTheme;
import digibinder.view.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * criação e edição de grupos
 * utilizado tanto para novos grupos quanto para edição dos existentes
 */
public class GrupoFormDialog extends JDialog {

    private JTextField campoNome;
    private JTextField campoDataDebut;
    private boolean confirmado = false;

    /**
     * @param parent janela pai
     * @param grupo grupo a editar, ou null para criar novo
     */
    public GrupoFormDialog(Frame parent, Grupo grupo) {
        super(parent, grupo == null ? "Novo Grupo" : "Editar Grupo", true);

        setSize(420, 280);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BG_APP);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(AppTheme.BG_APP);
        painel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // título do dialog
        JLabel titulo = new JLabel(grupo == null ? "Adicionar Grupo" : "Editar Grupo");
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // campo: Nome
        JLabel lblNome = criarLabel("Nome do Grupo *");
        campoNome = criarCampo();
        if (grupo != null) campoNome.setText(grupo.getNome());

        // campo: data de Debut
        JLabel lblData = criarLabel("Data de Debut (AAAA-MM-DD)");
        campoDataDebut = criarCampo();
        if (grupo != null && grupo.getDataDebut() != null) {
            campoDataDebut.setText(grupo.getDataDebut().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        // botões
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton btnCancelar = new StyledButton("Cancelar", StyledButton.Style.SECONDARY);
        StyledButton btnSalvar = new StyledButton(grupo == null ? "Criar" : "Salvar");

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> { confirmado = true; dispose(); });

        botoes.add(btnCancelar);
        botoes.add(btnSalvar);

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(20));
        painel.add(lblNome);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campoNome);
        painel.add(Box.createVerticalStrut(14));
        painel.add(lblData);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campoDataDebut);
        painel.add(Box.createVerticalStrut(24));
        painel.add(botoes);

        setContentPane(painel);

        // enter confirma
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

    /** retorna true se o usuário confirmou o formulário */
    public boolean foiConfirmado() { return confirmado; }

    /** retorna o nome digitado */
    public String getNome() { return campoNome.getText().trim(); }

    /** retorna a data de debut digitada (pode ser vazio) */
    public String getDataDebut() { return campoDataDebut.getText().trim(); }
}
