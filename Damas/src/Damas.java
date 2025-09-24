import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.GameController;
import view.GamePanel;

/**
 * Criar Executável .JAR
 * jar cfm damas.jar MANIFEST.MF -C src/ . -C . sounds/
 * java -jar damas.jar
 * Executat pelo terminal
 * javac Damas.java && java Damas
 * Classe principal do Jogo de Damas.
 * Inicializa a interface gráfica e coordena os componentes do jogo.
 */
public class Damas extends JFrame {
    
    /**
     * Construtor da janela principal do jogo
     */
    public Damas() {
        super("Damas - Regras Oficiais");
        initializeWindow();
        setupGameComponents();
        displayWindow();
    }
    
    /**
     * Inicializa as configurações da janela
     */
    private void initializeWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }
    
    /**
     * Configura os componentes do jogo
     */
    private void setupGameComponents() {
        GameController controller = new GameController();
        GamePanel gamePanel = new GamePanel(controller);
        
        add(gamePanel, BorderLayout.CENTER);
        add(createSouthPanel(controller), BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel inferior com informações do jogo
     */
    private JPanel createSouthPanel(GameController controller) {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(controller.getScoreLabel(), BorderLayout.WEST);
        southPanel.add(controller.getStatusLabel(), BorderLayout.CENTER);
        
        return southPanel;
    }
    
    /**
     * Exibe a janela configurada
     */
    private void displayWindow() {
        pack();
        setResizable(false);
        setLocationRelativeTo(null); // Centraliza na tela
        setVisible(true);
    }
    
    /**
     * Método principal - ponto de entrada da aplicação
     */
    public static void main(String[] args) {
        // Garante que a interface seja criada na Thread EDT
        SwingUtilities.invokeLater(() -> new Damas());
    }
}