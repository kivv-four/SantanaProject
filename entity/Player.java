package entity;

import game.GamePanel;
import input.KeyHandler;
import effect.FloatingText;
import effect.Particle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class Player extends Entity {

    private final KeyHandler keyH;
    public final int screenX, screenY;
    private boolean attacking = false;
    private int attackTimer = 0;
    public int level = 1, strength = 5, exp = 0, nextLevelExp = 50, leveledUpCounter = 0;
    private boolean moving = false;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;
        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);
        worldX = gp.tileSize * 25;
        worldY = gp.tileSize * 25;
        speed = 5;
        solidArea = new Rectangle(12, 16, 24, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        maxLife = 100;
        life = maxLife;
    }

    /** เพิ่ม exp; ถ้าครบ nextLevelExp จะ level up (เพิ่ม maxLife, strength, แสดง LEVEL UP) */
    public void gainExp(int amount) {
        exp += amount;
        if (exp >= nextLevelExp) {
            level++;
            exp = 0;
            nextLevelExp *= 1.5;
            maxLife += 20;
            life = maxLife;
            strength += 2;
            leveledUpCounter = 120;
            gp.floatingTexts.add(new FloatingText(gp, "LEVEL UP!", worldX, worldY - 40, Color.GREEN));
        }
    }

    @Override
    public void update() {
        if (life <= 0) return;

        if (keyH.attackPressed && !attacking) {
            attacking = true;
            attackTimer = 0;
            attackEntities();
        }
        if (attacking) {
            attackTimer++;
            if (attackTimer > 20) {
                attacking = false;
                attackTimer = 0;
            }
        }

        moving = false;
        if (keyH.upPressed && keyH.lastKey.equals("up")) { direction = "up"; moving = true; }
        else if (keyH.downPressed && keyH.lastKey.equals("down")) { direction = "down"; moving = true; }
        else if (keyH.leftPressed && keyH.lastKey.equals("left")) { direction = "left"; moving = true; }
        else if (keyH.rightPressed && keyH.lastKey.equals("right")) { direction = "right"; moving = true; }
        else if (keyH.upPressed) { direction = "up"; moving = true; }
        else if (keyH.downPressed) { direction = "down"; moving = true; }
        else if (keyH.leftPressed) { direction = "left"; moving = true; }
        else if (keyH.rightPressed) { direction = "right"; moving = true; }

        if (moving) {
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
            if (worldY < 0) worldY = 0;
            if (worldX < 0) worldX = 0;
            int maxX = (gp.maxWorldCol * gp.tileSize) - gp.tileSize;
            int maxY = (gp.maxWorldRow * gp.tileSize) - gp.tileSize;
            if (worldX > maxX) worldX = maxX;
            if (worldY > maxY) worldY = maxY;
        }

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    /** สร้าง hitbox โจมตีตามทิศทาง เช็ค intersect กับมอนสเตอร์ โดนแล้วเรียก damageMonster */
    private void attackEntities() {
        int attackAreaSize = gp.tileSize;
        Rectangle ar = new Rectangle(worldX, worldY, attackAreaSize, attackAreaSize);
        switch (direction) {
            case "up" -> ar.y -= gp.tileSize;
            case "down" -> ar.y += gp.tileSize;
            case "left" -> ar.x -= gp.tileSize;
            case "right" -> ar.x += gp.tileSize;
        }
        for (Entity m : gp.monsters) {
            if (m != null && m.alive && !m.invincible) {
                Rectangle mr = new Rectangle(
                    m.worldX + m.solidArea.x, m.worldY + m.solidArea.y,
                    m.solidArea.width, m.solidArea.height);
                if (ar.intersects(mr)) damageMonster(m);
            }
        }
    }

    /** คำนวณดาเมจ (strength + สุ่ม), 20% crit, ลด HP ตั้ง invincible สร้าง particle + floating text แล้ว applyRecoil */
    private void damageMonster(Entity m) {
        int dmg = strength + new Random().nextInt(5);
        boolean isCrit = new Random().nextInt(100) < 20;
        if (isCrit) dmg *= 2;
        m.life -= dmg;
        m.invincible = true;
        m.invincibleCounter = 0;
        generateParticles(m, new Color(100, 255, 100));
        gp.floatingTexts.add(new FloatingText(gp, String.valueOf(dmg), m.worldX, m.worldY, isCrit ? Color.YELLOW : Color.WHITE));
        applyRecoil(m);
    }

    /** ผลัก entity ไปทางทิศทางที่ผู้เล่นหัน โดยเช็ค tile collision ทีละ step */
    private void applyRecoil(Entity m) {
        int recoilDistance = 48;
        int step = 2;
        String originalDir = m.direction;
        m.direction = this.direction;
        for (int i = 0; i < recoilDistance; i += step) {
            m.collisionOn = false;
            gp.cChecker.checkTile(m);
            if (!m.collisionOn) {
                switch (m.direction) {
                    case "up" -> m.worldY -= step;
                    case "down" -> m.worldY += step;
                    case "left" -> m.worldX -= step;
                    case "right" -> m.worldX += step;
                }
            } else break;
        }
        m.direction = originalDir;
    }

    /** สร้าง particle จำนวน 8 ตัวที่ตำแหน่ง target */
    public void generateParticles(Entity target, Color color) {
        for (int i = 0; i < 8; i++)
            gp.particles.add(new Particle(gp, target.worldX, target.worldY, color));
    }

    @Override
    public void draw(Graphics2D g2) {
        int bobY = moving ? (int) (Math.sin(gp.gameFrame * 0.2) * 3) : 0;
        g2.setColor(Color.CYAN);
        g2.fillRect(screenX, screenY + bobY, gp.tileSize, gp.tileSize);
        g2.setColor(new Color(0, 0, 150));
        if (direction.equals("up")) g2.fillRect(screenX + 10, screenY + 10 + bobY, 28, 30);
        g2.setColor(Color.BLACK);
        if (direction.equals("right") || direction.equals("down")) g2.fillRect(screenX + 30, screenY + 10 + bobY, 6, 6);
        if (direction.equals("left") || direction.equals("down")) g2.fillRect(screenX + 10, screenY + 10 + bobY, 6, 6);
        if (attacking) drawSword(g2, bobY);
    }

    /** วาดดาบหมุนตาม attackTimer และทิศทาง */
    private void drawSword(Graphics2D g2, int bobY) {
        AffineTransform oldTransform = g2.getTransform();
        int swordCenterX = screenX + gp.tileSize / 2;
        int swordCenterY = screenY + gp.tileSize / 2 + bobY;
        double startAngle = 0;
        double swingRange = 120;
        double currentSwing = (double) (attackTimer + 1) / 20 * swingRange;
        switch (direction) {
            case "up" -> startAngle = -45 - 90;
            case "down" -> startAngle = -45 + 90;
            case "left" -> startAngle = -45 + 180;
            case "right" -> startAngle = -45;
        }
        g2.rotate(Math.toRadians(startAngle + currentSwing), swordCenterX, swordCenterY);
        g2.setColor(Color.WHITE);
        g2.fillRect(swordCenterX, swordCenterY - 40, 10, 40);
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(swordCenterX, swordCenterY, 10, 15);
        g2.setTransform(oldTransform);
    }
}
