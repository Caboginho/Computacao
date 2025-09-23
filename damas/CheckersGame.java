import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class CheckersGame extends JFrame {
    private final int[][] board = new int[8][8]; // 0: empty, 1: P1, 2: P2, 11: P1 king, 22: P2 king
    private boolean turn = true; // true: Player 1, false: Player 2
    private int p1Wins = 0, p2Wins = 0, ties = 0;
    private final JLabel scoreLabel;
    private Point selected = null;

    public CheckersGame() {
        setTitle("Jogo de Damas");
        setSize(800, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BoardPanel boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        boardPanel.addMouseListener(new MouseHandler());

        // Painel para entrada de coordenadas
        JPanel southPanel = new JPanel();
        JTextField inputField = new JTextField(20);
        inputField.addActionListener(new InputHandler(inputField));
        southPanel.add(new JLabel("Digite a jogada (linha1 coluna1 linha2 coluna2): "));
        southPanel.add(inputField);
        add(southPanel, BorderLayout.SOUTH);

        scoreLabel = new JLabel("Placar: Jogador 1: 0 | Jogador 2: 0 | Empates: 0");
        add(scoreLabel, BorderLayout.NORTH);

        initBoard();
        setVisible(true);
    }

    private void initBoard() {
        // Inicializa o tabuleiro com peças do Jogador 1 (linhas 0-2) e Jogador 2 (linhas 5-7)
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = 0;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = (i % 2 == 0 ? 1 : 0); j < 8; j += 2) {
                board[i][j] = 1;
            }
        }
        for (int i = 5; i < 8; i++) {
            for (int j = (i % 2 == 0 ? 1 : 0); j < 8; j += 2) {
                board[i][j] = 2;
            }
        }
    }

    private class BoardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Desenha o tabuleiro
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    g.setColor((i + j) % 2 == 0 ? Color.WHITE : Color.BLACK);
                    g.fillRect(j * 100, i * 100, 100, 100);
                    if (board[i][j] > 0) {
                        g.setColor((board[i][j] % 10 == 1) ? Color.RED : Color.BLUE);
                        g.fillOval(j * 100 + 10, i * 100 + 10, 80, 80);
                        if (board[i][j] > 10) {
                            g.setColor(Color.WHITE);
                            g.drawString("D", j * 100 + 40, i * 100 + 60); // "D" para Dama
                        }
                    }
                }
            }
            // Destaca a peça selecionada
            if (selected != null) {
                g.setColor(Color.YELLOW);
                g.drawRect(selected.y * 100, selected.x * 100, 100, 100);
            }
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = e.getY() / 100;
            int col = e.getX() / 100;
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (selected == null) {
                    // Seleciona uma peça se for do jogador atual
                    if (board[row][col] > 0 && ((turn && board[row][col] % 10 == 1) || (!turn && board[row][col] % 10 == 2))) {
                        selected = new Point(row, col);
                    }
                } else {
                    // Tenta mover
                    if (move(selected.x, selected.y, row, col)) {
                        turn = !turn;
                        checkEndGame();
                        selected = null;
                    }
                }
                repaint();
            }
        }
    }

    private class InputHandler implements ActionListener {
        private final JTextField field;

        public InputHandler(JTextField field) {
            this.field = field;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String input = field.getText().trim();
            String[] parts = input.split("\\s+");
            if (parts.length == 4) {
                try {
                    // Coordenadas base 1, converte para base 0
                    int r1 = Integer.parseInt(parts[0]) - 1;
                    int c1 = Integer.parseInt(parts[1]) - 1;
                    int r2 = Integer.parseInt(parts[2]) - 1;
                    int c2 = Integer.parseInt(parts[3]) - 1;
                    if (move(r1, c1, r2, c2)) {
                        turn = !turn;
                        checkEndGame();
                    }
                } catch (NumberFormatException ex) {
                    // Ignora entrada inválida
                }
            }
            field.setText("");
            repaint();
        }
    }

    private boolean move(int r1, int c1, int r2, int c2) {
        if (r1 < 0 || r1 >= 8 || c1 < 0 || c1 >= 8 || r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8) {
            return false;
        }
        int piece = board[r1][c1];
        if (piece == 0 || (turn && piece % 10 != 1) || (!turn && piece % 10 != 2)) {
            return false;
        }
        boolean isKing = piece > 10;
        int dir = (piece % 10 == 1) ? 1 : -1;

        // Movimento simples
        if (Math.abs(r2 - r1) == 1 && Math.abs(c2 - c1) == 1 && board[r2][c2] == 0) {
            if (isKing || (r2 - r1 == dir)) {
                board[r2][c2] = board[r1][c1];
                board[r1][c1] = 0;
                promoteToKing(r2, c2);
                return true;
            }
        }

        // Captura
        if (Math.abs(r2 - r1) == 2 && Math.abs(c2 - c1) == 2 && board[r2][c2] == 0) {
            int mr = (r1 + r2) / 2;
            int mc = (c1 + c2) / 2;
            if (board[mr][mc] > 0 && board[mr][mc] % 10 != piece % 10 && (isKing || (r2 - r1 == 2 * dir))) {
                board[r2][c2] = board[r1][c1];
                board[r1][c1] = 0;
                board[mr][mc] = 0;
                promoteToKing(r2, c2);
                // Nota: Não implementa capturas múltiplas em uma jogada para simplicidade
                return true;
            }
        }
        return false;
    }

    private void promoteToKing(int row, int col) {
        if (board[row][col] == 1 && row == 7) {
            board[row][col] = 11;
        } else if (board[row][col] == 2 && row == 0) {
            board[row][col] = 22;
        }
    }

    private void checkEndGame() {
        if (!canMove(true) && !canMove(false)) {
            ties++;
            JOptionPane.showMessageDialog(this, "Empate!");
            resetBoard();
        } else if (!canMove(true)) {
            p2Wins++;
            JOptionPane.showMessageDialog(this, "Vitória do Jogador 2!");
            resetBoard();
        } else if (!canMove(false)) {
            p1Wins++;
            JOptionPane.showMessageDialog(this, "Vitória do Jogador 1!");
            resetBoard();
        }
        updateScore();
    }

    private boolean canMove(boolean forPlayer1) {
        int player = forPlayer1 ? 1 : 2;
        int dir = forPlayer1 ? 1 : -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] % 10 == player) {
                    boolean isKing = board[r][c] > 10;
                    int[] dirs = isKing ? new int[]{dir, -dir} : new int[]{dir};
                    int[] dcs = {-1, 1};
                    for (int dr : dirs) {
                        for (int dc : dcs) {
                            // Movimento simples
                            int nr = r + dr;
                            int nc = c + dc;
                            if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8 && board[nr][nc] == 0) {
                                return true;
                            }
                            // Captura
                            nr = r + 2 * dr;
                            nc = c + 2 * dc;
                            int mr = r + dr;
                            int mc = c + dc;
                            if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8 && board[nr][nc] == 0 &&
                                board[mr][mc] > 0 && board[mr][mc] % 10 != player) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void resetBoard() {
        initBoard();
        turn = true;
    }

    private void updateScore() {
        scoreLabel.setText("Placar: Jogador 1: " + p1Wins + " | Jogador 2: " + p2Wins + " | Empates: " + ties);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CheckersGame::new);
    }
}