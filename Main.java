import game.CharacterSelectionWindow;
import game.GamePanel;

/**
 * จุดเข้าเกม: สร้าง CharacterSelectionWindow ให้เลือกตัวละคร
 */
public class Main {

    public static void main(String[] args) {
        // สร้าง GamePanel สำหรับเกมหลัก (ยังไม่เริ่มเลย)
        GamePanel gamePanel = new GamePanel();
        
        // สร้าง Character Selection Window
        new CharacterSelectionWindow(gamePanel);
        
        // เมื่อเลือกตัวละคร จะเรียก startGameWithCharacter() ของ gamePanel
        // และสร้าง main game window
    }
}