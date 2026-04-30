package digibinder.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * painel com cantos arredondados e sombra
 */
public class RoundedPanel extends JPanel {
    private final int radius;
    private Color bgColor;
    private boolean hasShadow;

    public RoundedPanel(int radius) {
        this(radius, AppTheme.BG_CARD, true);
    }

    public RoundedPanel(int radius, Color bgColor, boolean hasShadow) {
        this.radius = radius;
        this.bgColor = bgColor;
        this.hasShadow = hasShadow;
        setOpaque(false); 
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // bordas suaves
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (hasShadow) {
            // sombra sutil
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(3, 4, w - 4, h - 4, radius, radius);
        }

        // fundo
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w - (hasShadow ? 2 : 0), h - (hasShadow ? 2 : 0), radius, radius);

        g2.dispose();
    }
}
