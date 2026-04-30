package digibinder.view;

import digibinder.controller.*;
import digibinder.exception.DatabaseException;
import digibinder.model.Album;
import digibinder.model.Grupo;
import digibinder.view.components.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * janela principal
 * contém a barra lateral de navegação e a área de conteúdo central, que é trocada conforme o usuário navega.
 * a MainFrame coordena a troca de Views 
 */
public class MainFrame extends JFrame {

    // controllers
    private GrupoController grupoController;
    private AlbumController albumController;
    private PhotocardController photocardController;
    private BinderController binderController;

    // layout
    private JPanel areaSidebar;
    private JPanel areaConteudo;

    // buttons
    private JLabel btnNavHome;
    private JLabel btnNavBinder;

    public MainFrame() {
        super("DigiBinder");

        inicializarControllers();
        configurarJanela();
        construirUI();
        voltarDashboard(); // tela inicial
    }

    // init

    private void inicializarControllers() {
        try {
            grupoController = new GrupoController();
            albumController  = new AlbumController();
            photocardController = new PhotocardController();
            binderController = new BinderController();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(null,
                "Erro ao inicializar banco de dados:\n" + e.getMessage() +
                "\n\nVerifique se o sqlite-jdbc.jar está no classpath.",
                "Erro de inicialização", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null); // centraliza na tela

        // fecha a conexão com o banco ao encerrar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    digibinder.database.DatabaseConnection.getInstance().fechar();
                } catch (DatabaseException ex) {
                    // apenas fecha a janela
                }
            }
        });

        // tenta aplicar o visual do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // ajustes de UI
        UIManager.put("OptionPane.background", AppTheme.BG_APP);
        UIManager.put("Panel.background", AppTheme.BG_APP);
        UIManager.put("OptionPane.messageFont", AppTheme.FONT_BODY);
    }

    private void construirUI() {
        setLayout(new BorderLayout());

        construirSidebar();
        construirAreaConteudo();
    }

    // sidebar

    private void construirSidebar() {
        areaSidebar = new JPanel();
        areaSidebar.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        areaSidebar.setBackground(AppTheme.BG_SIDEBAR);
        areaSidebar.setLayout(new BoxLayout(areaSidebar, BoxLayout.Y_AXIS));
        areaSidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // título do app
        JPanel logoArea = new JPanel(new BorderLayout());
        logoArea.setOpaque(false);
        logoArea.setBorder(new EmptyBorder(28, 22, 28, 22));

        JLabel lblApp = new JLabel("DigiBinder");
        lblApp.setFont(AppTheme.FONT_APP_TITLE);
        lblApp.setForeground(AppTheme.PRIMARY);
        logoArea.add(lblApp, BorderLayout.WEST);

        areaSidebar.add(logoArea);

        // separador
        areaSidebar.add(criarSeparadorSidebar());

        // itens de navegação
        btnNavHome   = criarNavItem("Grupos");
        btnNavBinder = criarNavItem("Binders");

        btnNavHome.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { 
            	voltarDashboard(); 
            }
        });
        btnNavBinder.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { 
            	abrirBinders(); 
            }
        });

        areaSidebar.add(criarNavWrapper(btnNavHome));
        areaSidebar.add(criarNavWrapper(btnNavBinder));

        areaSidebar.add(Box.createVerticalGlue());

        // vers no rodapé
        JLabel lblVersao = new JLabel("v1.0");
        lblVersao.setFont(AppTheme.FONT_SMALL);
        lblVersao.setForeground(new Color(0x55, 0x50, 0x4A));
        lblVersao.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel versaoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        versaoPanel.setOpaque(false);
        versaoPanel.add(lblVersao);
        areaSidebar.add(versaoPanel);
        areaSidebar.add(Box.createVerticalStrut(12));

        add(areaSidebar, BorderLayout.WEST);
    }

    private JLabel criarNavItem(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppTheme.FONT_BODY);
        label.setForeground(AppTheme.TEXT_LIGHT);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    private JPanel criarNavWrapper(JLabel item) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 22, 10, 22));
        wrapper.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 44));
        wrapper.add(item);

        wrapper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                wrapper.setBackground(new Color(0x38, 0x33, 0x2F));
                wrapper.setOpaque(true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                wrapper.setOpaque(false);
            }
        });

        return wrapper;
    }

    private JSeparator criarSeparadorSidebar() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x40, 0x3C, 0x38));
        sep.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 1));
        return sep;
    }

    // área de conteúdo

    private void construirAreaConteudo() {
        areaConteudo = new JPanel(new BorderLayout());
        areaConteudo.setBackground(AppTheme.BG_APP);
        areaConteudo.setBorder(new EmptyBorder(28, 28, 28, 28));

        add(areaConteudo, BorderLayout.CENTER);
    }

    /**
     * substitui o conteúdo central por um novo painel
     * todas as navegacoes passam por este método
     */
    private void trocarConteudo(JPanel novoConteudo) {
        areaConteudo.removeAll();
        areaConteudo.add(novoConteudo, BorderLayout.CENTER);
        areaConteudo.revalidate();
        areaConteudo.repaint();
    }

    // navegação

    /**
     * volta para a tela inicial com os grupos
     * 
     */
    public void voltarDashboard() {
        marcarNavAtivo(btnNavHome);
        trocarConteudo(new DashboardView(this, grupoController));
    }

    /**
     * abre a tela de álbuns de um grupo
     *
     * @param grupo grupo selecionado
     */
    public void abrirAlbums(Grupo grupo) {
        marcarNavAtivo(btnNavHome);
        trocarConteudo(new AlbumView(this, albumController, grupo));
    }

    /**
     * abre a grade de photocards de um álbum
     * @param album álbum selecionado
     */
    public void abrirPhotocards(Album album) {
        marcarNavAtivo(btnNavHome);
        Grupo grupo = album.getGrupo();
        trocarConteudo(new PhotocardGridView(this, photocardController, album, grupo));
    }

    /**
     * abre a tela de binders
     */
    public void abrirBinders() {
        marcarNavAtivo(btnNavBinder);
        trocarConteudo(new BinderView(this, binderController, photocardController,
                grupoController, albumController));
    }

    /** destaca visualmente o item de nav ativo */
    private void marcarNavAtivo(JLabel ativo) {
        for (JLabel item : new JLabel[]{btnNavHome, btnNavBinder}) {
            item.setForeground(item == ativo ? AppTheme.PRIMARY : AppTheme.TEXT_LIGHT);
        }
    }
}