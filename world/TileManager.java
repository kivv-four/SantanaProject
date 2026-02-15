package world;

import game.GamePanel;
import java.awt.*;
import java.util.Random;

/** จัดการ tile และแมพ: กำหนดสี/การชนของแต่ละ tile โหลดแมพสุ่ม และวาดเฉพาะ tile ที่อยู่ในหน้าจอ */
public class TileManager {

    private final GamePanel gp;
    public Tile[] tiles;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[10];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        initTiles();
        loadMap();
    }

    /** สร้าง tile 0=หญ้า 1=กำแพง 2=น้ำ 3=ดิน (1,2 มี collision) */
    public void initTiles() {
        tiles[0] = new Tile();
        tiles[0].color = new Color(34, 139, 34);
        tiles[1] = new Tile();
        tiles[1].color = Color.GRAY;
        tiles[1].collision = true;
        tiles[2] = new Tile();
        tiles[2].color = new Color(0, 150, 255);
        tiles[2].collision = true;
        tiles[3] = new Tile();
        tiles[3].color = new Color(139, 69, 19);
    }

    /** สร้างแมพสุ่ม: ส่วนใหญ่หญ้า บางจุดกำแพง/น้ำ และ zone ดินตรงกลาง */
    public void loadMap() {
        Random r = new Random();
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int num = r.nextInt(100);
                if (num < 80) mapTileNum[col][row] = 0;
                else if (num < 90) mapTileNum[col][row] = 1;
                else mapTileNum[col][row] = 2;
                if (col > 20 && col < 30 && row > 20 && row < 30) mapTileNum[col][row] = 3;
            }
        }
    }

    /** วาดเฉพาะ tile ที่อยู่ในระยะมองเห็น (เทียบกับตำแหน่งผู้เล่น) */
    public void draw(Graphics2D g2) {
        for (int worldCol = 0; worldCol < gp.maxWorldCol; worldCol++) {
            for (int worldRow = 0; worldRow < gp.maxWorldRow; worldRow++) {
                int tileNum = mapTileNum[worldCol][worldRow];
                int worldX = worldCol * gp.tileSize;
                int worldY = worldRow * gp.tileSize;
                int screenX = worldX - gp.player.worldX + gp.player.screenX;
                int screenY = worldY - gp.player.worldY + gp.player.screenY;
                if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                    worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                    worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                    worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
                    g2.setColor(tiles[tileNum].color);
                    g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                }
            }
        }
    }
}
