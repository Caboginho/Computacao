package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um movimento no jogo de damas.
 * Armazena destino e peças capturadas durante o movimento.
 */
public class Move {
    private final Point destination;
    private final List<Point> capturedPieces;
    
    public Move(int row, int col) {
        this.destination = new Point(row, col);
        this.capturedPieces = new ArrayList<>();
    }
    
    /**
     * Adiciona uma peça capturada neste movimento
     */
    public void addCapturedPiece(Point piece) {
        capturedPieces.add(piece);
    }
    
    /**
     * Verifica se este movimento é uma captura
     */
    public boolean isCapture() {
        return !capturedPieces.isEmpty();
    }
    
    // Getters
    public Point getDestination() { return destination; }
    public List<Point> getCapturedPieces() { return capturedPieces; }
    
    @Override
    public String toString() {
        return String.format("Move[to(%d,%d), captures=%d]", 
            destination.x, destination.y, capturedPieces.size());
    }
}