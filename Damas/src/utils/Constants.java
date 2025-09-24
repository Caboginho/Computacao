package utils;

import java.awt.Color;

public class Constants {
    public static final int BOARD_SIZE = 8;
    public static final int SQUARE_SIZE = 90;
    
    // Códigos das peças
    public static final int EMPTY = 0;
    public static final int PLAYER1_PAWN = 1;
    public static final int PLAYER2_PAWN = 2;
    public static final int PLAYER1_KING = 11;
    public static final int PLAYER2_KING = 22;
    
    // Cores
    public static final Color LIGHT_SQUARE = new Color(245, 222, 179);
    public static final Color DARK_SQUARE = new Color(139, 69, 19);
    public static final Color PLAYER1_COLOR = new Color(220, 20, 60);
    public static final Color PLAYER2_COLOR = new Color(30, 144, 255);
    public static final Color HIGHLIGHT_COLOR = new Color(255, 215, 0, 180);
    public static final Color CAPTURE_HIGHLIGHT = new Color(255, 69, 0, 150);
    public static final Color MOVE_HIGHLIGHT = new Color(50, 205, 50, 150);
    
    // Sons
    public static final String SOUND_MOVE = "sounds/move.wav";
    public static final String SOUND_CAPTURE = "sounds/capture.wav";
    public static final String SOUND_KING = "sounds/king.wav";
    public static final String SOUND_WIN = "sounds/win.wav";
    
    // Mensagens
    public static final String MSG_MANDATORY_CAPTURE = "Existe captura obrigatória!";
    public static final String MSG_SELECT_HIGHLIGHTED = "Selecione uma das peças destacadas em amarelo.";
    public static final String MSG_PLAYER1_WIN = "Vitória do Jogador 1 (Vermelho)!";
    public static final String MSG_PLAYER2_WIN = "Vitória do Jogador 2 (Azul)!";
    public static final String MSG_DRAW = "Empate! (Regra dos 20 lances)";
    public static final String MSG_REMAINING_PIECES = "Peças restantes:";
}