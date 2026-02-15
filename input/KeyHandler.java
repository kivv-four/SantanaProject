package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** รับ input คีย์บอร์ด (WASD เดิน, J โจมตี, R restart) และเก็บ lastKey สำหรับ priority การเดิน */
public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed, attackPressed, restartPressed;
    public String lastKey = "";

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) { upPressed = true; lastKey = "up"; }
        if (code == KeyEvent.VK_S) { downPressed = true; lastKey = "down"; }
        if (code == KeyEvent.VK_A) { leftPressed = true; lastKey = "left"; }
        if (code == KeyEvent.VK_D) { rightPressed = true; lastKey = "right"; }
        if (code == KeyEvent.VK_J) attackPressed = true;
        if (code == KeyEvent.VK_R) restartPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_J) attackPressed = false;
        if (code == KeyEvent.VK_R) restartPressed = false;
    }
}
