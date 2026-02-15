package entity;

import game.GamePanel;
import java.awt.*;

/**
 * ฐานของตัวละครในเกม (ผู้เล่น, มอนสเตอร์).
 * มีพิกัดโลก การเคลื่อนที่ collision box HP และสถานะ invincible.
 */
public abstract class Entity {

    protected GamePanel gp;
    public int worldX, worldY, speed;
    public String direction = "down";
    public Rectangle solidArea;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public boolean collisionOn = false;
    public int maxLife, life;
    public boolean alive = true;
    public boolean invincible = false;
    public int invincibleCounter = 0;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2);
}
