package entity;

import game.GamePanel;
import effect.FloatingText;

import java.awt.*;
import java.util.Random;

/** มอนสเตอร์สไลม์: เดินสุ่มทุก 120 เฟรม ชนผู้เล่นทำดาเมจ 10; ตายให้ exp */
public class GreenSlime extends Entity {

    private int actionLockCounter = 0;
    private final double animOffset;

    public GreenSlime(GamePanel gp) {
        super(gp);
        speed = 2;
        maxLife = 30;
        life = maxLife;
        solidArea = new Rectangle(8, 16, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        animOffset = new Random().nextDouble() * 10;
    }

    /** อัปเดต: ตาย/โดนตีหยุดนิ่ง; ไม่โดนตีแล้วเดินสุ่มทิศทางและเช็ค tile; ชนผู้เล่นทำดาเมจ 10 */
    @Override
    public void update() {
        if (life <= 0) { alive = false; return; }
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 30) invincible = false;
            return;
        }
        actionLockCounter++;
        if (actionLockCounter == 120) {
            int i = new Random().nextInt(100) + 1;
            if (i <= 25) direction = "up";
            else if (i <= 50) direction = "down";
            else if (i <= 75) direction = "left";
            else direction = "right";
            actionLockCounter = 0;
        }
        collisionOn = false;
        gp.cChecker.checkTile(this);
        if (!collisionOn) {
            switch (direction) {
                case "up" -> worldY -= speed;
                case "down" -> worldY += speed;
                case "left" -> worldX -= speed;
                case "right" -> worldX += speed;
            }
        }

        // บังคับไม่ให้มอนสเตอร์ออกนอกขอบแมพ (เหมือน player)
        if (worldY < 0) worldY = 0;
        if (worldX < 0) worldX = 0;
        int maxX = (gp.maxWorldCol * gp.tileSize) - gp.tileSize;
        int maxY = (gp.maxWorldRow * gp.tileSize) - gp.tileSize;
        if (worldX > maxX) worldX = maxX;
        if (worldY > maxY) worldY = maxY;
        Rectangle monsterRect = new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
        Rectangle playerRect = new Rectangle(gp.player.worldX + gp.player.solidArea.x, gp.player.worldY + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height);
        if (monsterRect.intersects(playerRect) && !gp.player.invincible) {
            gp.player.life -= 10;
            gp.player.invincible = true;
            gp.floatingTexts.add(new FloatingText(gp, "-10", gp.player.worldX, gp.player.worldY, Color.RED));
        }
    }

    /** วาดสไลม์ (และ HP bar) เฉพาะเมื่ออยู่ในหน้าจอ; เอฟเฟกต์หายใจด้วย sin */
    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            double breathing = Math.sin((gp.gameFrame * 0.1) + animOffset);
            int scaleX = (int) (breathing * 3);
            int scaleY = (int) (breathing * 2);
            g2.setColor(invincible ? Color.WHITE : new Color(50, 200, 50));
            g2.fillOval(screenX - scaleX, screenY + 10 + scaleY, gp.tileSize + (scaleX * 2), gp.tileSize - 10 - (scaleY * 2));
            g2.setColor(Color.BLACK);
            g2.fillOval(screenX + 10, screenY + 20, 8, 8);
            g2.fillOval(screenX + 30, screenY + 20, 8, 8);
            drawHPBar(g2, screenX, screenY);
        }
    }

    private void drawHPBar(Graphics2D g2, int screenX, int screenY) {
        double hpScale = (double) gp.tileSize / maxLife;
        int barWidth = (int) (life * hpScale);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(screenX - 1, screenY - 6, gp.tileSize + 2, 7);
        g2.setColor(new Color(255, 0, 30));
        g2.fillRect(screenX, screenY - 5, barWidth, 5);
    }
}
