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
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * DamasSkeleton.java
 * Versão modular inicial:
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
        GameController controller = new GameController();
        GamePanel panel = new GamePanel(controller);
        add(panel, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(controller.getScoreLabel(), BorderLayout.WEST);
        south.add(controller.getStatusLabel(), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

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

        void addCaptured(Point p){ 
            captured.add(p); 
        }

        boolean isCapture(){ 
            return !captured.isEmpty(); 
        }

        @Override 
        public String toString(){ 
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

    Board(){ init(); }

    void init(){
        for(int r=0;r<SIZE;r++) for(int c=0;c<SIZE;c++) board[r][c]=0;
        // configuração inicial
        for(int r=0;r<3;r++){
            for(int c=(r%2==0?1:0); c<SIZE; c+=2) board[r][c] = 1;
        }
        for(int r=5;r<8;r++){
            for(int c=(r%2==0?1:0); c<SIZE; c+=2) board[r][c] = 2;
        }
    }

    boolean inside(int r,int c){ return r>=0 && r<SIZE && c>=0 && c<SIZE; }
    int get(int r,int c){ return board[r][c]; }
    void set(int r,int c,int val){ board[r][c] = val; }

    // gera movimentos crus (sem considerar captura obrigatória)
    List<Move> rawMovesForPiece(int r,int c){
        List<Move> out = new ArrayList<>();
        int piece = get(r,c);
        if(piece==0) return out;
        int player = piece%10;
        boolean isKing = piece>=10;

        if(!isKing){
            int forward = (player==1) ? 1 : -1;
            int[] dcs = {-1,1};
            for(int dc : dcs){
                int nr = r + forward;
                int nc = c + dc;
                if(inside(nr,nc) && get(nr,nc)==0){
                    out.add(new Move(nr,nc));
                }
                // captura
                int jr = r + 2*forward;
                int jc = c + 2*dc;
                int mr = r + forward;
                int mc = c + dc;
                if(inside(jr,jc) && get(jr,jc)==0 && get(mr,mc)!=0 && get(mr,mc)%10 != player){
                    Move m = new Move(jr,jc);
                    m.addCaptured(new Point(mr,mc));
                    out.add(m);
                }
            }
        } else {
            // dama
            int[][] dirs = {{1,1},{1,-1},{-1,1},{-1,-1}};
            for(int[] d : dirs){
                int dr=d[0], dc=d[1];
                int nr=r+dr, nc=c+dc;
                while(inside(nr,nc) && get(nr,nc)==0){
                    out.add(new Move(nr,nc));
                    nr += dr; nc += dc;
                }
                // captura à distância
                int searchR=r+dr, searchC=c+dc;
                while(inside(searchR,searchC)){
                    if(get(searchR,searchC)==0){ 
                        searchR += dr; searchC += dc; 
                        continue; 
                    }
                    if(get(searchR,searchC)%10 != player){
                        int landR = searchR + dr, landC = searchC + dc;
                        while(inside(landR,landC) && get(landR,landC)==0){
                            Move m = new Move(landR,landC);
                            m.addCaptured(new Point(searchR,searchC));
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

    boolean hasAnyPiece(int player){
        for(int r=0;r<SIZE;r++) 
            for(int c=0;c<SIZE;c++) 
                if(get(r,c)%10==player) return true;
        return false;
    }
}


    // -----------------------------
// GameController: rules & state
// -----------------------------
static final class GameController {
    final Board board = new Board();
    boolean turnPlayer1 = true; // true -> player1
    boolean inCaptureSequence = false;
    Point captureSequencePiece = null;
    final List<Point> mandatoryPieces = new ArrayList<>();

    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel scoreLabel = new JLabel("Score: P1 0 - P2 0");
    private int p1wins=0, p2wins=0, ties=0;

    // flag para evitar múltiplos diálogos simultâneos/reentrância
    private boolean showingEndDialog = false;

    GameController(){
        updateMandatoryPieces();
        updateStatusLabel();
        // debug inicial: imprime estado quando controller criado
        debugPrintBoardSummary("Startup");
    }

    JLabel getStatusLabel(){ return statusLabel; }
    JLabel getScoreLabel(){ return scoreLabel; }

    void updateStatusLabel(){
        String turn = turnPlayer1 ? "Jogador1 (Vermelho)" : "Jogador2 (Azul)";
        String seq = inCaptureSequence ? " [Sequência de captura ativa]" : "";
        statusLabel.setText("Turno: " + turn + seq);
        scoreLabel.setText(String.format("Score: P1 %d - P2 %d", p1wins, p2wins));
    }

    void updateMandatoryPieces(){
        mandatoryPieces.clear();
        int player = turnPlayer1 ? 1 : 2;
        for(int r=0;r<board.SIZE;r++){
            for(int c=0;c<board.SIZE;c++){
                int piece = board.get(r,c);
                if(piece==0) continue;
                if(piece%10 != player) continue;
                // if any raw move for this piece is capture -> mandatory
                List<Move> raw = board.rawMovesForPiece(r,c);
                for(Move m: raw) if(m.isCapture()){ mandatoryPieces.add(new Point(r,c)); break; }
            }
        }
    }

    // Given a square (r,c) return valid moves respecting global mandatory captures and capture sequence constraint.
    List<Move> validMovesFor(int r,int c){
        List<Move> raw = board.rawMovesForPiece(r,c);
        List<Move> filtered = new ArrayList<>();
        if(inCaptureSequence){
            // only the sequence piece may move, and only capture moves allowed
            if(captureSequencePiece == null || captureSequencePiece.x!=r || captureSequencePiece.y!=c) return filtered;
            for(Move m: raw) if(m.isCapture()) filtered.add(m);
            return filtered;
        }
        if(!mandatoryPieces.isEmpty()){
            for(Move m: raw) if(m.isCapture()) filtered.add(m);
            return filtered;
        } else {
            // no mandatory captures: all raw moves allowed
            filtered.addAll(raw);
            return filtered;
        }
    }

    // execute a chosen move from (rFrom,cFrom) to move.dest
    void executeMove(int rFrom,int cFrom, Move move){
        int piece = board.get(rFrom,cFrom);
        // apply move
        board.set(move.dest.x, move.dest.y, piece);
        board.set(rFrom,cFrom,0);
        // remove captured
        for(Point cap: move.captured) board.set(cap.x, cap.y, 0);
        // promotion
        if(piece==1 && move.dest.x==board.SIZE-1) board.set(move.dest.x, move.dest.y, 11);
        if(piece==2 && move.dest.x==0) board.set(move.dest.x, move.dest.y, 22);

        // after move, check if chain capture is possible from new location (only if this move was a capture)
        if(move.isCapture()){
            // recompute raw moves for the moved piece; but we must evaluate with new board state
            List<Move> further = board.rawMovesForPiece(move.dest.x, move.dest.y);
            boolean hasFurtherCapture=false;
            for(Move m: further) if(m.isCapture()){ hasFurtherCapture=true; break; }
            if(hasFurtherCapture){
                inCaptureSequence = true;
                captureSequencePiece = new Point(move.dest.x, move.dest.y);
                updateMandatoryPieces(); // reflect that the only allowed piece is captureSequencePiece
                updateStatusLabel();
                // debug
                debugPrintBoardSummary("After capture (continuation)");
                return; // same player's turn, forced to continue
            }
        }

        // otherwise the move ends the turn
        inCaptureSequence = false;
        captureSequencePiece = null;
        turnPlayer1 = !turnPlayer1;
        updateMandatoryPieces();
        updateStatusLabel();
        // debug
        debugPrintBoardSummary("After move (turn end)");
        // check endgame
        checkEndGame();
    }

    // Helper: check if ANY legal move exists for the given player (considers mandatory-capture rule for that player)
    boolean hasAnyLegalMoveForPlayer(int player){
        // Simulate: set temporary turn to that player and compute mandatory pieces for that player
        List<Point> forced = new ArrayList<>();
        for(int r=0;r<board.SIZE;r++){
            for(int c=0;c<board.SIZE;c++){
                int p = board.get(r,c);
                if(p==0) continue;
                if(p%10 != player) continue;
                for(Move m: board.rawMovesForPiece(r,c)) if(m.isCapture()){ forced.add(new Point(r,c)); break; }
            }
        }
        // if forced not empty => only consider captures
        for(int r=0;r<board.SIZE;r++){
            for(int c=0;c<board.SIZE;c++){
                int p = board.get(r,c);
                if(p==0 || p%10 != player) continue;
                List<Move> raw = board.rawMovesForPiece(r,c);
                for(Move m: raw){
                    if(forced.isEmpty() || m.isCapture()) return true;
                }
            }
        }
        return false;
    }

    boolean hasAnyPieceForPlayer(int player){
        return board.hasAnyPiece(player);
    }

    void checkEndGame(){
        // proteja contra múltiplas invocações que mostrem o mesmo diálogo
        if(showingEndDialog) return;

        boolean p1Has = hasAnyPieceForPlayer(1);
        boolean p2Has = hasAnyPieceForPlayer(2);
        boolean p1Can = hasAnyLegalMoveForPlayer(1);
        boolean p2Can = hasAnyLegalMoveForPlayer(2);

        // debug: imprimir resumo detalhado antes de decidir
        System.out.println("=== checkEndGame() ===");
        System.out.println("P1 pieces: " + countPieces(1) + ", legalMoves: " + countLegalMoves(1));
        System.out.println("P2 pieces: " + countPieces(2) + ", legalMoves: " + countLegalMoves(2));
        System.out.println("p1Has=" + p1Has + " p1Can=" + p1Can + " | p2Has=" + p2Has + " p2Can=" + p2Can);

        // Only when a player has no pieces OR no legal moves -> he loses
        if(!p1Has || !p1Can){
            showingEndDialog = true;
            p2wins++;
            JOptionPane.showMessageDialog(null, "Vitória: Jogador 2 (Azul)");
            board.init();
            // reset state: player1 starts
            turnPlayer1 = true; inCaptureSequence=false; captureSequencePiece=null;
            updateMandatoryPieces(); updateStatusLabel();
            debugPrintBoardSummary("After reset (P2 win)");
            showingEndDialog = false;
        } else if(!p2Has || !p2Can){
            showingEndDialog = true;
            p1wins++;
            JOptionPane.showMessageDialog(null, "Vitória: Jogador 1 (Vermelho)");
            board.init();
            turnPlayer1 = true; inCaptureSequence=false; captureSequencePiece=null;
            updateMandatoryPieces(); updateStatusLabel();
            debugPrintBoardSummary("After reset (P1 win)");
            showingEndDialog = false;
        }
        // else continue
    }

    // --------------------
    // Debug / Instrumentation helpers
    // --------------------
    void debugPrintBoardSummary(String tag){
        System.out.println(">>> DEBUG: " + tag);
        System.out.println("Board snapshot (rows x cols): " + board.SIZE + "x" + board.SIZE);
        System.out.println("P1 pieces: " + countPieces(1) + ", legal moves: " + countLegalMoves(1));
        System.out.println("P2 pieces: " + countPieces(2) + ", legal moves: " + countLegalMoves(2));
        System.out.println("Mandatory pieces (current turn " + (turnPlayer1? "P1":"P2") + "): " + getMandatoryPiecesSummary());
    }

    int countPieces(int player){
        int s=0;
        for(int r=0;r<board.SIZE;r++) for(int c=0;c<board.SIZE;c++){
            int p = board.get(r,c);
            if(p!=0 && p%10==player) s++;
        }
        return s;
    }

    int countLegalMoves(int player){
        int s=0;
        // compute forced for that player
        List<Point> forced = new ArrayList<>();
        for(int r=0;r<board.SIZE;r++){
            for(int c=0;c<board.SIZE;c++){
                int p = board.get(r,c);
                if(p==0 || p%10 != player) continue;
                for(Move m: board.rawMovesForPiece(r,c)) if(m.isCapture()){ forced.add(new Point(r,c)); break; }
            }
        }
        for(int r=0;r<board.SIZE;r++){
            for(int c=0;c<board.SIZE;c++){
                int p = board.get(r,c);
                if(p==0 || p%10 != player) continue;
                for(Move m: board.rawMovesForPiece(r,c)){
                    if(forced.isEmpty() || m.isCapture()) s++;
                }
            }
        }
        return s;
    }

    String getMandatoryPiecesSummary(){
        if(mandatoryPieces.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for(Point p: mandatoryPieces) sb.append("[").append(p.x).append(",").append(p.y).append("] ");
        return sb.toString();
    }

    // Expose a small API used by GUI
    List<Point> getMandatoryPieces(){ return Collections.unmodifiableList(mandatoryPieces); }
    boolean isInCaptureSequence(){ return inCaptureSequence; }
    Point getCaptureSequencePiece(){ return captureSequencePiece; }
    boolean isTurnPlayer1(){ return turnPlayer1; }
}

    // -----------------------------
    // GamePanel: UI (selection & deselection)
    // -----------------------------
    static class GamePanel extends JPanel {
        final GameController controller;
        final int SQUARE = 90;
        Point selected = null; // selection for UI convenience (keeps sync with controller only by choice)
        java.util.List<Move> currentValidMoves = new ArrayList<>();

        GamePanel(GameController controller){
            this.controller = controller;
            setPreferredSize(new Dimension(controller.board.SIZE * SQUARE, controller.board.SIZE * SQUARE));
            addMouseListener(new MouseAdapter(){
                @Override public void mouseClicked(MouseEvent e){
                    int r = e.getY() / SQUARE;
                    int c = e.getX() / SQUARE;
                    if(!controller.board.inside(r,c)) return;
                    handleClick(r,c);
                    repaint();
                }
            });
        }

        void handleClick(int r,int c){
            int piece = controller.board.get(r,c);
            // Deselect if clicking same selected piece
            if(selected!=null && selected.x==r && selected.y==c){
                selected = null;
                currentValidMoves.clear();
                return;
            }

            // If no selected piece: select piece if belongs to current player and allowed
            if(selected==null){
                if(piece==0) return;
                int player = piece%10;
                boolean curPlayerIs1 = controller.isTurnPlayer1();
                if((curPlayerIs1 && player!=1) || (!curPlayerIs1 && player!=2)) return;
                // if mandatory pieces exist, only allow selecting them
                List<Point> mand = controller.getMandatoryPieces();
                if(!mand.isEmpty()){
                    boolean ok=false;
                    for(Point p: mand) if(p.x==r && p.y==c){ ok=true; break; }
                    if(!ok){
                        JOptionPane.showMessageDialog(this, "Existe captura obrigatória. Selecione uma das peças marcadas.");
                        return;
                    }
                }
                // select and fetch valid moves
                selected = new Point(r,c);
                currentValidMoves = controller.validMovesFor(r,c);
                return;
            }

            // If there is a selection and user clicked on a valid destination => execute move
            Move chosen = null;
            for(Move m: currentValidMoves){
                if(m.dest.x==r && m.dest.y==c){ chosen = m; break; }
            }
            if(chosen!=null){
                // TODO: here we will animate the piece sliding from selected -> chosen.dest prior to calling execute.
                // For now, perform move immediately (skeleton). Later replace with animateMove(selected, chosen, callback->controller.executeMove(...))
                controller.executeMove(selected.x, selected.y, chosen);

                // after execution, if a capture-sequence remains active and the controller set captureSequencePiece,
                // we should set selected to that piece (so the player continues automatically)
                if(controller.isInCaptureSequence()){
                    Point cs = controller.getCaptureSequencePiece();
                    selected = new Point(cs.x, cs.y);
                    currentValidMoves = controller.validMovesFor(cs.x, cs.y);
                } else {
                    // not in sequence: clear selection
                    selected = null;
                    currentValidMoves.clear();
                }
                return;
            }

            // clicked somewhere else that's not a valid dest, treat as selecting another piece (if allowed)
            if(piece!=0){
                int player = piece%10;
                boolean curPlayerIs1 = controller.isTurnPlayer1();
                if((curPlayerIs1 && player==1) || (!curPlayerIs1 && player==2)){
                    // allowed. Replace selection
                    selected = new Point(r,c);
                    currentValidMoves = controller.validMovesFor(r,c);
                } else {
                    // invalid target (opponent piece) -> ignore
                }
            } else {
                // clicked empty but not a valid dest -> just deselect
                selected = null;
                currentValidMoves.clear();
            }
        }

        @Override protected void paintComponent(Graphics g0){
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // draw board
            for(int r=0;r<controller.board.SIZE;r++){
                for(int c=0;c<controller.board.SIZE;c++){
                    g.setColor((r+c)%2==0? new Color(245,222,179) : new Color(139,69,19));
                    g.fillRect(c*SQUARE, r*SQUARE, SQUARE, SQUARE);
                }
            }

            // highlight mandatory pieces
            for(Point p: controller.getMandatoryPieces()){
                g.setColor(new Color(255,215,0,180));
                g.setStroke(new BasicStroke(4));
                g.drawRect(p.y*SQUARE+4, p.x*SQUARE+4, SQUARE-8, SQUARE-8);
            }

            // highlight selected
            if(selected!=null){
                g.setColor(new Color(255,215,0,180));
                g.setStroke(new BasicStroke(3));
                g.drawRect(selected.y*SQUARE+3, selected.x*SQUARE+3, SQUARE-6, SQUARE-6);

                // draw valid destinations
                for(Move m: currentValidMoves){
                    int rr = m.dest.x, cc = m.dest.y;
                    if(m.isCapture()) g.setColor(new Color(255,69,0,150)); else g.setColor(new Color(50,205,50,150));
                    int cx = cc*SQUARE + SQUARE/2, cy = rr*SQUARE + SQUARE/2;
                    g.fillOval(cx-18, cy-18, 36, 36);
                }
            }

            // draw pieces
            for(int r=0;r<controller.board.SIZE;r++){
                for(int c=0;c<controller.board.SIZE;c++){
                    int piece = controller.board.get(r,c);
                    if(piece==0) continue;
                    boolean isKing = piece>=10;
                    Color col = (piece%10==1)? new Color(220,20,60) : new Color(30,144,255);
                    // shadow
                    g.setColor(new Color(0,0,0,80));
                    g.fillOval(c*SQUARE+8, r*SQUARE+8, SQUARE-16, SQUARE-32);
                    // body
                    GradientPaint gp = new GradientPaint(c*SQUARE+12, r*SQUARE+12, col.brighter(), c*SQUARE+SQUARE-20, r*SQUARE+SQUARE-28, col.darker());
                    g.setPaint(gp);
                    g.fillOval(c*SQUARE+12, r*SQUARE+12, SQUARE-24, SQUARE-24);
                    // border
                    g.setPaint(null);
                    g.setColor(col.darker().darker());
                    g.setStroke(new BasicStroke(3));
                    g.drawOval(c*SQUARE+12, r*SQUARE+12, SQUARE-24, SQUARE-24);
                    if(isKing){
                        g.setColor(Color.YELLOW);
                        g.setFont(new Font("SansSerif", Font.BOLD, 28));
                        g.drawString("♔", c*SQUARE + SQUARE/2 - 10, r*SQUARE + SQUARE/2 + 10);
                    }
                }
            }
            controller.updateStatusLabel();
        }
    }
}
