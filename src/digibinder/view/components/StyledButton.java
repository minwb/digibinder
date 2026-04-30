package digibinder.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * botão personalizado com visual arredondado 
 */
public class StyledButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, DANGER }

    private Style style;
    private Color normalBg;
    private Color hoverBg;
    private Color textColor;
    private boolean hovering = false;

    public StyledButton(String texto, Style style) {
        super(texto);
        this.style = style;
        configurar();
    }

    public StyledButton(String texto) {
        this(texto, Style.PRIMARY);
    }

    private void configurar() {
        normalBg = switch (style) {
            case PRIMARY -> AppTheme.PRIMARY;
            case SECONDARY -> AppTheme.BG_APP;
            case DANGER -> new Color(0xCC, 0x6A, 0x6A);
        };
        hoverBg = switch (style) {
            case PRIMARY -> AppTheme.PRIMARY_DARK;
            case SECONDARY -> AppTheme.BORDER;
            case DANGER -> new Color(0xAA, 0x50, 0x50);
        };
        textColor = switch (style) {
            case PRIMARY -> Color.WHITE;
            case SECONDARY -> AppTheme.TEXT_PRIMARY;
            case DANGER -> Color.WHITE;
        };

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(AppTheme.FONT_LABEL);
        setForeground(textColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e){
            	hovering = true;  repaint(); 
            }
            @Override
            public void mouseExited(MouseEvent e){ 
            	hovering = false; repaint(); 
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(hovering ? hoverBg : normalBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BUTTON, AppTheme.RADIUS_BUTTON);

        g2.dispose();
        super.paintComponent(g);
    }
}