import game.GamePanel;
import game.MainMenuWindow;

/**
 * จุดเข้าเกม: สร้าง MainMenuWindow ให้เมนูหลัก
 */
public class Main {

    public static void main(String[] args) {
        // สร้าง GamePanel สำหรับเกมหลัก
        GamePanel gamePanel = new GamePanel();
        
        // สร้าง Main Menu Window
        new MainMenuWindow(gamePanel);
        
        // เมื่อกด Play จะเรียก Character Selection Window
        // เมื่อเลือกตัวละคร จะเรียก startGameWithCharacter() ของ gamePanel
    }
}