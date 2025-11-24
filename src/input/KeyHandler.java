package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class KeyHandler implements KeyListener {

    // Movement keys
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Skill keys
    public boolean skillQ, skillW, skillE, skillR, skillT;

    // Debounce mechanism as a fallback
    private Map<Integer, Long> lastPressTime = new HashMap<>();  // Key: keyCode, Value: last press time
    private final long DEBOUNCE_DELAY = 50;  // Reduced from 300ms to 50ms for better combat responsiveness

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        long currentTime = System.currentTimeMillis();  // Get current time for debounce

        switch (code) {
            // Movement
            case KeyEvent.VK_UP: upPressed = true; break;
            case KeyEvent.VK_DOWN: downPressed = true; break;
            case KeyEvent.VK_LEFT: leftPressed = true; break;
            case KeyEvent.VK_RIGHT: rightPressed = true; break;

            // Skills - remove debounce for combat responsiveness
            case KeyEvent.VK_Q:
                skillQ = true;
                break;
            case KeyEvent.VK_W:
                skillW = true;
                break;
            case KeyEvent.VK_E:
                skillE = true;
                break;
            case KeyEvent.VK_R:
                skillR = true;
                break;
            case KeyEvent.VK_T:
                skillT = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        switch (code) {
            // Movement
            case KeyEvent.VK_UP: upPressed = false; break;
            case KeyEvent.VK_DOWN: downPressed = false; break;
            case KeyEvent.VK_LEFT: leftPressed = false; break;
            case KeyEvent.VK_RIGHT: rightPressed = false; break;

            // Skills
            case KeyEvent.VK_Q: skillQ = false; break;
            case KeyEvent.VK_W: skillW = false; break;
            case KeyEvent.VK_E: skillE = false; break;
            case KeyEvent.VK_R: skillR = false; break;
            case KeyEvent.VK_T: skillT = false; break;
        }
    }
}
