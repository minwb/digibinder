package digibinder.view;

import digibinder.controller.AlbumController;
import digibinder.controller.BinderController;
import digibinder.controller.GrupoController;
import digibinder.controller.PhotocardController;
import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.*;
import digibinder.view.components.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * tela de gerenciamento de binders
 * exibe a lista de binders na lateral e as páginas do binder selecionado
 * slots são clicáveis, slot vazio abre seleção de photocard, slot ocupado mostra menu para remover o card
 */
public class BinderView extends JPanel {

    private final BinderController binderController;
    private final PhotocardController photocardController;
    private final GrupoController grupoController;
    private final AlbumController albumController;
    private final MainFrame mainFrame;

    private JPanel painelListaBinders;
    private JPanel painelConteudoBinder;
    private Binder binderAtivo;

    public BinderView(MainFrame mainFrame,
                      BinderController binderController,
                      PhotocardController photocardController,
                      GrupoController grupoController,
                      AlbumController albumController) {
        this.mainFrame = mainFrame;
        this.binderController = binderController;
        this.photocardController = photocardController;
        this.grupoController = grupoController;
        this.albumController = albumController;

        setBackground(AppTheme.BG_APP);
        setLayout(new BorderLayout(20, 0));

        construirCabecalho();
        construirListaBinders();
        construirAreaConteudo();
        carregarBinders();
    }

    // header

    private void construirCabecalho() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titulo = new JLabel("Meus Binders");
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);

        StyledButton btnNovo = new StyledButton("Novo Binder");
        btnNovo.addActionListener(e -> abrirDialogNovoBinder());

        header.add(titulo,  BorderLayout.WEST);
        header.add(btnNovo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // lista lateral de binders

    private void construirListaBinders() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(220, 0));
        wrapper.setBackground(AppTheme.BG_CARD);
        wrapper.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));

        painelListaBinders = new JPanel();
        painelListaBinders.setLayout(new BoxLayout(painelListaBinders, BoxLayout.Y_AXIS));
        painelListaBinders.setBackground(AppTheme.BG_CARD);

        JScrollPane scroll = new JScrollPane(painelListaBinders);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        wrapper.add(scroll);
        add(wrapper, BorderLayout.WEST);
    }

    private void construirAreaConteudo() {
        painelConteudoBinder = new JPanel(new BorderLayout());
        painelConteudoBinder.setOpaque(false);

        JLabel placeholder = new JLabel("Selecione um binder para visualizar suas páginas");
        placeholder.setFont(AppTheme.FONT_BODY);
        placeholder.setForeground(AppTheme.TEXT_SECONDARY);
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        painelConteudoBinder.add(placeholder, BorderLayout.CENTER);

        add(painelConteudoBinder, BorderLayout.CENTER);
    }

    // carregamento

    public void carregarBinders() {
        painelListaBinders.removeAll();

        try {
            List<Binder> binders = binderController.listarBinders();

            if (binders.isEmpty()) {
                JLabel vazio = new JLabel("<html><center>Nenhum binder.<br>Crie um!</center></html>");
                vazio.setFont(AppTheme.FONT_BODY);
                vazio.setForeground(AppTheme.TEXT_SECONDARY);
                vazio.setBorder(new EmptyBorder(20, 12, 20, 12));
                painelListaBinders.add(vazio);
            } else {
                for (Binder binder : binders) {
                    painelListaBinders.add(criarItemBinder(binder));
                }
            }
        } catch (DatabaseException e) {
            mostrarErro(e.getMessage());
        }

        painelListaBinders.revalidate();
        painelListaBinders.repaint();
    }

    private JPanel criarItemBinder(Binder binder) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setBackground(AppTheme.BG_CARD);
        item.setBorder(new EmptyBorder(10, 14, 10, 14));
        item.setMaximumSize(new Dimension(220, 56));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bolinha = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(binder.getCorCapa());
                g.fillOval(0, 2, 14, 14);
            }
        };
        bolinha.setPreferredSize(new Dimension(14, 18));
        bolinha.setOpaque(false);

        JLabel lblNome = new JLabel(binder.getNome());
        lblNome.setFont(AppTheme.FONT_BODY);
        lblNome.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel btnDel = new JLabel("Excluir");
        btnDel.setFont(AppTheme.FONT_SMALL);
        btnDel.setForeground(AppTheme.TEXT_SECONDARY);
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                confirmarDelecaoBinder(binder);
            }
        });

        item.add(bolinha, BorderLayout.WEST);
        item.add(lblNome, BorderLayout.CENTER);
        item.add(btnDel, BorderLayout.EAST);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { 
            	selecionarBinder(binder); 
            }
           
            @Override public void mouseEntered(MouseEvent e) { 
            	item.setBackground(AppTheme.PRIMARY_LIGHT); 
            }
            
            @Override public void mouseExited(MouseEvent e)  { 
            	item.setBackground(AppTheme.BG_CARD); 
            }
        });

        return item;
    }

    private void selecionarBinder(Binder binder) {
        try {
            binderAtivo = binderController.buscarComPaginas(binder.getId());
            exibirConteudoBinder();
        } catch (DatabaseException e) {
            mostrarErro(e.getMessage());
        }
    }

    // conteúdo do binder

    private void exibirConteudoBinder() {
        painelConteudoBinder.removeAll();
        if (binderAtivo == null) return;

        // faixa colorida com nome do binder
        JPanel faixaCor = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        faixaCor.setBackground(binderAtivo.getCorCapa());

        JLabel lblNome = new JLabel(binderAtivo.getNome());
        lblNome.setFont(AppTheme.FONT_SUBTITLE);
        lblNome.setForeground(Color.WHITE);
        faixaCor.add(lblNome);

        StyledButton btnPagina = new StyledButton("Página");
        btnPagina.addActionListener(e -> abrirDialogNovaPagina());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(faixaCor,  BorderLayout.CENTER);
        header.add(btnPagina, BorderLayout.EAST);

        // dica de uso
        JLabel dica = new JLabel("Clique nos slots para adicionar photocards. Clique em um card para removê-lo.");
        dica.setFont(AppTheme.FONT_SMALL);
        dica.setForeground(AppTheme.TEXT_SECONDARY);
        dica.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel topoArea = new JPanel(new BorderLayout());
        topoArea.setOpaque(false);
        topoArea.add(header, BorderLayout.NORTH);
        topoArea.add(dica, BorderLayout.CENTER);

        // grid de páginas
        JPanel gridPaginas = new JPanel();
        gridPaginas.setOpaque(false);
        gridPaginas.setLayout(new DashboardView.WrapLayout(FlowLayout.LEFT, 18, 18));

        if (binderAtivo.getPaginas().isEmpty()) {
            JLabel vazio = new JLabel("Nenhuma página. Clique em 'Página' para adicionar.");
            vazio.setFont(AppTheme.FONT_BODY);
            vazio.setForeground(AppTheme.TEXT_SECONDARY);
            gridPaginas.add(vazio);
        } else {
            for (PaginaBinder pagina : binderAtivo.getPaginas()) {
                gridPaginas.add(criarCardPagina(pagina));
            }
        }

        JScrollPane scroll = new JScrollPane(gridPaginas);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        painelConteudoBinder.add(topoArea, BorderLayout.NORTH);
        painelConteudoBinder.add(scroll, BorderLayout.CENTER);

        painelConteudoBinder.revalidate();
        painelConteudoBinder.repaint();
    }

    // card de página

    private JPanel criarCardPagina(PaginaBinder pagina) {
        int cols = pagina.getLayout().getColunas();
        int rows = pagina.getLayout().getLinhas();
        int slotW = 72;
        int slotH = 100;

        RoundedPanel card = new RoundedPanel(AppTheme.RADIUS_CARD);
        card.setPreferredSize(new Dimension(cols * slotW + 32, rows * slotH + 70));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblPag = new JLabel("Página " + pagina.getNumeroPagina()
            + "  ·  " + pagina.getLayout().getDescricao());
        lblPag.setFont(AppTheme.FONT_SMALL);
        lblPag.setForeground(AppTheme.TEXT_SECONDARY);
        lblPag.setBorder(new EmptyBorder(0, 0, 8, 0));

        // grade de slots
        JPanel grade = new JPanel(new GridLayout(rows, cols, 5, 5));
        grade.setOpaque(false);

        try {
            List<Photocard> pcs = photocardController.listarPorPagina(pagina.getId());
            Map<String, Photocard> mapa = new HashMap<>();
            for (Photocard pc : pcs) {
                if (pc.getPosicaoX() != null && pc.getPosicaoY() != null) {
                    mapa.put(pc.getPosicaoY() + "," + pc.getPosicaoX(), pc);
                }
            }
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Photocard pc = mapa.get(r + "," + c);
                    grade.add(criarSlot(pagina, r, c, pc, slotW, slotH));
                }
            }
        } catch (DatabaseException e) {
            grade.add(new JLabel("Erro ao carregar slots"));
        }

        // remover página
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setOpaque(false);
        rodape.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel btnRemover = new JLabel("Remover página");
        btnRemover.setFont(AppTheme.FONT_SMALL);
        btnRemover.setForeground(new Color(0xCC, 0x5A, 0x5A));
        btnRemover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemover.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { confirmarRemocaoPagina(pagina); }
        });
        rodape.add(btnRemover);

        card.add(lblPag, BorderLayout.NORTH);
        card.add(grade,  BorderLayout.CENTER);
        card.add(rodape, BorderLayout.SOUTH);

        return card;
    }

    // slot clicável

    private JPanel criarSlot(PaginaBinder pagina, int row, int col,
                              Photocard pc, int slotW, int slotH) {
        JPanel slot = new JPanel(new BorderLayout(0, 2));
        slot.setPreferredSize(new Dimension(slotW, slotH));
        slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slot.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));

        if (pc != null) {
            // slot ocupado
            slot.setBackground(AppTheme.BG_CARD);
            slot.setToolTipText("<html><b>" + pc.getMembro() + "</b><br>"
                + pc.getDescricao() + "<br>"
                + pc.getStatus().getNome()
                + "<br><i>Clique para remover</i></html>");

            // miniatura da imagem
            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(SwingConstants.CENTER);
            lblImg.setBackground(AppTheme.PRIMARY_LIGHT);
            lblImg.setOpaque(true);
            carregarMiniaturaSlot(pc, lblImg, slotW - 4, slotH - 22);

            // nome do membro no topo
            JLabel lblNome = new JLabel(pc.getMembro(), SwingConstants.CENTER);
            lblNome.setFont(new Font("Segoe UI", Font.BOLD, 8));
            lblNome.setForeground(AppTheme.TEXT_PRIMARY);
            lblNome.setPreferredSize(new Dimension(slotW, 14));

            // barra de cor de status na base
            JPanel barraStatus = new JPanel();
            barraStatus.setBackground(AppTheme.corDoStatus(pc.getStatus()));
            barraStatus.setPreferredSize(new Dimension(slotW, 5));

            slot.add(lblNome, BorderLayout.NORTH);
            slot.add(lblImg, BorderLayout.CENTER);
            slot.add(barraStatus, BorderLayout.SOUTH);

            final Photocard pcFinal = pc;
            slot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem itemRemover = new JMenuItem(
                        "Remover " + pcFinal.getMembro() + " deste slot");
                    itemRemover.setFont(AppTheme.FONT_BODY);
                    itemRemover.addActionListener(ev -> removerDoSlot(pcFinal));
                    menu.add(itemRemover);
                    menu.show(slot, e.getX(), e.getY());
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    slot.setBorder(BorderFactory.createLineBorder(new Color(0xCC, 0x5A, 0x5A), 2));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    slot.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
                }
            });

        } else {
            // slot vazio
            slot.setBackground(new Color(0xF5, 0xF1, 0xED));
            slot.setToolTipText("Clique para adicionar um photocard");

            JLabel lblMais = new JLabel("+", SwingConstants.CENTER);
            lblMais.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblMais.setForeground(AppTheme.PRIMARY);
            slot.add(lblMais, BorderLayout.CENTER);

            slot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    abrirSeletorPhotocard(pagina, row, col);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    slot.setBackground(AppTheme.PRIMARY_LIGHT);
                    slot.setBorder(BorderFactory.createLineBorder(AppTheme.PRIMARY, 2));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    slot.setBackground(new Color(0xF5, 0xF1, 0xED));
                    slot.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
                }
            });
        }

        return slot;
    }

    // miniatura

    private void carregarMiniaturaSlot(Photocard pc, JLabel alvo, int w, int h) {
        alvo.setPreferredSize(new Dimension(w, h));
        String caminho = pc.getCaminhoImagem();

        if (caminho == null || caminho.isBlank()) {
            alvo.setText(pc.getMembro().substring(0, 1).toUpperCase());
            alvo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            alvo.setForeground(AppTheme.PRIMARY_DARK);
            return;
        }

        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            alvo.setText("?");
            alvo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            alvo.setForeground(AppTheme.TEXT_SECONDARY);
            return;
        }

        try {
            BufferedImage img = ImageIO.read(arquivo);
            if (img != null) {
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                alvo.setIcon(new ImageIcon(scaled));
                alvo.setText(null);
            }
        } catch (IOException e) {
            alvo.setText("?");
        }
    }

    // ações de slot

    private void abrirSeletorPhotocard(PaginaBinder pagina, int row, int col) {
        try {
            List<Photocard> todos = carregarTodosPhotocards();

            if (todos.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Nenhum photocard cadastrado.\n" +
                    "Cadastre photocards em Grupos -> Álbum primeiro.",
                    "Sem photocards", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            SelecionarPhotocardDialog dialog = new SelecionarPhotocardDialog(
                mainFrame, grupoController, albumController);
            dialog.setPhotocards(todos);
            dialog.setVisible(true);

            Photocard escolhido = dialog.getPhotocardSelecionado();
            if (escolhido != null) {
                photocardController.alocarNoPagina(
                    escolhido.getId(), pagina.getId(), col, row);
                selecionarBinder(binderAtivo); // recarrega
            }

        } catch (DatabaseException e) {
            mostrarErro("Erro ao alocar photocard: " + e.getMessage());
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(),
                "Validação", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removerDoSlot(Photocard pc) {
        try {
            photocardController.removerDoPagina(pc.getId());
            selecionarBinder(binderAtivo);
        } catch (DatabaseException e) {
            mostrarErro("Erro ao remover: " + e.getMessage());
        }
    }

    private List<Photocard> carregarTodosPhotocards() throws DatabaseException {
        List<Photocard> todos = new ArrayList<>();
        List<digibinder.model.Album> albuns = albumController.listarTodos();
        for (digibinder.model.Album album : albuns) {
            todos.addAll(photocardController.listarPorAlbum(album.getId()));
        }
        return todos;
    }

    // dialogs

    private void abrirDialogNovoBinder() {
        BinderFormDialog dialog = new BinderFormDialog(mainFrame);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            try {
                binderController.criarBinder(dialog.getNome(), dialog.getCorHex());
                carregarBinders();
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(),
                    "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void confirmarDelecaoBinder(Binder binder) {
        int resp = JOptionPane.showConfirmDialog(
            mainFrame, "Deletar o binder \"" + binder.getNome() + "\"?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                binderController.deletarBinder(binder.getId());
                binderAtivo = null;
                painelConteudoBinder.removeAll();
                carregarBinders();
                painelConteudoBinder.revalidate();
                painelConteudoBinder.repaint();
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void abrirDialogNovaPagina() {
        if (binderAtivo == null) return;

        String[] opcoes = {
            TipoLayout.GRID_2X2.getDescricao(),
            TipoLayout.GRID_3X3.getDescricao()
        };
        int resp = JOptionPane.showOptionDialog(
            mainFrame, "Escolha o layout da nova página:",
            "Nova Página", JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[1]);

        if (resp >= 0) {
            TipoLayout layout = resp == 0 ? TipoLayout.GRID_2X2 : TipoLayout.GRID_3X3;
            try {
                binderController.adicionarPagina(binderAtivo.getId(), layout);
                selecionarBinder(binderAtivo);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(),
                    "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void confirmarRemocaoPagina(PaginaBinder pagina) {
        int resp = JOptionPane.showConfirmDialog(
            mainFrame,
            "Remover a página " + pagina.getNumeroPagina() + "?\n"
            + "Os photocards alocados voltarão ao catálogo.",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                binderController.removerPagina(pagina.getId());
                selecionarBinder(binderAtivo);
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
