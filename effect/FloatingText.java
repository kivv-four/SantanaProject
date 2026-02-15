package effect;

import game.GamePanel;
import java.awt.*;

/** ข้อความลอย (ดาเมจ, LEVEL UP): เคลื่อนที่ขึ้นทีละน้อย และหายไปหลัง 50 เฟรม */
public class FloatingText {

    private final GamePanel gp;
    private final String text;
    private int x, y;
    private final Color color;
    public boolean alive = true;
    private int counter = 0;

    public FloatingText(GamePanel gp, String text, int x, int y, Color color) {
        this.gp = gp;
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    /** เลื่อน y ขึ้น 1; หมดอายุเมื่อ counter > 50 */
    public void update() {
        counter++;
        y -= 1;
        if (counter > 50) alive = false;
    }

    /** วาดข้อความที่ตำแหน่งจอ (แปลงจาก world) พร้อมเงาดำ */
    public void draw(Graphics2D g2) {
        int screenX = x - gp.player.worldX + gp.player.screenX;
        int screenY = y - gp.player.worldY + gp.player.screenY;
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(Color.BLACK);
        g2.drawString(text, screenX + 2, screenY + 2);
        g2.setColor(color);
        g2.drawString(text, screenX, screenY);
    }
}
