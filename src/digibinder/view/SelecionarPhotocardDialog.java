package digibinder.view;

import digibinder.controller.AlbumController;
import digibinder.controller.GrupoController;
import digibinder.exception.DatabaseException;
import digibinder.model.Album;
import digibinder.model.Grupo;
import digibinder.model.Photocard;
import digibinder.model.StatusPhotocard;
import digibinder.view.components.AppTheme;
import digibinder.view.components.StyledButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * selecionar um photocard a ser alocado em um slot do binder
 * exibe os photocards agrupados por grupo/álbum em uma lista com miniaturas
 * permite filtrar por grupo e por texto
 */
public class SelecionarPhotocardDialog extends JDialog {

    private final GrupoController grupoController;
    private final AlbumController albumController;

    private Photocard fotocardSelecionado = null;
    private JPanel painelCards;
    private JComboBox<Object> comboGrupo;
    private JTextField campoBusca;

    // todos os photocards disponíveis (carregados uma vez)
    private List<Photocard> todosPhotocards = new ArrayList<>();

    // card atualmente destacado
    private JPanel cardDestacado = null;

    public SelecionarPhotocardDialog(Frame parent,GrupoController grupoController, AlbumController albumController) {
        super(parent, "Escolher Photocard para o Slot", true);
        this.grupoController = grupoController;
        this.albumController = albumController;

        setSize(620, 520);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(AppTheme.BG_APP);

        construirUI();
        carregarTodosPhotocards();
    }

    // UI

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));

        // filtros
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);
        topo.setBorder(new EmptyBorder(16, 18, 12, 18));

        // combo de grupo
        comboGrupo = new JComboBox<>();
        comboGrupo.setFont(AppTheme.FONT_BODY);
        comboGrupo.setBackground(AppTheme.BG_CARD);
        comboGrupo.addActionListener(e -> aplicarFiltros());

        // campo de busca por nome
        campoBusca = new JTextField();
        campoBusca.setFont(AppTheme.FONT_BODY);
        campoBusca.setPreferredSize(new Dimension(180, 32));
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { aplicarFiltros(); }
        });

        JLabel lblFiltro = new JLabel("Grupo:");
        lblFiltro.setFont(AppTheme.FONT_LABEL);
        lblFiltro.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel lblBusca = new JLabel("Membro:");
        lblBusca.setFont(AppTheme.FONT_LABEL);
        lblBusca.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.setOpaque(false);
        filtros.add(lblFiltro);
        filtros.add(comboGrupo);
        filtros.add(Box.createHorizontalStrut(10));
        filtros.add(lblBusca);
        filtros.add(campoBusca);

        JLabel instrucao = new JLabel("Clique em um card para selecionar, depois confirme.");
        instrucao.setFont(AppTheme.FONT_SMALL);
        instrucao.setForeground(AppTheme.TEXT_SECONDARY);

        topo.add(filtros, BorderLayout.NORTH);
        topo.add(instrucao, BorderLayout.SOUTH);

        // grid de cards 
        painelCards = new JPanel();
        painelCards.setOpaque(false);
        painelCards.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        painelCards.setBorder(new EmptyBorder(4, 14, 4, 14));

        JScrollPane scroll = new JScrollPane(painelCards);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(AppTheme.BG_APP);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // botões 
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setBorder(new EmptyBorder(10, 18, 16, 18));

        JLabel lblSelecionado = new JLabel("Nenhum card selecionado");
        lblSelecionado.setFont(AppTheme.FONT_BODY);
        lblSelecionado.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);

        StyledButton btnCancelar = new StyledButton("Cancelar", StyledButton.Style.SECONDARY);
        StyledButton btnConfirmar = new StyledButton("Alocar no Slot ✓");
        btnConfirmar.setEnabled(false);

        btnCancelar.addActionListener(e -> {
            fotocardSelecionado = null;
            dispose();
        });
        btnConfirmar.addActionListener(e -> dispose());

        botoes.add(btnCancelar);
        botoes.add(btnConfirmar);

        rodape.add(lblSelecionado, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);

     // guarda referências para usar no listener de seleção
        this.getRootPane().putClientProperty("lblSelecionado", lblSelecionado);
        this.getRootPane().putClientProperty("btnConfirmar", btnConfirmar);

        add(topo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
    }

    // carregamento

    private void carregarTodosPhotocards() {
        try {
            List<Grupo> grupos = grupoController.listarGrupos();

            comboGrupo.addItem("Todos os grupos");
            for (Grupo g : grupos) {
                comboGrupo.addItem(g);
            }

            // carrega todos os photocards de todos os grupos
            for (Grupo g : grupos) {
                todosPhotocards.addAll(albumController.listarPorGrupo(g.getId())
                    .stream()
                    .flatMap(a -> {
                        try {
                            return albumController.buscarPorId(a.getId()) != null
                                ? carregarPhotocardDoAlbum(a).stream()
                                : java.util.stream.Stream.empty();
                        } catch (Exception ex) {
                            return java.util.stream.Stream.empty();
                        }
                    })
                    .toList() 
                );
            }
            aplicarFiltros();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar photocards: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Photocard> carregarPhotocardDoAlbum(Album album) {
        try {
            // carregamos os photocards via AlbumController que já foi injetado
            return java.util.Collections.emptyList(); // placeholder — ver abaixo
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * injeta a lista de photocards diretamente (chamado pelo BinderView).
     */
    public void setPhotocards(List<Photocard> lista) {
        this.todosPhotocards = lista;
        aplicarFiltros();
    }

    // filtros

    private void aplicarFiltros() {
        String termoBusca = campoBusca.getText().trim().toLowerCase();
        Object grupoSel   = comboGrupo.getSelectedItem();

        List<Photocard> filtrados = todosPhotocards.stream()
            .filter(pc -> {
                // filtro de grupo
                if (grupoSel instanceof Grupo g) {
                    if (pc.getAlbum() == null) return false;
                    // verifica pelo nome do grupo embutido no álbum
                    if (pc.getAlbum().getGrupo() == null) return false;
                    if (!pc.getAlbum().getGrupo().getId().equals(g.getId())) return false;
                }
                // filtro de texto
                if (!termoBusca.isEmpty()) {
                    return pc.getMembro().toLowerCase().contains(termoBusca);
                }
                return true;
            })
            .toList();

        renderizarCards(filtrados);
    }

    private void renderizarCards(List<Photocard> lista) {
        painelCards.removeAll();
        cardDestacado = null;
        fotocardSelecionado = null;
        atualizarRodape();

        if (lista.isEmpty()) {
            JLabel vazio = new JLabel("Nenhum photocard encontrado.");
            vazio.setFont(AppTheme.FONT_BODY);
            vazio.setForeground(AppTheme.TEXT_SECONDARY);
            painelCards.add(vazio);
        } else {
            for (Photocard pc : lista) {
                painelCards.add(criarMiniCard(pc));
            }
        }

        painelCards.revalidate();
        painelCards.repaint();
    }

    // mini card

    private JPanel criarMiniCard(Photocard pc) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setPreferredSize(new Dimension(88, 130));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1),
            new EmptyBorder(4, 4, 6, 4)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // imagem / placeholder
        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(80, 90));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setVerticalAlignment(SwingConstants.CENTER);
        lblImg.setOpaque(true);
        lblImg.setBackground(AppTheme.PRIMARY_LIGHT);
        carregarMiniaturaNoLabel(pc, lblImg);

        // nome do membro
        JLabel lblNome = new JLabel(pc.getMembro(), SwingConstants.CENTER);
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblNome.setForeground(AppTheme.TEXT_PRIMARY);

        // álbum
        String nomeAlbum = pc.getAlbum() != null ? pc.getAlbum().getNome() : "";
        JLabel lblAlbum = new JLabel(nomeAlbum, SwingConstants.CENTER);
        lblAlbum.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        lblAlbum.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        lblNome.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAlbum.setAlignmentX(Component.CENTER_ALIGNMENT);
        textos.add(lblNome);
        textos.add(lblAlbum);

        card.add(lblImg,  BorderLayout.CENTER);
        card.add(textos,  BorderLayout.SOUTH);

        // clique seleciona o card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selecionarCard(card, pc);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (card != cardDestacado) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppTheme.PRIMARY, 1),
                        new EmptyBorder(4, 4, 6, 4)
                    ));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (card != cardDestacado) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                        new EmptyBorder(4, 4, 6, 4)
                    ));
                }
            }
        });

        return card;
    }

    private void carregarMiniaturaNoLabel(Photocard pc, JLabel alvo) {
        String caminho = pc.getCaminhoImagem();
        if (caminho == null || caminho.isBlank()) {
            // sem imagem mostra inicial do membro com fundo colorido
            alvo.setText(pc.getMembro().substring(0, 1).toUpperCase());
            alvo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            alvo.setForeground(AppTheme.PRIMARY_DARK);
            return;
        }
        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            alvo.setText("?");
            alvo.setFont(new Font("Segoe UI", Font.BOLD, 20));
            alvo.setForeground(AppTheme.TEXT_SECONDARY);
            return;
        }
        try {
            BufferedImage img = ImageIO.read(arquivo);
            if (img != null) {
                Image scaled = img.getScaledInstance(80, 90, Image.SCALE_SMOOTH);
                alvo.setIcon(new ImageIcon(scaled));
                alvo.setText(null);
            }
        } catch (IOException e) {
            alvo.setText("?");
        }
    }

    private void selecionarCard(JPanel card, Photocard pc) {
        // desmarca o anterior
        if (cardDestacado != null) {
            cardDestacado.setBackground(AppTheme.BG_CARD);
            cardDestacado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(4, 4, 6, 4)
            ));
        }
        // marca o novo
        card.setBackground(AppTheme.PRIMARY_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.PRIMARY_DARK, 2),
            new EmptyBorder(4, 4, 6, 4)
        ));
        cardDestacado = card;
        fotocardSelecionado = pc;
        atualizarRodape();
    }

    private void atualizarRodape() {
        JLabel lbl = (JLabel) this.getRootPane().getClientProperty("lblSelecionado");
        StyledButton btn = (StyledButton) this.getRootPane().getClientProperty("btnConfirmar");
        if (lbl == null || btn == null) return;

        if (fotocardSelecionado != null) {
            lbl.setText("Selecionado: " + fotocardSelecionado.getDescricao()
                + "  ·  " + fotocardSelecionado.getStatus().getNome());
            lbl.setForeground(AppTheme.PRIMARY_DARK);
            btn.setEnabled(true);
        } else {
            lbl.setText("Nenhum card selecionado");
            lbl.setForeground(AppTheme.TEXT_SECONDARY);
            btn.setEnabled(false);
        }
    }

    /** retorna o photocard selecionado, ou null se o usuário cancelou */
    public Photocard getPhotocardSelecionado() {
        return fotocardSelecionado;
    }

    // wrap layout

    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target);
        }

        private Dimension layoutSize(Container target) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right - hgap * 2;
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                for (Component m : target.getComponents()) {
                    if (m.isVisible()) {
                        Dimension d = m.getPreferredSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.height += rowHeight + vgap;
                            rowWidth = d.width + hgap;
                            rowHeight = d.height;
                        } else {
                            rowWidth += d.width + hgap;
                            rowHeight = Math.max(rowHeight, d.height);
                        }
                        dim.width = Math.max(dim.width, rowWidth);
                    }
                }
                dim.height += rowHeight + vgap + insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }
}