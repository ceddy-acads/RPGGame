package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class KeyHandler implements KeyListener {

    // Movement keys
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Skill keys
    public boolean skillSPACE, skillW, skillE, skillR, skillT;

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
            case KeyEvent.VK_W: upPressed = true; break;
            case KeyEvent.VK_S: downPressed = true; break;
            case KeyEvent.VK_A: leftPressed = true; break;
            case KeyEvent.VK_D: rightPressed = true; break;

            // Skills - remove debounce for combat responsiveness
            case KeyEvent.VK_SPACE:
                skillSPACE = true;
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
            case KeyEvent.VK_W: upPressed = false; break;
            case KeyEvent.VK_S: downPressed = false; break;
            case KeyEvent.VK_A: leftPressed = false; break;
            case KeyEvent.VK_D: rightPressed = false; break;

            // Skills
            case KeyEvent.VK_SPACE: skillSPACE= false; break;
            case KeyEvent.VK_E: skillE = false; break;
            case KeyEvent.VK_R: skillR = false; break;
            case KeyEvent.VK_T: skillT = false; break;
        }
    }
}
