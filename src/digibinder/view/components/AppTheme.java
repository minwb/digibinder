package digibinder.view.components;

import java.awt.Color;
import java.awt.Font;

/**
 * paleta de cores e fontes 
 * todas as views utilizam estas constantes para manter consistência visual
 */
public final class AppTheme {

    private AppTheme() {} // util

    // cores
    /** cor principal, cabeçalhos e destaque */
    public static final Color PRIMARY       = new Color(0xC4, 0xA8, 0x82);
    /** hover e bordas */
    public static final Color PRIMARY_DARK  = new Color(0xA0, 0x86, 0x64);
    /** fondos suaves */
    public static final Color PRIMARY_LIGHT = new Color(0xEE, 0xE5, 0xD8);
    /** fundo principal */
    public static final Color BG_APP        = new Color(0xF7, 0xF3, 0xEF);
    /** fundo de cards e painéis brancos */
    public static final Color BG_CARD       = Color.WHITE;
    /** fundo da barra lateral */
    public static final Color BG_SIDEBAR    = new Color(0x2B, 0x27, 0x24);
    /** texto principal */
    public static final Color TEXT_PRIMARY  = new Color(0x1A, 0x1A, 0x1A);
    /** texto secundario */
    public static final Color TEXT_SECONDARY = new Color(0x88, 0x80, 0x78);
    /** ttexto em sidebar */
    public static final Color TEXT_LIGHT    = new Color(0xF0, 0xEB, 0xE4);
    /** separadores e bordas */
    public static final Color BORDER        = new Color(0xE5, 0xDF, 0xD8);

    // status
    /** ADQUIRIDO */
    public static final Color STATUS_ADQUIRIDO  = new Color(0x7C, 0xB8, 0x8A);
    /** DESEJADO */
    public static final Color STATUS_DESEJADO   = new Color(0x8A, 0xAF, 0xCC);
    /** PARA TROCA */
    public static final Color STATUS_TROCA      = new Color(0xE0, 0xA0, 0x6A);

    // fontes
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_APP_TITLE = new Font("Segoe UI", Font.BOLD, 18);

    // métricas
    public static final int RADIUS_CARD  = 14;
    public static final int RADIUS_BUTTON = 8;
    public static final int SIDEBAR_WIDTH = 200;
    
    // helpers

    /**
     * retorna a cor de fundo do badge correspondente ao status do photocard
     * @param status status do photocard
     * @return cor do badge
     */
    public static Color corDoStatus(digibinder.model.StatusPhotocard status) {
        return switch (status) {
            case ADQUIRIDO -> STATUS_ADQUIRIDO;
            case DESEJADO -> STATUS_DESEJADO;
            case PARA_TROCA -> STATUS_TROCA;
        };
    }
}
