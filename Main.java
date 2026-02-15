import game.GamePanel;
import javax.swing.JFrame;

/**
 * จุดเข้าเกม: สร้าง JFrame และ GamePanel แล้วเริ่มเกมลูป.
 */
public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame("Java RPG V3: Smooth Animations");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}