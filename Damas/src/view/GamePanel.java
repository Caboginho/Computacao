package view;

import controller.GameController;
import model.Move;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Painel gráfico que representa o tabuleiro e as peças do jogo.
 * Responsável pela interface visual e interação do usuário.
 */
public class GamePanel extends JPanel {
    private final GameController controller;
    private final int squareSize;
    private Point selectedPiece;
    private List<Move> currentValidMoves;
    
    public GamePanel(GameController controller) {
        this.controller = controller;
        this.squareSize = Constants.SQUARE_SIZE;
        this.selectedPiece = null;
        this.currentValidMoves = List.of();
        
        setPreferredSize(new Dimension(
            Constants.BOARD_SIZE * squareSize, 
            Constants.BOARD_SIZE * squareSize
        ));
        
        setupMouseListener();
    }
    
    /**
     * Configura o listener para eventos de mouse
     */
    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }
    
    /**
     * Processa clique do mouse no tabuleiro
     */
    private void handleMouseClick(MouseEvent e) {
        int row = e.getY() / squareSize;
        int col = e.getX() / squareSize;
        
        if (!controller.getBoard().isValidPosition(row, col)) return;
        
        processBoardClick(row, col);
        repaint();
    }
    
    /**
     * Processa a lógica de clique em uma posição do tabuleiro
     */
    private void processBoardClick(int row, int col) {
        if (isDeselectingSamePiece(row, col)) {
            clearSelection();
            return;
        }
        
        if (isSelectingNewPiece(row, col)) {
            selectPiece(row, col);
            return;
        }
        
        if (isMakingMove(row, col)) {
            executeSelectedMove(row, col);
            return;
        }
        
        // Clique em outra peça ou posição inválida
        handleAlternativeClick(row, col);
    }
    
    private boolean isDeselectingSamePiece(int row, int col) {
        return selectedPiece != null && 
               selectedPiece.x == row && selectedPiece.y == col;
    }
    
    private boolean isSelectingNewPiece(int row, int col) {
        return selectedPiece == null && 
               controller.getBoard().getPiece(row, col) != Constants.EMPTY;
    }
    
    private boolean isMakingMove(int row, int col) {
        return selectedPiece != null && isValidMoveDestination(row, col);
    }
    
    /**
     * Seleciona uma peça para movimento
     */
    private void selectPiece(int row, int col) {
        if (!isValidPieceSelection(row, col)) {
            showMandatoryCaptureMessage();
            return;
        }
        
        selectedPiece = new Point(row, col);
        currentValidMoves = controller.getValidMoves(row, col);
    }
    
    private boolean isValidPieceSelection(int row, int col) {
        int piece = controller.getBoard().getPiece(row, col);
        int player = controller.getBoard().getPieceOwner(piece);
        boolean isCurrentPlayer = (controller.isPlayer1Turn() && player == 1) || 
                                 (!controller.isPlayer1Turn() && player == 2);
        
        if (!isCurrentPlayer) return false;
        
        // Verifica captura obrigatória
        List<Point> mandatoryPieces = controller.getMandatoryPieces();
        if (!mandatoryPieces.isEmpty()) {
            return isMandatoryPiece(row, col, mandatoryPieces);
        }
        
        return true;
    }
    
    private boolean isMandatoryPiece(int row, int col, List<Point> mandatoryPieces) {
        for (Point mandatory : mandatoryPieces) {
            if (mandatory.x == row && mandatory.y == col) {
                return true;
            }
        }
        return false;
    }
    
    private void showMandatoryCaptureMessage() {
        JOptionPane.showMessageDialog(this, 
            Constants.MSG_MANDATORY_CAPTURE + "\n" + Constants.MSG_SELECT_HIGHLIGHTED);
    }
    
    /**
     * Executa o movimento selecionado
     */
    private void executeSelectedMove(int row, int col) {
        Move chosenMove = findMoveForDestination(row, col);
        if (chosenMove != null) {
            controller.executeMove(selectedPiece.x, selectedPiece.y, chosenMove);
            updateSelectionAfterMove();
        }
    }
    
    private Move findMoveForDestination(int row, int col) {
        for (Move move : currentValidMoves) {
            if (move.getDestination().x == row && move.getDestination().y == col) {
                return move;
            }
        }
        return null;
    }
    
    private void updateSelectionAfterMove() {
        if (controller.isInCaptureSequence()) {
            Point sequencePiece = controller.getCaptureSequencePiece();
            selectedPiece = new Point(sequencePiece.x, sequencePiece.y);
            currentValidMoves = controller.getValidMoves(sequencePiece.x, sequencePiece.y);
        } else {
            clearSelection();
        }
    }
    
    /**
     * Trata clique em outra peça ou posição alternativa
     */
    private void handleAlternativeClick(int row, int col) {
        int piece = controller.getBoard().getPiece(row, col);
        
        if (piece != Constants.EMPTY) {
            // Tenta selecionar outra peça do mesmo jogador
            trySelectAlternativePiece(row, col);
        } else {
            // Clique em casa vazia - desseleciona
            clearSelection();
        }
    }
    
    private void trySelectAlternativePiece(int row, int col) {
        int piece = controller.getBoard().getPiece(row, col);
        int player = controller.getBoard().getPieceOwner(piece);
        boolean isCurrentPlayer = (controller.isPlayer1Turn() && player == 1) || 
                                 (!controller.isPlayer1Turn() && player == 2);
        
        if (isCurrentPlayer) {
            selectedPiece = new Point(row, col);
            currentValidMoves = controller.getValidMoves(row, col);
        }
    }
    
    private boolean isValidMoveDestination(int row, int col) {
        for (Move move : currentValidMoves) {
            if (move.getDestination().x == row && move.getDestination().y == col) {
                return true;
            }
        }
        return false;
    }
    
    private void clearSelection() {
        selectedPiece = null;
        currentValidMoves = List.of();
    }
    
    /**
     * Renderiza o componente gráfico
     */
    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        setupRenderingHints(g2d);
        drawBoard(g2d);
        highlightMandatoryPieces(g2d);
        highlightSelection(g2d);
        drawPieces(g2d);
    }
    
    private void setupRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
    
    /**
     * Desenha o tabuleiro
     */
    private void drawBoard(Graphics2D g2d) {
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                drawSquare(g2d, row, col);
            }
        }
    }
    
    private void drawSquare(Graphics2D g2d, int row, int col) {
        Color squareColor = ((row + col) % 2 == 0) ? 
            Constants.LIGHT_SQUARE : Constants.DARK_SQUARE;
        
        g2d.setColor(squareColor);
        g2d.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
    }
    
    /**
     * Destaca peças com captura obrigatória
     */
    private void highlightMandatoryPieces(Graphics2D g2d) {
        for (Point piece : controller.getMandatoryPieces()) {
            drawPieceHighlight(g2d, piece, Constants.HIGHLIGHT_COLOR, 4);
        }
    }
    
    /**
     * Destaca a peça selecionada e movimentos válidos
     */
    private void highlightSelection(Graphics2D g2d) {
        if (selectedPiece == null) return;
        
        // Destaca peça selecionada
        drawPieceHighlight(g2d, selectedPiece, Constants.HIGHLIGHT_COLOR, 3);
        
        // Destaca movimentos válidos
        highlightValidMoves(g2d);
    }
    
    private void highlightValidMoves(Graphics2D g2d) {
        for (Move move : currentValidMoves) {
            Point destination = move.getDestination();
            Color highlightColor = move.isCapture() ? 
                Constants.CAPTURE_HIGHLIGHT : Constants.MOVE_HIGHLIGHT;
            
            drawMoveHighlight(g2d, destination, highlightColor);
        }
    }
    
    private void drawPieceHighlight(Graphics2D g2d, Point piece, Color color, int strokeWidth) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawRect(
            piece.y * squareSize + strokeWidth, 
            piece.x * squareSize + strokeWidth, 
            squareSize - 2 * strokeWidth, 
            squareSize - 2 * strokeWidth
        );
    }
    
    private void drawMoveHighlight(Graphics2D g2d, Point destination, Color color) {
        g2d.setColor(color);
        int centerX = destination.y * squareSize + squareSize / 2;
        int centerY = destination.x * squareSize + squareSize / 2;
        int ovalSize = 36;
        
        g2d.fillOval(
            centerX - ovalSize / 2, 
            centerY - ovalSize / 2, 
            ovalSize, 
            ovalSize
        );
    }
    
    /**
     * Desenha as peças no tabuleiro
     */
    private void drawPieces(Graphics2D g2d) {
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                int piece = controller.getBoard().getPiece(row, col);
                if (piece != Constants.EMPTY) {
                    drawPiece(g2d, row, col, piece);
                }
            }
        }
    }
    
    private void drawPiece(Graphics2D g2d, int row, int col, int piece) {
        boolean isKing = controller.getBoard().isKing(piece);
        Color pieceColor = getPieceColor(piece);
        
        drawPieceShadow(g2d, row, col);
        drawPieceBody(g2d, row, col, pieceColor);
        drawPieceBorder(g2d, row, col, pieceColor);
        
        if (isKing) {
            drawKingCrown(g2d, row, col);
        }
    }
    
    private Color getPieceColor(int piece) {
        return (controller.getBoard().getPieceOwner(piece) == 1) ? 
            Constants.PLAYER1_COLOR : Constants.PLAYER2_COLOR;
    }
    
    private void drawPieceShadow(Graphics2D g2d, int row, int col) {
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(
            col * squareSize + 8, 
            row * squareSize + 8, 
            squareSize - 16, 
            squareSize - 16
        );
    }
    
    private void drawPieceBody(Graphics2D g2d, int row, int col, Color color) {
        g2d.setColor(color);
        g2d.fillOval(
            col * squareSize + 12, 
            row * squareSize + 12, 
            squareSize - 24, 
            squareSize - 24
        );
    }
    
    private void drawPieceBorder(Graphics2D g2d, int row, int col, Color color) {
        g2d.setColor(color.darker().darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(
            col * squareSize + 12, 
            row * squareSize + 12, 
            squareSize - 24, 
            squareSize - 24
        );
    }
    
    private void drawKingCrown(Graphics2D g2d, int row, int col) {
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        
        String crownSymbol = "♔";
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(crownSymbol);
        int textHeight = metrics.getHeight();
        
        g2d.drawString(
            crownSymbol, 
            col * squareSize + squareSize / 2 - textWidth / 2, 
            row * squareSize + squareSize / 2 + textHeight / 4
        );
    }
}