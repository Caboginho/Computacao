package controller;

import model.Board;
import model.Move;
import utils.Constants;
import view.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal do jogo de damas.
 * Gerencia a lógica do jogo, turnos e regras.
 */
public class GameController {
    private final Board board;
    private boolean isPlayer1Turn;
    private boolean inCaptureSequence;
    private Point captureSequencePiece;
    private List<Point> mandatoryPieces;
    private SoundManager soundManager;
    
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private int player1Wins;
    private int player2Wins;
    private boolean showingEndDialog;
    private int movesWithoutCaptureOrPawn;
    
    public GameController() {
        this.board = new Board();
        this.mandatoryPieces = new ArrayList<>();
        this.soundManager = new SoundManager();
        this.statusLabel = new JLabel("Pronto");
        this.scoreLabel = new JLabel("Placar: P1 0 - P2 0");
        
        initializeGame();
    }
    
    /**
     * Inicializa ou reinicia o jogo
     */
    public void initializeGame() {
        board.initialize();
        isPlayer1Turn = true;
        inCaptureSequence = false;
        captureSequencePiece = null;
        movesWithoutCaptureOrPawn = 0;
        
        updateMandatoryPieces();
        updateStatusLabel();
    }
    
    // Getters para a view
    public Board getBoard() { return board; }
    public JLabel getStatusLabel() { return statusLabel; }
    public JLabel getScoreLabel() { return scoreLabel; }
    public List<Point> getMandatoryPieces() { return mandatoryPieces; }
    public boolean isInCaptureSequence() { return inCaptureSequence; }
    public Point getCaptureSequencePiece() { return captureSequencePiece; }
    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    
    /**
     * Atualiza o rótulo de status com informações do turno atual
     */
    public void updateStatusLabel() {
        String player = isPlayer1Turn ? "Jogador 1 (Vermelho)" : "Jogador 2 (Azul)";
        String sequence = inCaptureSequence ? " [Sequência de Captura]" : "";
        statusLabel.setText("Turno: " + player + sequence);
        scoreLabel.setText(String.format("Placar: P1 %d - P2 %d", player1Wins, player2Wins));
    }
    
    /**
     * Atualiza a lista de peças com capturas obrigatórias
     */
    private void updateMandatoryPieces() {
        mandatoryPieces.clear();
        int currentPlayer = isPlayer1Turn ? 1 : 2;
        
        for (Point piece : board.getPieces(currentPlayer)) {
            if (hasCaptureMoves(piece.x, piece.y)) {
                mandatoryPieces.add(piece);
            }
        }
    }
    
    /**
     * Obtém movimentos válidos para uma peça específica
     */
    public List<Move> getValidMoves(int row, int col) {
        List<Move> moves = new ArrayList<>();
        int piece = board.getPiece(row, col);
        
        if (piece == Constants.EMPTY) return moves;
        
        int currentPlayer = isPlayer1Turn ? 1 : 2;
        if (board.getPieceOwner(piece) != currentPlayer) return moves;
        
        // Verifica restrições de sequência de captura
        if (inCaptureSequence && !isCaptureSequencePiece(row, col)) {
            return moves;
        }
        
        moves = generateMoves(row, col);
        
        // Filtra apenas capturas se houver peças obrigatórias
        if (!mandatoryPieces.isEmpty()) {
            return filterCaptureMoves(moves);
        }
        
        return moves;
    }
    
    private boolean isCaptureSequencePiece(int row, int col) {
        return captureSequencePiece != null && 
               captureSequencePiece.x == row && captureSequencePiece.y == col;
    }
    
    private List<Move> filterCaptureMoves(List<Move> moves) {
        List<Move> captureMoves = new ArrayList<>();
        for (Move move : moves) {
            if (move.isCapture()) {
                captureMoves.add(move);
            }
        }
        return captureMoves;
    }
    
    /**
     * Gera todos os movimentos possíveis para uma peça
     */
    private List<Move> generateMoves(int row, int col) {
        int piece = board.getPiece(row, col);
        
        if (board.isKing(piece)) {
            return generateKingMoves(row, col);
        } else {
            return generatePawnMoves(row, col);
        }
    }
    
    private List<Move> generatePawnMoves(int row, int col) {
        List<Move> moves = new ArrayList<>();
        int piece = board.getPiece(row, col);
        int direction = (piece == Constants.PLAYER1_PAWN) ? 1 : -1;
        
        // Movimentos simples
        addSimplePawnMoves(moves, row, col, direction);
        
        // Movimentos de captura
        addCapturePawnMoves(moves, row, col, direction);
        
        return moves;
    }
    
    private void addSimplePawnMoves(List<Move> moves, int row, int col, int direction) {
        int[][] directions = {{direction, -1}, {direction, 1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (isValidMovePosition(newRow, newCol)) {
                moves.add(new Move(newRow, newCol));
            }
        }
    }
    
    private void addCapturePawnMoves(List<Move> moves, int row, int col, int direction) {
        int[][] directions = {{direction, -1}, {direction, 1}};
        
        for (int[] dir : directions) {
            int jumpRow = row + 2 * dir[0];
            int jumpCol = col + 2 * dir[1];
            int middleRow = row + dir[0];
            int middleCol = col + dir[1];
            
            if (isValidCapture(row, col, middleRow, middleCol, jumpRow, jumpCol)) {
                Move captureMove = new Move(jumpRow, jumpCol);
                captureMove.addCapturedPiece(new Point(middleRow, middleCol));
                moves.add(captureMove);
            }
        }
    }
    
    private List<Move> generateKingMoves(int row, int col) {
        List<Move> moves = new ArrayList<>();
        int piece = board.getPiece(row, col);
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        
        for (int[] dir : directions) {
            // Movimentos simples
            addSimpleKingMoves(moves, row, col, dir);
            
            // Movimentos de captura
            addCaptureKingMoves(moves, row, col, piece, dir);
        }
        
        return moves;
    }
    
    private void addSimpleKingMoves(List<Move> moves, int row, int col, int[] dir) {
        for (int distance = 1; distance < Constants.BOARD_SIZE; distance++) {
            int newRow = row + distance * dir[0];
            int newCol = col + distance * dir[1];
            
            if (!isValidMovePosition(newRow, newCol)) break;
            
            moves.add(new Move(newRow, newCol));
        }
    }
    
    private void addCaptureKingMoves(List<Move> moves, int row, int col, int piece, int[] dir) {
        for (int distance = 1; distance < Constants.BOARD_SIZE; distance++) {
            int enemyRow = row + distance * dir[0];
            int enemyCol = col + distance * dir[1];
            
            if (!board.isValidPosition(enemyRow, enemyCol)) break;
            
            int enemyPiece = board.getPiece(enemyRow, enemyCol);
            if (enemyPiece != Constants.EMPTY) {
                if (board.getPieceOwner(enemyPiece) == board.getPieceOwner(piece)) break;
                
                int landRow = enemyRow + dir[0];
                int landCol = enemyCol + dir[1];
                
                if (isValidMovePosition(landRow, landCol)) {
                    Move captureMove = new Move(landRow, landCol);
                    captureMove.addCapturedPiece(new Point(enemyRow, enemyCol));
                    moves.add(captureMove);
                }
                break;
            }
        }
    }
    
    private boolean isValidMovePosition(int row, int col) {
        return board.isValidPosition(row, col) && 
               board.getPiece(row, col) == Constants.EMPTY;
    }
    
    private boolean isValidCapture(int fromRow, int fromCol, int middleRow, int middleCol, 
                                  int toRow, int toCol) {
        if (!board.isValidPosition(toRow, toCol) || 
            !board.isValidPosition(middleRow, middleCol)) {
            return false;
        }
        
        int piece = board.getPiece(fromRow, fromCol);
        int middlePiece = board.getPiece(middleRow, middleCol);
        
        return board.getPiece(toRow, toCol) == Constants.EMPTY &&
               middlePiece != Constants.EMPTY &&
               board.getPieceOwner(middlePiece) != board.getPieceOwner(piece);
    }
    
    private boolean hasCaptureMoves(int row, int col) {
        List<Move> moves = generateMoves(row, col);
        for (Move move : moves) {
            if (move.isCapture()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Executa um movimento no tabuleiro
     */
    public void executeMove(int fromRow, int fromCol, Move move) {
        int piece = board.getPiece(fromRow, fromCol);
        boolean wasCapture = move.isCapture();
        boolean wasPawnMove = !board.isKing(piece);
        
        // Executa o movimento
        board.movePiece(fromRow, fromCol, move.getDestination().x, move.getDestination().y);
        
        // Processa capturas
        processCaptures(move);
        
        // Processa promoção
        processPromotion(move, piece);
        
        // Atualiza contadores e toca sons
        updateGameState(wasCapture, wasPawnMove);
        
        // Verifica sequência de captura ou finaliza turno
        if (wasCapture && hasCaptureMoves(move.getDestination().x, move.getDestination().y)) {
            startCaptureSequence(move.getDestination().x, move.getDestination().y);
        } else {
            endTurn();
        }
        
        updateMandatoryPieces();
        updateStatusLabel();
        checkEndGame();
    }
    
    private void processCaptures(Move move) {
        for (Point captured : move.getCapturedPieces()) {
            board.removePiece(captured.x, captured.y);
        }
    }
    
    private void processPromotion(Move move, int piece) {
        int destRow = move.getDestination().x;
        int player = board.getPieceOwner(piece);
        
        if (board.isPromotionPosition(destRow, player) && !board.isKing(piece)) {
            board.promotePiece(destRow, move.getDestination().y);
            soundManager.playKingSound();
        }
    }
    
    private void updateGameState(boolean wasCapture, boolean wasPawnMove) {
        if (wasCapture) {
            soundManager.playCaptureSound();
            movesWithoutCaptureOrPawn = 0;
        } else {
            soundManager.playMoveSound();
            if (wasPawnMove) {
                movesWithoutCaptureOrPawn = 0;
            } else {
                movesWithoutCaptureOrPawn++;
            }
        }
    }
    
    private void startCaptureSequence(int row, int col) {
        inCaptureSequence = true;
        captureSequencePiece = new Point(row, col);
    }
    
    private void endTurn() {
        inCaptureSequence = false;
        captureSequencePiece = null;
        isPlayer1Turn = !isPlayer1Turn;
    }
    
    /**
     * Verifica se um jogador tem movimentos legais disponíveis
     */
    public boolean hasLegalMoves(int player) {
        boolean hasMandatoryCaptures = hasMandatoryCaptures(player);
        
        for (Point piece : board.getPieces(player)) {
            List<Move> moves = generateMoves(piece.x, piece.y);
            for (Move move : moves) {
                if (!hasMandatoryCaptures || move.isCapture()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean hasMandatoryCaptures(int player) {
        for (Point piece : board.getPieces(player)) {
            if (hasCaptureMoves(piece.x, piece.y)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica condições de fim de jogo
     */
    private void checkEndGame() {
        if (showingEndDialog) return;
        
        boolean player1HasPieces = board.hasPieces(1);
        boolean player2HasPieces = board.hasPieces(2);
        boolean player1CanMove = hasLegalMoves(1);
        boolean player2CanMove = hasLegalMoves(2);
        
        if (!player1HasPieces || !player1CanMove) {
            endGame(2);
        } else if (!player2HasPieces || !player2CanMove) {
            endGame(1);
        } else if (movesWithoutCaptureOrPawn >= 20) {
            endGame(0); // Empate
        }
    }
    
    /**
     * Finaliza o jogo e mostra resultado
     */
    private void endGame(int winner) {
        showingEndDialog = true;
        
        String message = buildEndGameMessage(winner);
        
        switch (winner) {
            case 1:
                player1Wins++;
                soundManager.playWinSound();
                break;
            case 2:
                player2Wins++;
                soundManager.playWinSound();
                break;
            case 0:
                // Empate - sem som de vitória
                break;
        }
        
        JOptionPane.showMessageDialog(null, message);
        initializeGame();
        showingEndDialog = false;
    }
    
    private String buildEndGameMessage(int winner) {
        StringBuilder message = new StringBuilder();
        
        switch (winner) {
            case 1:
                message.append(Constants.MSG_PLAYER1_WIN);
                break;
            case 2:
                message.append(Constants.MSG_PLAYER2_WIN);
                break;
            case 0:
                message.append(Constants.MSG_DRAW);
                break;
        }
        
        message.append("\n\n").append(Constants.MSG_REMAINING_PIECES).append("\n");
        message.append("Jogador 1: ").append(board.countPieces(1)).append("\n");
        message.append("Jogador 2: ").append(board.countPieces(2));
        
        return message.toString();
    }
}