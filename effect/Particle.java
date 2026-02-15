package effect;

import game.GamePanel;
import java.awt.*;
import java.util.Random;

/** อนุภาคเอฟเฟกต์: เคลื่อนที่ตามความเร็ว xv,yv และจางหายเมื่อ life หมด */
public class Particle {

    private final GamePanel gp;
    private int x, y;
    private final double xv, yv;
    private final Color color;
    private final int size = 8;
    private int life = 25;
    public boolean alive = true;

    public Particle(GamePanel gp, int x, int y, Color color) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.color = color;
        Random rand = new Random();
        this.xv = (rand.nextDouble() * 4) - 2;
        this.yv = (rand.nextDouble() * 4) - 2;
    }

    /** เคลื่อนที่และลด life; ตายเมื่อ life < 0 */
    public void update() {
        x += xv;
        y += yv;
        life--;
        if (life < 0) alive = false;
    }

    /** วาดวงกลมที่ตำแหน่งจอ (แปลงจาก world) ด้วยความโปร่งใส */
    public void draw(Graphics2D g2) {
        int screenX = x - gp.player.worldX + gp.player.screenX;
        int screenY = y - gp.player.worldY + gp.player.screenY;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g2.fillOval(screenX, screenY, size, size);
    }
}
