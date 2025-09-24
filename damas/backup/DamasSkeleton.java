import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * DamasSkeleton.java
 * Versão modular inicial em português:
 * - Board (regras e geração de movimentos)
 * - GameController (estado do jogo, execução de movimentos, verificação de fim)
 * - GamePanel (UI: seleção/desseleção e highlights)
 *
 * Próximo passo: adicionar animação de deslocamento usando os hooks marcados com TODO.
 *
 * Compile:
 *   javac DamasSkeleton.java
 * Run:
 *   java DamasSkeleton
 */
public class DamasSkeleton extends JFrame {

    public DamasSkeleton() {
        super("Damas - Skeleton Modular");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // cria o controlador do jogo
        GameController controller = new GameController();
        GamePanel panel = new GamePanel(controller);

        add(panel, BorderLayout.CENTER);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(controller.getScoreLabel(), BorderLayout.WEST);
        sul.add(controller.getStatusLabel(), BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DamasSkeleton::new);
    }

    // -----------------------------
    // Core model: Board + Move
    // -----------------------------
    static class Move {
        final Point dest;
        final List<Point> captured; // lista de peças capturadas (suporta múltiplas em cadeia)

        Move(int r, int c) {
            dest = new Point(r, c);
            captured = new ArrayList<>();
        }

        void addCaptured(Point p) {
            captured.add(p);
        }

        boolean isCapture() {
            return !captured.isEmpty();
        }

        @Override
        public String toString() {
            return "Move -> " + dest + " caps=" + captured;
        }
    }

    static final class Board {
        final int SIZE = 8;
        // encoding:
        // 0 = vazio
        // 1 = peão jogador1 (move para baixo)
        // 2 = peão jogador2 (move para cima)
        // 11 = dama jogador1
        // 22 = dama jogador2
        final int[][] board = new int[SIZE][SIZE];

        Board() { init(); }

        void init() {
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++)
                    board[r][c] = 0;

            // configuração inicial
            for (int r = 0; r < 3; r++) {
                for (int c = (r % 2 == 0 ? 1 : 0); c < SIZE; c += 2)
                    board[r][c] = 1;
            }
            for (int r = 5; r < 8; r++) {
                for (int c = (r % 2 == 0 ? 1 : 0); c < SIZE; c += 2)
                    board[r][c] = 2;
            }
        }

        boolean inside(int r, int c) {
            return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
        }

        int get(int r, int c) {
            return board[r][c];
        }

        void set(int r, int c, int val) {
            board[r][c] = val;
        }

        // gera movimentos crus (sem considerar captura obrigatória)
        List<Move> rawMovesForPiece(int r, int c) {
            List<Move> out = new ArrayList<>();
            int piece = get(r, c);
            if (piece == 0) return out;
            int player = piece % 10;
            boolean isKing = piece >= 10;

            if (!isKing) {
                int forward = (player == 1) ? 1 : -1;
                int[] dcs = {-1, 1};
                for (int dc : dcs) {
                    int nr = r + forward;
                    int nc = c + dc;
                    if (inside(nr, nc) && get(nr, nc) == 0) {
                        out.add(new Move(nr, nc));
                    }
                    // captura
                    int jr = r + 2 * forward;
                    int jc = c + 2 * dc;
                    int mr = r + forward;
                    int mc = c + dc;
                    if (inside(jr, jc) && get(jr, jc) == 0 && get(mr, mc) != 0 && get(mr, mc) % 10 != player) {
                        Move m = new Move(jr, jc);
                        m.addCaptured(new Point(mr, mc));
                        out.add(m);
                    }
                }
            } else {
                // dama
                int[][] dirs = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                for (int[] d : dirs) {
                    int dr = d[0], dc = d[1];
                    int nr = r + dr, nc = c + dc;
                    while (inside(nr, nc) && get(nr, nc) == 0) {
                        out.add(new Move(nr, nc));
                        nr += dr; nc += dc;
                    }
                    // captura à distância
                    int searchR = r + dr, searchC = c + dc;
                    while (inside(searchR, searchC)) {
                        if (get(searchR, searchC) == 0) {
                            searchR += dr; searchC += dc;
                            continue;
                        }
                        if (get(searchR, searchC) % 10 != player) {
                            int landR = searchR + dr, landC = searchC + dc;
                            while (inside(landR, landC) && get(landR, landC) == 0) {
                                Move m = new Move(landR, landC);
                                m.addCaptured(new Point(searchR, searchC));
                                out.add(m);
                                landR += dr; landC += dc;
                            }
                        }
                        break;
                    }
                }
            }
            return out;
        }

        boolean hasAnyPiece(int player) {
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++)
                    if (get(r, c) % 10 == player) return true;
            return false;
        }
    }

    // -----------------------------
    // GameController: regras e estado
    // -----------------------------
    static final class GameController {
        Board board;
        boolean turnPlayer1 = true;
        boolean inCaptureSequence = false;
        Point captureSequencePiece = null;

        int p1wins = 0, p2wins = 0;
        boolean showingEndDialog = false;

        JLabel statusLabel;

        // construtor default
        GameController() {
            board = new Board();
            statusLabel = new JLabel();
            updateStatusLabel();
        }

        // getters de estado
        boolean isTurnPlayer1() { return turnPlayer1; }

        List<Point> getMandatoryPieces() {
            List<Point> out = new ArrayList<>();
            for (int r = 0; r < board.SIZE; r++) {
                for (int c = 0; c < board.SIZE; c++) {
                    int p = board.get(r, c);
                    if (isTurnOf(p)) {
                        for (Move m : board.rawMovesForPiece(r, c)) {
                            if (m.isCapture()) {
                                out.add(new Point(r, c));
                                break;
                            }
                        }
                    }
                }
            }
            return out;
        }

        List<Move> validMovesFor(int r, int c) { return legalMovesForPiece(r, c); }

        boolean isInCaptureSequence() { return inCaptureSequence; }

        Point getCaptureSequencePiece() { return captureSequencePiece; }

        JLabel getStatusLabel() { return statusLabel; }
        JLabel getScoreLabel() { return new JLabel("Placar: "); }

        void updateStatusLabel() {
            String t = (turnPlayer1 ? "Vermelho" : "Azul");
            statusLabel.setText("Vez: " + t + " | Placar Vermelho " + p1wins + " - " + p2wins + " Azul");
        }

        boolean isTurnOf(int piece) {
            return piece != 0 && (piece % 10 == 1) == turnPlayer1;
        }

        int countPieces(int player) {
            int c = 0;
            for (int r = 0; r < board.SIZE; r++)
                for (int col = 0; col < board.SIZE; col++)
                    if (board.get(r, col) % 10 == player) c++;
            return c;
        }

        int countLegalMoves(int player) {
            int c = 0;
            for (int r = 0; r < board.SIZE; r++)
                for (int col = 0; col < board.SIZE; col++) {
                    int piece = board.get(r, col);
                    if (piece % 10 == player) {
                        c += legalMovesForPiece(r, col).size();
                    }
                }
            return c;
        }

        boolean hasAnyPieceForPlayer(int player) { return board.hasAnyPiece(player); }

        boolean hasAnyLegalMoveForPlayer(int player) {
            for (int r = 0; r < board.SIZE; r++)
                for (int c = 0; c < board.SIZE; c++) {
                    int piece = board.get(r, c);
                    if (piece % 10 == player) {
                        if (!legalMovesForPiece(r, c).isEmpty()) return true;
                    }
                }
            return false;
        }

        List<Move> legalMovesForPiece(int r, int c) {
            List<Move> raw = board.rawMovesForPiece(r, c);
            if (raw.isEmpty()) return raw;

            // verificar captura obrigatória
            boolean mustCapture = false;
            for (int rr = 0; rr < board.SIZE; rr++)
                for (int cc = 0; cc < board.SIZE; cc++) {
                    int p = board.get(rr, cc);
                    if (isTurnOf(p)) {
                        for (Move m : board.rawMovesForPiece(rr, cc))
                            if (m.isCapture()) mustCapture = true;
                    }
                }
            if (mustCapture) {
                List<Move> filtered = new ArrayList<>();
                for (Move m : raw) if (m.isCapture()) filtered.add(m);
                return filtered;
            }
            return raw;
        }

        void executeMove(Point from, Move m) {
            int piece = board.get(from.x, from.y);
            board.set(from.x, from.y, 0);
            board.set(m.dest.x, m.dest.y, piece);

            for (Point cap : m.captured) board.set(cap.x, cap.y, 0);

            // promoção a dama
            if (piece == 1 && m.dest.x == board.SIZE - 1) board.set(m.dest.x, m.dest.y, 11);
            if (piece == 2 && m.dest.x == 0) board.set(m.dest.x, m.dest.y, 22);

            // sequência de captura
            if (m.isCapture()) {
                List<Move> further = board.rawMovesForPiece(m.dest.x, m.dest.y);
                List<Move> captures = new ArrayList<>();
                for (Move mm : further) if (mm.isCapture()) captures.add(mm);
                if (!captures.isEmpty()) {
                    inCaptureSequence = true;
                    captureSequencePiece = m.dest;
                    return;
                }
            }

            inCaptureSequence = false;
            captureSequencePiece = null;
            turnPlayer1 = !turnPlayer1;
            updateStatusLabel();

            checkEndGame();
        }

        void checkEndGame() {
            if (showingEndDialog) return;

            boolean p1Has = hasAnyPieceForPlayer(1);
            boolean p2Has = hasAnyPieceForPlayer(2);
            boolean p1Can = hasAnyLegalMoveForPlayer(1);
            boolean p2Can = hasAnyLegalMoveForPlayer(2);

            System.out.println("=== checkEndGame() ===");
            System.out.println("P1 pieces: " + countPieces(1) + ", legalMoves: " + countLegalMoves(1));
            System.out.println("P2 pieces: " + countPieces(2) + ", legalMoves: " + countLegalMoves(2));

            if (!p1Has || !p1Can) {
                showingEndDialog = true;
                p2wins++;
                JOptionPane.showMessageDialog(null, "Vitória: Jogador 2 (Azul)");
                resetGame();
                showingEndDialog = false;
            } else if (!p2Has || !p2Can) {
                showingEndDialog = true;
                p1wins++;
                JOptionPane.showMessageDialog(null, "Vitória: Jogador 1 (Vermelho)");
                resetGame();
                showingEndDialog = false;
            }
        }

        void resetGame() {
            board.init();
            turnPlayer1 = true;
            inCaptureSequence = false;
            captureSequencePiece = null;
            updateStatusLabel();
        }
    }

    // -----------------------------
    // GamePanel: UI (seleção & deselection)
    // -----------------------------
    static class GamePanel extends JPanel {
        final GameController controller;
        final int SQUARE = 90;
        Point selected = null;
        java.util.List<Move> currentValidMoves = new ArrayList<>();

        GamePanel(GameController controller) {
            this.controller = controller;
            setPreferredSize(new Dimension(controller.board.SIZE * SQUARE, controller.board.SIZE * SQUARE));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int r = e.getY() / SQUARE;
                    int c = e.getX() / SQUARE;
                    if (!controller.board.inside(r, c)) return;
                    handleClick(r, c);
                    repaint();
                }
            });
        }

        void handleClick(int r, int c) {
            int piece = controller.board.get(r, c);

            if (selected != null && selected.x == r && selected.y == c) {
                selected = null;
                currentValidMoves.clear();
                return;
            }

            if (selected == null) {
                if (piece == 0) return;
                int player = piece % 10;
                boolean curPlayerIs1 = controller.isTurnPlayer1();
                if ((curPlayerIs1 && player != 1) || (!curPlayerIs1 && player != 2)) return;

                List<Point> mand = controller.getMandatoryPieces();
                if (!mand.isEmpty()) {
                    boolean ok = false;
                    for (Point p : mand) if (p.x == r && p.y == c) { ok = true; break; }
                    if (!ok) {
                        JOptionPane.showMessageDialog(this, "Existe captura obrigatória. Selecione uma das peças marcadas.");
                        return;
                    }
                }

                selected = new Point(r, c);
                currentValidMoves = controller.validMovesFor(r, c);
                return;
            }

            Move chosen = null;
            for (Move m : currentValidMoves) {
                if (m.dest.x == r && m.dest.y == c) { chosen = m; break; }
            }
            if (chosen != null) {
                controller.executeMove(selected, chosen);

                if (controller.isInCaptureSequence()) {
                    Point cs = controller.getCaptureSequencePiece();
                    selected = new Point(cs.x, cs.y);
                    currentValidMoves = controller.validMovesFor(cs.x, cs.y);
                } else {
                    selected = null;
                    currentValidMoves.clear();
                }
                return;
            }

            if (piece != 0) {
                int player = piece % 10;
                boolean curPlayerIs1 = controller.isTurnPlayer1();
                if ((curPlayerIs1 && player == 1) || (!curPlayerIs1 && player == 2)) {
                    selected = new Point(r, c);
                    currentValidMoves = controller.validMovesFor(r, c);
                }
            } else {
                selected = null;
                currentValidMoves.clear();
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // tabuleiro
            for (int r = 0; r < controller.board.SIZE; r++) {
                for (int c = 0; c < controller.board.SIZE; c++) {
                    g.setColor((r + c) % 2 == 0 ? new Color(245, 222, 179) : new Color(139, 69, 19));
                    g.fillRect(c * SQUARE, r * SQUARE, SQUARE, SQUARE);
                }
            }

            // highlight peças obrigatórias
            for (Point p : controller.getMandatoryPieces()) {
                g.setColor(new Color(255, 215, 0, 180));
                g.setStroke(new BasicStroke(4));
                g.drawRect(p.y * SQUARE + 4, p.x * SQUARE + 4, SQUARE - 8, SQUARE - 8);
            }

            // highlight seleção
            if (selected != null) {
                g.setColor(new Color(255, 215, 0, 180));
                g.setStroke(new BasicStroke(3));
                g.drawRect(selected.y * SQUARE + 3, selected.x * SQUARE + 3, SQUARE - 6, SQUARE - 6);

                for (Move m : currentValidMoves) {
                    int rr = m.dest.x, cc = m.dest.y;
                    if (m.isCapture()) g.setColor(new Color(255, 69, 0, 150));
                    else g.setColor(new Color(50, 205, 50, 150));
                    int cx = cc * SQUARE + SQUARE / 2, cy = rr * SQUARE + SQUARE / 2;
                    g.fillOval(cx - 18, cy - 18, 36, 36);
                }
            }

            // peças
            for (int r = 0; r < controller.board.SIZE; r++) {
                for (int c = 0; c < controller.board.SIZE; c++) {
                    int piece = controller.board.get(r, c);
                    if (piece == 0) continue;
                    boolean isKing = piece >= 10;
                    Color col = (piece % 10 == 1) ? new Color(220, 20, 60) : new Color(30, 144, 255);
                    g.setColor(new Color(0, 0, 0, 80));
                    g.fillOval(c * SQUARE + 8, r * SQUARE + 8, SQUARE - 16, SQUARE - 32);
                    GradientPaint gp = new GradientPaint(c * SQUARE + 12, r * SQUARE + 12, col.brighter(),
                            c * SQUARE + SQUARE - 20, r * SQUARE + SQUARE - 28, col.darker());
                    g.setPaint(gp);
                    g.fillOval(c * SQUARE + 12, r * SQUARE + 12, SQUARE - 24, SQUARE - 24);
                    g.setPaint(null);
                    g.setColor(col.darker().darker());
                    g.setStroke(new BasicStroke(3));
                    g.drawOval(c * SQUARE + 12, r * SQUARE + 12, SQUARE - 24, SQUARE - 24);
                    if (isKing) {
                        g.setColor(Color.YELLOW);
                        g.setFont(new Font("SansSerif", Font.BOLD, 28));
                        g.drawString("♔", c * SQUARE + SQUARE / 2 - 10, r * SQUARE + SQUARE / 2 + 10);
                    }
                }
            }
            controller.updateStatusLabel();
        }
    }
}
