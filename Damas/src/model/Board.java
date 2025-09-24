package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import utils.Constants;

/**
 * Representa o tabuleiro do jogo de damas.
 * Gerencia o estado das peças e validações de posição.
 */
public final class Board {
    private int[][] board;
    
    public Board() {
        board = new int[Constants.BOARD_SIZE][Constants.BOARD_SIZE];
        initialize();
    }
    
    /**
     * Inicializa o tabuleiro com a configuração padrão
     */
    public void initialize() {
        // Limpa o tabuleiro
        for (int r = 0; r < Constants.BOARD_SIZE; r++) {
            for (int c = 0; c < Constants.BOARD_SIZE; c++) {
                board[r][c] = Constants.EMPTY;
            }
        }
        
        // Configura peças do jogador 1 (parte superior)
        setupPlayerPieces(0, 3, Constants.PLAYER1_PAWN);
        
        // Configura peças do jogador 2 (parte inferior)
        setupPlayerPieces(5, Constants.BOARD_SIZE, Constants.PLAYER2_PAWN);
    }
    
    private void setupPlayerPieces(int startRow, int endRow, int pieceType) {
        for (int r = startRow; r < endRow; r++) {
            for (int c = (r % 2 == 0 ? 1 : 0); c < Constants.BOARD_SIZE; c += 2) {
                board[r][c] = pieceType;
            }
        }
    }
    
    /**
     * Verifica se uma posição está dentro dos limites do tabuleiro
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < Constants.BOARD_SIZE && 
               col >= 0 && col < Constants.BOARD_SIZE;
    }
    
    /**
     * Obtém o tipo de peça em uma posição específica
     */
    public int getPiece(int row, int col) {
        return isValidPosition(row, col) ? board[row][col] : Constants.EMPTY;
    }
    
    /**
     * Define uma peça em uma posição específica
     */
    public void setPiece(int row, int col, int piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
        }
    }
    
    /**
     * Move uma peça de uma posição para outra
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        int piece = getPiece(fromRow, fromCol);
        setPiece(toRow, toCol, piece);
        setPiece(fromRow, fromCol, Constants.EMPTY);
    }
    
    /**
     * Remove uma peça do tabuleiro
     */
    public void removePiece(int row, int col) {
        setPiece(row, col, Constants.EMPTY);
    }
    
    /**
     * Verifica se um jogador ainda tem peças no tabuleiro
     */
    public boolean hasPieces(int player) {
        for (int r = 0; r < Constants.BOARD_SIZE; r++) {
            for (int c = 0; c < Constants.BOARD_SIZE; c++) {
                int piece = getPiece(r, c);
                if (piece != Constants.EMPTY && getPieceOwner(piece) == player) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Obtém todas as peças de um jogador
     */
    public List<Point> getPieces(int player) {
        List<Point> pieces = new ArrayList<>();
        for (int r = 0; r < Constants.BOARD_SIZE; r++) {
            for (int c = 0; c < Constants.BOARD_SIZE; c++) {
                int piece = getPiece(r, c);
                if (piece != Constants.EMPTY && getPieceOwner(piece) == player) {
                    pieces.add(new Point(r, c));
                }
            }
        }
        return pieces;
    }
    
    /**
     * Conta quantas peças um jogador possui
     */
    public int countPieces(int player) {
        int count = 0;
        for (int r = 0; r < Constants.BOARD_SIZE; r++) {
            for (int c = 0; c < Constants.BOARD_SIZE; c++) {
                int piece = getPiece(r, c);
                if (piece != Constants.EMPTY && getPieceOwner(piece) == player) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Obtém o proprietário de uma peça (1 ou 2)
     */
    public int getPieceOwner(int piece) {
        return piece % 10;
    }
    
    /**
     * Verifica se uma peça é uma dama
     */
    public boolean isKing(int piece) {
        return piece >= 10;
    }
    
    /**
     * Promove uma peça a dama
     */
    public void promotePiece(int row, int col) {
        int piece = getPiece(row, col);
        if (piece == Constants.PLAYER1_PAWN) {
            setPiece(row, col, Constants.PLAYER1_KING);
        } else if (piece == Constants.PLAYER2_PAWN) {
            setPiece(row, col, Constants.PLAYER2_KING);
        }
    }
    
    /**
     * Verifica se uma posição é válida para promoção a dama
     */
    public boolean isPromotionPosition(int row, int player) {
        if (player == 1) {
            return row == Constants.BOARD_SIZE - 1; // Última linha
        } else {
            return row == 0; // Primeira linha
        }
    }
}