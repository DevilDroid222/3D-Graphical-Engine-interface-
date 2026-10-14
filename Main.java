import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Grid3D - Budowanie + Spektator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            Grid3D grid = new Grid3D();
            frame.add(grid, BorderLayout.CENTER);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            controls.setBackground(new Color(240, 242, 245));
            controls.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 222, 229)));

            JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            colorPanel.setOpaque(false);
            colorPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Kolor klocka"));
            addColorButton(colorPanel, grid, "Czerwony", new Color(225, 71, 71));
            addColorButton(colorPanel, grid, "Niebieski", new Color(66, 133, 244));
            addColorButton(colorPanel, grid, "Zielony", new Color(52, 168, 83));
            addColorButton(colorPanel, grid, "Żółty", new Color(251, 188, 5));
            addCustomColorButton(colorPanel, grid);
            addImageTextureButton(colorPanel, grid);

            JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            modePanel.setOpaque(false);
            modePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Tryb budowy"));
            JComboBox<Grid3D.BuildMode> modes = new JComboBox<>(Grid3D.BuildMode.values());
            modes.setPreferredSize(new Dimension(130, 28));
            modes.addActionListener(e -> grid.setBuildMode((Grid3D.BuildMode) modes.getSelectedItem()));
            modePanel.add(modes);

            JPanel wallHeightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            wallHeightPanel.setOpaque(false);
            wallHeightPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Wysokość"));
            JSpinner wallHeightSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 40, 1));
            wallHeightSpinner.setPreferredSize(new Dimension(50, 28));
            wallHeightSpinner.addChangeListener(e -> grid.setWallHeight((Integer) wallHeightSpinner.getValue()));
            wallHeightPanel.add(wallHeightSpinner);

            JPanel radiusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            radiusPanel.setOpaque(false);
            radiusPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Promień"));
            JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
            radiusSpinner.setPreferredSize(new Dimension(50, 28));
            radiusSpinner.addChangeListener(e -> grid.setShapeRadius((Integer) radiusSpinner.getValue()));
            radiusPanel.add(radiusSpinner);

            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            actionPanel.setOpaque(false);
            actionPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Akcje"));
            JButton undo = createStyledButton("Cofnij (Ctrl+Z)", new Color(108, 117, 125));
            undo.addActionListener(e -> grid.undo());
            JButton clear = createStyledButton("Wyczyść", new Color(220, 53, 69));
            clear.addActionListener(e -> grid.clearBlocks());
            JButton fpsBtn = createStyledButton("Spektator (Tab)", new Color(40, 167, 69));
            fpsBtn.addActionListener(e -> grid.toggleFpsMode());
            actionPanel.add(undo);
            actionPanel.add(clear);
            actionPanel.add(fpsBtn);

            controls.add(colorPanel);
            controls.add(modePanel);
            controls.add(wallHeightPanel);
            controls.add(radiusPanel);
            controls.add(actionPanel);

            frame.add(controls, BorderLayout.SOUTH);
            frame.setVisible(true);
            grid.requestFocusInWindow();
        });
    }

    private static void addColorButton(JPanel panel, Grid3D grid, String text, Color color) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(32, 32));
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        button.setToolTipText(text);
        button.addActionListener(e -> grid.setSelectedColorAndTexture(color, null));
        panel.add(button);
    }

    private static void addCustomColorButton(JPanel panel, Grid3D grid) {
        JButton button = new JButton("+");
        button.setPreferredSize(new Dimension(32, 32));
        button.setBackground(new Color(230, 235, 240));
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Własny kolor");
        button.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(panel, "Kolor", grid.getSelectedColor());
            if (chosen != null) grid.setSelectedColorAndTexture(chosen, null);
        });
        panel.add(button);
    }

    private static void addImageTextureButton(JPanel panel, Grid3D grid) {
        JButton button = new JButton("IMG");
        button.setPreferredSize(new Dimension(42, 32));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                    if (img != null) grid.setSelectedColorAndTexture(Color.WHITE, img);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Błąd: " + ex.getMessage());
                }
            }
        });
        panel.add(button);
    }

    private static JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? baseColor.darker() :
                        getModel().isRollover() ? baseColor.brighter() : baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 32));
        return button;
    }
}

class Grid3D extends JPanel {

    private static final int SIZE = 120;
    private static final int MAX_HEIGHT = 200;
    private static final double CELL_SIZE = 24.0;
    private static final double SCALE = 0.1;

    enum BuildMode {
        SZEREG("Szereg"), PLATFORMA("Platforma"), SCIANA("Ściana"), SCHODY("Schody"),
        STOZEK("Stożek"), PIRAMIDA("Piramida"), CYLINDER("Cylinder"), KULA("Kula"), TRAPEZ("Trapez");
        private final String label;
        BuildMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private double zoom = 1.0;
    private double rotationAngle = 0.0;
    private double panX = 0.0, panY = 0.0;

    private boolean fpsMode = false;
    private double playerX = 20.5, playerY = 20.5, playerZ = 1.0;
    private double yaw = 0.0, pitch = -0.15;

    private static final double ACCEL = 55.0;
    private static final double FRICTION = 20.0;
    private static final double COLLIDE_R = 0.28;
    private static final double COLLIDE_H = 0.45;

    private static final double GRAVITY = 28.0;
    private static final double JUMP_SPEED = Math.sqrt(2.0 * GRAVITY * 2.0);
    private static final double HORIZONTAL_SPEED = 10.0;

    private double velMoveX = 0, velMoveY = 0, velMoveZ = 0;
    private boolean onGround = false;

    private final Set<Integer> keys = new HashSet<>();
    private boolean mouseCaptured = false;
    private Point lastMouse;

    private Color selectedColor = new Color(225, 71, 71);
    private BufferedImage selectedTexture = null;
    private BuildMode buildMode = BuildMode.SZEREG;
    private int wallHeight = 5;
    private int shapeRadius = 4;

    private static final class BlockData {
        final Color color;
        final BufferedImage texture;
        BlockData(Color color, BufferedImage texture) {
            this.color = color;
            this.texture = texture;
        }
    }

    private final Map<Cell3, BlockData> blocks = new HashMap<>();
    private final Deque<Map<Cell3, BlockData>> undoStack = new ArrayDeque<>();

    private Point lastMousePosition;
    private boolean rotating, panning, building, erasing;
    private Cell3 anchor, lastPathCell, previewTarget, chainEnd;
    private List<Cell3> preview = new ArrayList<>();

    private final javax.swing.Timer gameTimer;

    public Grid3D() {
        setBackground(Color.WHITE);
        setFocusable(true);

        gameTimer = new javax.swing.Timer(16, e -> {
            if (fpsMode) {
                updatePlayer(0.016);
                repaint();
            }
        });
        gameTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keys.add(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_TAB) toggleFpsMode();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mouseCaptured = false;
                    setCursor(Cursor.getDefaultCursor());
                }
                if (e.getKeyCode() == KeyEvent.VK_Z && e.isControlDown()) undo();
            }
            @Override
            public void keyReleased(KeyEvent e) {
                keys.remove(e.getKeyCode());
            }
        });

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastMousePosition = e.getPoint();
                lastMouse = e.getPoint();

                if (fpsMode) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (!mouseCaptured) {
                            mouseCaptured = true;
                            lastMouse = e.getPoint();
                            setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                                    new Point(0, 0), "empty"));
                        } else {
                            breakBlockInFront();
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        if (!mouseCaptured) {
                            mouseCaptured = true;
                            lastMouse = e.getPoint();
                            setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                                    new Point(0, 0), "empty"));
                        }
                        placeBlockInFront();
                    }
                    return;
                }

                if (SwingUtilities.isMiddleMouseButton(e)) {
                    rotating = true;
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    erasing = true;
                    saveUndoState();
                    removeAtScreen(e.getPoint());
                    repaint();
                    return;
                }
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (e.isShiftDown()) {
                    panning = true;
                    return;
                }

                if (isShapeMode()) {
                    building = true;
                    saveUndoState();
                    placeShapeAt(e.getPoint());
                    building = false;
                    repaint();
                    return;
                }

                building = true;
                saveUndoState();
                Cell3 hit = findVisibleBlock(e.getPoint());
                if (hit != null) {
                    int targetZ = topHeight(hit.x, hit.y);
                    Cell3 start = new Cell3(hit.x, hit.y, targetZ);
                    if (!inBounds(start) || blocks.containsKey(start)) {
                        cancelBuild();
                        return;
                    }
                    blocks.put(start, new BlockData(selectedColor, selectedTexture));
                    anchor = chainEnd = lastPathCell = previewTarget = start;
                } else {
                    Point3D ground = screenToGridAtZ(e.getPoint(), 0);
                    if (!inBounds(ground.x, ground.y)) {
                        cancelBuild();
                        return;
                    }
                    int z = topHeight(ground.x, ground.y);
                    if (z >= MAX_HEIGHT) {
                        cancelBuild();
                        return;
                    }
                    Cell3 start = new Cell3(ground.x, ground.y, z);
                    if (z > 0 && !hasNeighbor(start)) {
                        cancelBuild();
                        return;
                    }
                    blocks.put(start, new BlockData(selectedColor, selectedTexture));
                    anchor = chainEnd = lastPathCell = previewTarget = start;
                }
                updatePreview(e.getPoint());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (fpsMode && mouseCaptured) {
                    handleMouseLook(e);
                    return;
                }
                if (lastMousePosition == null) {
                    lastMousePosition = e.getPoint();
                    return;
                }
                int dx = e.getX() - lastMousePosition.x;
                int dy = e.getY() - lastMousePosition.y;
                if (rotating) rotationAngle += dx * 0.01;
                else if (panning) {
                    panX += dx;
                    panY += dy;
                } else if (building && !isShapeMode()) updatePreview(e.getPoint());
                else if (erasing) removeAtScreen(e.getPoint());
                lastMousePosition = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!fpsMode && building && !isShapeMode()) {
                    updatePreview(e.getPoint());
                    commitPreview();
                }
                rotating = panning = building = erasing = false;
                anchor = chainEnd = lastPathCell = previewTarget = null;
                preview.clear();
                lastMousePosition = null;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (fpsMode && mouseCaptured) handleMouseLook(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (fpsMode) return;
                double oldZoom = zoom;
                zoom *= (e.getWheelRotation() < 0) ? 1.1 : 1.0 / 1.1;
                zoom = Math.max(0.2, Math.min(4.0, zoom));
                double factor = zoom / oldZoom;
                panX = e.getX() - (e.getX() - panX) * factor;
                panY = e.getY() - (e.getY() - panY) * factor;
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
    }

    private void handleMouseLook(MouseEvent e) {
        if (lastMouse == null) {
            lastMouse = e.getPoint();
            return;
        }
        int mdx = e.getX() - lastMouse.x;
        int mdy = e.getY() - lastMouse.y;
        yaw   += mdx * 0.0035;
        pitch -= mdy * 0.0035;
        pitch  = Math.max(-1.50, Math.min(1.50, pitch));
        lastMouse = e.getPoint();
        try {
            Robot r = new Robot();
            Point p = getLocationOnScreen();
            r.mouseMove(p.x + getWidth() / 2, p.y + getHeight() / 2);
            lastMouse = new Point(getWidth() / 2, getHeight() / 2);
        } catch (Exception ignored) {}
    }

    public void toggleFpsMode() {
        fpsMode = !fpsMode;
        if (fpsMode) {
            playerX = Math.max(COLLIDE_R + 0.5, Math.min(SIZE - COLLIDE_R - 0.5, 20.5));
            playerY = Math.max(COLLIDE_R + 0.5, Math.min(SIZE - COLLIDE_R - 0.5, 20.5));
            playerZ = topHeight((int) playerX, (int) playerY) + COLLIDE_H + 0.15;
            while (collidesAt(playerX, playerY, playerZ) && playerZ < MAX_HEIGHT - 1) {
                playerZ += 0.5;
            }
            yaw = 0;
            pitch = -0.1;
            velMoveX = velMoveY = velMoveZ = 0;
            onGround = false;
            mouseCaptured = false;
            setCursor(Cursor.getDefaultCursor());
        } else {
            mouseCaptured = false;
            setCursor(Cursor.getDefaultCursor());
        }
        requestFocusInWindow();
        repaint();
    }

    private boolean collidesAt(double x, double y, double z) {
        int minX = (int) Math.floor(x - COLLIDE_R);
        int maxX = (int) Math.floor(x + COLLIDE_R);
        int minY = (int) Math.floor(y - COLLIDE_R);
        int maxY = (int) Math.floor(y + COLLIDE_R);
        int minZ = (int) Math.floor(z - COLLIDE_H);
        int maxZ = (int) Math.floor(z + COLLIDE_H);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (blocks.containsKey(new Cell3(bx, by, bz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updatePlayer(double dt) {
        double forward = 0, strafe = 0;
        if (keys.contains(KeyEvent.VK_W)) forward += 1;
        if (keys.contains(KeyEvent.VK_S)) forward -= 1;
        if (keys.contains(KeyEvent.VK_A)) strafe -= 1;
        if (keys.contains(KeyEvent.VK_D)) strafe += 1;

        double speed = HORIZONTAL_SPEED;
        if (keys.contains(KeyEvent.VK_SHIFT)) speed *= 1.8;

        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);

        double wishX = forward * cosY - strafe * sinY;
        double wishY = forward * sinY + strafe * cosY;
        double len = Math.sqrt(wishX * wishX + wishY * wishY);
        if (len > 0.001) {
            wishX = wishX / len * speed;
            wishY = wishY / len * speed;
        } else {
            wishX = wishY = 0;
        }

        double rate = (len > 0.001 ? ACCEL : FRICTION) * dt;
        velMoveX = approach(velMoveX, wishX, rate);
        velMoveY = approach(velMoveY, wishY, rate);

        if (keys.contains(KeyEvent.VK_SPACE) && onGround) {
            velMoveZ = JUMP_SPEED;
            onGround = false;
        }

        velMoveZ -= GRAVITY * dt;

        double nx = playerX + velMoveX * dt;
        if (!collidesAt(nx, playerY, playerZ)) {
            playerX = nx;
        } else {
            velMoveX = 0;
        }

        double ny = playerY + velMoveY * dt;
        if (!collidesAt(playerX, ny, playerZ)) {
            playerY = ny;
        } else {
            velMoveY = 0;
        }

        double nz = playerZ + velMoveZ * dt;
        if (!collidesAt(playerX, playerY, nz)) {
            playerZ = nz;
            onGround = false;
        } else {
            if (velMoveZ < 0) onGround = true;
            velMoveZ = 0;
        }

        double min = COLLIDE_R + 0.01;
        double max = SIZE - COLLIDE_R - 0.01;
        playerX = Math.max(min, Math.min(max, playerX));
        playerY = Math.max(min, Math.min(max, playerY));
        playerZ = Math.max(COLLIDE_H + 0.05, Math.min(MAX_HEIGHT - 1, playerZ));
    }

    private static double approach(double current, double target, double maxDelta) {
        if (current < target) return Math.min(current + maxDelta, target);
        if (current > target) return Math.max(current - maxDelta, target);
        return target;
    }

    private void breakBlockInFront() {
        Cell3 target = raycast(true);
        if (target != null) {
            saveUndoState();
            blocks.remove(target);
        }
    }

    private void placeBlockInFront() {
        Cell3 target = raycast(false);
        if (target != null && inBounds(target) && !blocks.containsKey(target)) {
            saveUndoState();
            blocks.put(target, new BlockData(selectedColor, selectedTexture));
        }
    }

    private Cell3 raycast(boolean hitBlock) {
        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);
        double cosP = Math.cos(pitch);
        double sinP = Math.sin(pitch);

        double dx = cosP * cosY;
        double dy = cosP * sinY;
        double dz = sinP;

        double x = playerX, y = playerY, z = playerZ;
        Cell3 lastAir = null;
        double step = 0.06;

        for (int i = 0; i < 150; i++) {
            x += dx * step;
            y += dy * step;
            z += dz * step;
            int bx = (int) Math.floor(x);
            int by = (int) Math.floor(y);
            int bz = (int) Math.floor(z);
            Cell3 c = new Cell3(bx, by, bz);
            if (blocks.containsKey(c)) return hitBlock ? c : lastAir;
            lastAir = c;
            if (bz < 0) return hitBlock ? null : lastAir;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (fpsMode) {
            drawFpsView(g2);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            drawGrid(g2);
            drawBlocks(g2);
            drawPreview(g2);
        }

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(10, 10, 360, fpsMode ? 100 : 48, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        if (fpsMode) {
            g2.drawString("SPEKTATOR  |  Tab = budowanie", 20, 30);
            g2.drawString("WASD – chodzenie   Spacja – skok (max 2)   Shift – bieg", 20, 50);
            g2.drawString("1. LPM = złap mysz   kolejne LPM = niszcz", 20, 70);
            g2.drawString("PPM = stawiaj   Esc = zwolnij mysz", 20, 90);
        } else {
            g2.drawString("BUDOWANIE  |  Tab / przycisk = spektator", 20, 35);
        }
        g2.dispose();
    }

    private void drawFpsView(Graphics2D g2) {
        int w = getWidth(), h = getHeight();
        GradientPaint sky = new GradientPaint(0, 0, new Color(110, 180, 230),
                0, h / 2, new Color(180, 220, 255));
        g2.setPaint(sky);
        g2.fillRect(0, 0, w, h / 2);
        g2.setColor(new Color(110, 180, 230));
        g2.fillRect(0, h / 2, w, h / 2);

        List<Map.Entry<Cell3, BlockData>> visible = new ArrayList<>();
        for (Map.Entry<Cell3, BlockData> e : blocks.entrySet()) {
            Cell3 c = e.getKey();
            double dx = c.x + 0.5 - playerX;
            double dy = c.y + 0.5 - playerY;
            double dz = c.z + 0.5 - playerZ;
            if (dx * dx + dy * dy + dz * dz < 50 * 50) visible.add(e);
        }
        visible.sort((a, b) -> Double.compare(dist2(b.getKey()), dist2(a.getKey())));
        for (Map.Entry<Cell3, BlockData> e : visible) {
            drawCubeFps(g2, e.getKey(), e.getValue());
        }

        g2.setColor(new Color(255, 255, 255, 200));
        int cx = w / 2, cy = h / 2;
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cx - 10, cy, cx + 10, cy);
        g2.drawLine(cx, cy - 10, cx, cy + 10);
    }

    private double dist2(Cell3 c) {
        double dx = c.x + 0.5 - playerX;
        double dy = c.y + 0.5 - playerY;
        double dz = c.z + 0.5 - playerZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private void drawCubeFps(Graphics2D g2, Cell3 c, BlockData data) {
        boolean[] solid = {
                blocks.containsKey(new Cell3(c.x, c.y, c.z + 1)),
                blocks.containsKey(new Cell3(c.x, c.y, c.z - 1)),
                blocks.containsKey(new Cell3(c.x, c.y - 1, c.z)),
                blocks.containsKey(new Cell3(c.x, c.y + 1, c.z)),
                blocks.containsKey(new Cell3(c.x - 1, c.y, c.z)),
                blocks.containsKey(new Cell3(c.x + 1, c.y, c.z))
        };

        double[][] verts = {
                {c.x, c.y, c.z}, {c.x+1, c.y, c.z}, {c.x+1, c.y+1, c.z}, {c.x, c.y+1, c.z},
                {c.x, c.y, c.z+1}, {c.x+1, c.y, c.z+1}, {c.x+1, c.y+1, c.z+1}, {c.x, c.y+1, c.z+1}
        };

        Point[] screen = new Point[8];
        boolean any = false;
        for (int i = 0; i < 8; i++) {
            screen[i] = projectFps(verts[i][0], verts[i][1], verts[i][2]);
            if (screen[i] != null) any = true;
        }
        if (!any) return;

        int[][] faces = {
                {4,5,6,7}, {0,1,2,3}, {0,1,5,4}, {2,3,7,6}, {0,3,7,4}, {1,2,6,5}
        };

        Color base = data.color;
        Color[] faceColors = {
                base.brighter(), base.darker().darker(), base, base.darker(),
                base.brighter().darker(), base.darker()
        };

        for (int f = 0; f < 6; f++) {
            if (solid[f]) continue;

            Polygon poly = new Polygon();
            boolean ok = true;
            for (int idx : faces[f]) {
                if (screen[idx] == null) { ok = false; break; }
                poly.addPoint(screen[idx].x, screen[idx].y);
            }
            if (!ok || poly.npoints < 3) continue;

            g2.setColor(new Color(
                    faceColors[f].getRed(),
                    faceColors[f].getGreen(),
                    faceColors[f].getBlue(),
                    255));
            g2.fillPolygon(poly);
        }
    }

    private Point projectFps(double wx, double wy, double wz) {
        double dx = wx - playerX;
        double dy = wy - playerY;
        double dz = wz - playerZ;

        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);
        double fx =  dx * cosY + dy * sinY;
        double rx = -dx * sinY + dy * cosY;
        double ux =  dz;

        double cosP = Math.cos(pitch);
        double sinP = Math.sin(pitch);
        double fx2 = fx * cosP - ux * sinP;
        double ux2 = fx * sinP + ux * cosP;

        if (fx2 < 0.12) return null;

        double fov = 1.2;
        double scale = (getHeight() * 0.5) / (Math.tan(fov / 2.0) * fx2);
        int sx = (int) (getWidth()  / 2.0 + rx  * scale);
        int sy = (int) (getHeight() / 2.0 - ux2 * scale);
        return new Point(sx, sy);
    }

    private boolean isShapeMode() {
        return buildMode == BuildMode.STOZEK || buildMode == BuildMode.PIRAMIDA
                || buildMode == BuildMode.CYLINDER || buildMode == BuildMode.KULA
                || buildMode == BuildMode.TRAPEZ;
    }

    private void placeShapeAt(Point mouse) {
        Cell3 hit = findVisibleBlock(mouse);
        int baseX, baseY, baseZ;
        if (hit != null) {
            baseX = hit.x; baseY = hit.y; baseZ = topHeight(hit.x, hit.y);
        } else {
            Point3D ground = screenToGridAtZ(mouse, 0);
            if (!inBounds(ground.x, ground.y)) return;
            baseX = ground.x; baseY = ground.y; baseZ = topHeight(ground.x, ground.y);
        }
        if (baseZ >= MAX_HEIGHT) return;

        List<Cell3> shapeBlocks = switch (buildMode) {
            case STOZEK   -> generateCone(baseX, baseY, baseZ, shapeRadius, wallHeight);
            case PIRAMIDA -> generatePyramid(baseX, baseY, baseZ, shapeRadius, wallHeight);
            case CYLINDER -> generateCylinder(baseX, baseY, baseZ, shapeRadius, wallHeight);
            case KULA     -> generateSphere(baseX, baseY, baseZ, shapeRadius);
            case TRAPEZ   -> generateTrapezoid(baseX, baseY, baseZ, shapeRadius, wallHeight);
            default -> List.of();
        };
        for (Cell3 c : shapeBlocks) {
            if (inBounds(c) && !blocks.containsKey(c))
                blocks.put(c, new BlockData(selectedColor, selectedTexture));
        }
    }

    private List<Cell3> generateCone(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> r = new ArrayList<>();
        for (int h = 0; h < height; h++) {
            double t = height <= 1 ? 0 : (double) h / (height - 1);
            int rad = Math.max(0, (int) Math.round(radius * (1 - t)));
            for (int dx = -rad; dx <= rad; dx++)
                for (int dy = -rad; dy <= rad; dy++)
                    if (dx*dx + dy*dy <= rad*rad + rad*0.3)
                        r.add(new Cell3(cx+dx, cy+dy, cz+h));
        }
        return r;
    }

    private List<Cell3> generatePyramid(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> r = new ArrayList<>();
        for (int h = 0; h < height; h++) {
            double t = height <= 1 ? 0 : (double) h / (height - 1);
            int rad = Math.max(0, (int) Math.round(radius * (1 - t)));
            for (int dx = -rad; dx <= rad; dx++)
                for (int dy = -rad; dy <= rad; dy++)
                    r.add(new Cell3(cx+dx, cy+dy, cz+h));
        }
        return r;
    }

    private List<Cell3> generateCylinder(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> r = new ArrayList<>();
        for (int h = 0; h < height; h++)
            for (int dx = -radius; dx <= radius; dx++)
                for (int dy = -radius; dy <= radius; dy++)
                    if (dx*dx + dy*dy <= radius*radius + radius*0.3)
                        r.add(new Cell3(cx+dx, cy+dy, cz+h));
        return r;
    }

    private List<Cell3> generateSphere(int cx, int cy, int cz, int radius) {
        List<Cell3> r = new ArrayList<>();
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++)
            for (int dy = -radius; dy <= radius; dy++)
                for (int dz = -radius; dz <= radius; dz++)
                    if (dx*dx + dy*dy + dz*dz <= r2 + radius*0.4)
                        r.add(new Cell3(cx+dx, cy+dy, cz+dz+radius));
        return r;
    }

    private List<Cell3> generateTrapezoid(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> r = new ArrayList<>();
        int topR = Math.max(1, radius / 2);
        for (int h = 0; h < height; h++) {
            double t = height <= 1 ? 0 : (double) h / (height - 1);
            int curR = (int) Math.round(radius + (topR - radius) * t);
            for (int dx = -curR; dx <= curR; dx++)
                for (int dy = -radius; dy <= radius; dy++)
                    if (Math.abs(dy) <= radius * (1 - t * 0.4))
                        r.add(new Cell3(cx+dx, cy+dy, cz+h));
        }
        return r;
    }

    public void setSelectedColorAndTexture(Color c, BufferedImage t) {
        selectedColor = c; selectedTexture = t;
    }
    public Color getSelectedColor() { return selectedColor; }
    public void setBuildMode(BuildMode m) { if (m != null) buildMode = m; }
    public void setWallHeight(int h) { wallHeight = h; }
    public void setShapeRadius(int r) { shapeRadius = r; }

    public void undo() {
        if (!undoStack.isEmpty()) {
            blocks.clear();
            blocks.putAll(undoStack.pop());
            repaint();
        }
    }

    public void clearBlocks() {
        if (blocks.isEmpty()) return;
        saveUndoState();
        blocks.clear();
        repaint();
    }

    private void saveUndoState() {
        undoStack.push(new HashMap<>(blocks));
        while (undoStack.size() > 100) undoStack.removeLast();
    }

    private void cancelBuild() {
        building = false;
        anchor = chainEnd = lastPathCell = previewTarget = null;
        preview.clear();
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
    private boolean inBounds(Cell3 c) {
        return inBounds(c.x, c.y) && c.z >= 0 && c.z < MAX_HEIGHT;
    }

    private int topHeight(int x, int y) {
        int h = -1;
        for (Cell3 c : blocks.keySet())
            if (c.x == x && c.y == y) h = Math.max(h, c.z);
        return h + 1;
    }

    private boolean areNeighbors(Cell3 a, Cell3 b) {
        int dx = Math.abs(a.x - b.x), dy = Math.abs(a.y - b.y), dz = Math.abs(a.z - b.z);
        if (buildMode == BuildMode.SCHODY)
            return dx <= 1 && dy <= 1 && dz <= 1 && (dx + dy + dz > 0);
        return dx + dy + dz == 1;
    }

    private boolean hasNeighbor(Cell3 c) {
        if (buildMode == BuildMode.SCHODY) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++)
                        if ((dx | dy | dz) != 0 && blocks.containsKey(new Cell3(c.x+dx, c.y+dy, c.z+dz)))
                            return true;
            return false;
        }
        return blocks.containsKey(new Cell3(c.x+1,c.y,c.z)) || blocks.containsKey(new Cell3(c.x-1,c.y,c.z))
                || blocks.containsKey(new Cell3(c.x,c.y+1,c.z)) || blocks.containsKey(new Cell3(c.x,c.y-1,c.z))
                || blocks.containsKey(new Cell3(c.x,c.y,c.z+1)) || blocks.containsKey(new Cell3(c.x,c.y,c.z-1));
    }

    private Point3D screenToGridAtZ(Point mouse, int z) {
        double unit = CELL_SIZE * zoom;
        double sx = (mouse.x - getWidth()/2.0 - panX) / unit;
        double sy = (mouse.y - getHeight()/2.0 - panY) / unit + z;
        double rx = sy + sx * 0.5, ry = sy - sx * 0.5;
        double cos = Math.cos(rotationAngle), sin = Math.sin(rotationAngle);
        return new Point3D((int)Math.floor(rx*cos + ry*sin), (int)Math.floor(-rx*sin + ry*cos));
    }

    private void updatePreview(Point mouse) {
        if (anchor == null) { preview.clear(); return; }
        Point3D p = screenToGridAtZ(mouse, anchor.z);
        if (!inBounds(p.x, p.y)) { preview.clear(); return; }
        previewTarget = new Cell3(p.x, p.y, anchor.z);
        preview = createBuildPath(anchor, previewTarget);
    }

    private List<Cell3> createBuildPath(Cell3 start, Cell3 end) {
        List<Cell3> r = new ArrayList<>();
        int x = start.x, y = start.y;
        while (x != end.x) { x += Integer.compare(end.x, x); r.add(new Cell3(x, y, start.z)); }
        while (y != end.y) { y += Integer.compare(end.y, y); r.add(new Cell3(x, y, start.z)); }
        if (buildMode == BuildMode.SCHODY)
            for (int i = 0; i < r.size(); i++) {
                Cell3 c = r.get(i);
                r.set(i, new Cell3(c.x, c.y, Math.min(MAX_HEIGHT-1, start.z + i + 1)));
            }
        return r;
    }

    private void commitPreview() {
        if (anchor == null || preview.isEmpty()) return;
        Cell3 prev = chainEnd;
        for (Cell3 cand : preview) {
            List<Cell3> toPlace = new ArrayList<>();
            if (buildMode == BuildMode.PLATFORMA) {
                for (int x = Math.min(anchor.x, cand.x); x <= Math.max(anchor.x, cand.x); x++)
                    for (int y = Math.min(anchor.y, cand.y); y <= Math.max(anchor.y, cand.y); y++)
                        toPlace.add(new Cell3(x, y, anchor.z));
            } else if (buildMode == BuildMode.SCIANA) {
                for (int z = anchor.z; z < anchor.z + wallHeight && z < MAX_HEIGHT; z++)
                    toPlace.add(new Cell3(cand.x, cand.y, z));
            } else toPlace.add(cand);

            for (Cell3 c : toPlace) {
                if (!inBounds(c) || blocks.containsKey(c)) continue;
                if (c.z == 0 || (prev != null && areNeighbors(prev, c)) || hasNeighbor(c)) {
                    blocks.put(c, new BlockData(selectedColor, selectedTexture));
                    prev = c;
                }
            }
        }
        repaint();
    }

    private void removeAtScreen(Point mouse) {
        Cell3 hit = findVisibleBlock(mouse);
        if (hit != null) { blocks.remove(hit); repaint(); return; }
        Point3D p = screenToGridAtZ(mouse, 0);
        if (!inBounds(p.x, p.y)) return;
        int z = topHeight(p.x, p.y) - 1;
        if (z >= 0) { blocks.remove(new Cell3(p.x, p.y, z)); repaint(); }
    }

    private Cell3 findVisibleBlock(Point mouse) {
        Cell3 result = null;
        double best = Double.NEGATIVE_INFINITY;
        for (Cell3 c : blocks.keySet()) {
            for (Polygon face : getCubeFaces(c.x, c.y, c.z)) {
                if (face.contains(mouse)) {
                    double d = depth(c.x+0.5, c.y+0.5, c.z+0.5);
                    if (result == null || d > best) { result = c; best = d; }
                }
            }
        }
        return result;
    }

    private Point2D.Double project(double x, double y, double z) {
        double cos = Math.cos(rotationAngle), sin = Math.sin(rotationAngle);
        double rx = x*cos - y*sin, ry = x*sin + y*cos;
        double unit = CELL_SIZE * zoom;
        return new Point2D.Double(getWidth()/2.0 + (rx-ry)*unit + panX,
                getHeight()/2.0 + (rx+ry)*unit*0.5 - z*unit + panY);
    }

    private double depth(double x, double y, double z) {
        double cos = Math.cos(rotationAngle), sin = Math.sin(rotationAngle);
        return (x*cos - y*sin) + (x*sin + y*cos) + z;
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(170, 170, 175));
        g2.setStroke(new BasicStroke(1.1f));
        for (int i = 0; i <= SIZE; i++) {
            Point2D.Double a = project(i, 0, 0);
            Point2D.Double b = project(i, SIZE, 0);
            g2.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
            Point2D.Double c = project(0, i, 0);
            Point2D.Double d = project(SIZE, i, 0);
            g2.drawLine((int) c.x, (int) c.y, (int) d.x, (int) d.y);
        }
    }

    private void drawBlocks(Graphics2D g2) {
        List<Map.Entry<Cell3, BlockData>> list = new ArrayList<>(blocks.entrySet());
        list.sort(Comparator.comparingDouble(e ->
                depth(e.getKey().x+0.5, e.getKey().y+0.5, e.getKey().z+0.5)));
        for (var e : list)
            drawCube(g2, e.getKey().x, e.getKey().y, e.getKey().z,
                    e.getValue().color, e.getValue().texture, 255);
    }

    private void drawPreview(Graphics2D g2) {
        if (!building || preview.isEmpty()) return;
        for (Cell3 c : preview)
            if (inBounds(c) && !blocks.containsKey(c))
                drawCube(g2, c.x, c.y, c.z, selectedColor, selectedTexture, 100);
    }

    private Polygon[] getCubeFaces(int x, int y, int z) {
        
        Point2D.Double p1 = project(x,y,z+1), p2 = project(x+1,y,z+1),
                p3 = project(x+1,y+1,z+1), p4 = project(x,y+1,z+1);
        Point2D.Double b1 = project(x,y,z), b2 = project(x+1,y,z),
                b3 = project(x+1,y+1,z), b4 = project(x,y+1,z);
        return new Polygon[]{
                polygon(p1,p2,p3,p4), polygon(b1,b2,b3,b4), polygon(p1,p2,b2,b1),
                polygon(p4,p3,b3,b4), polygon(p1,p4,b4,b1), polygon(p2,p3,b3,b2)
        };
    }

    private void drawCube(Graphics2D g2, int x, int y, int z, Color color, BufferedImage texture, int alpha) {
        Polygon[] faces = getCubeFaces(x, y, z);
        Color[] colors = {
                color.brighter(), color.darker().darker(), color, color.darker(),
                color.brighter().darker(), color.darker()
        };
        double[] depths = {
                depth(x+0.5,y+0.5,z+1), depth(x+0.5,y+0.5,z), depth(x+0.5,y,z+0.5),
                depth(x+0.5,y+1,z+0.5), depth(x,y+0.5,z+0.5), depth(x+1,y+0.5,z+0.5)
        };
        Integer[] order = {0,1,2,3,4,5};
        Arrays.sort(order, Comparator.comparingDouble(i -> depths[i]));
        for (int i : order) {
            Polygon face = faces[i];
            Color c = colors[i];
            if (texture != null) {
                Rectangle b = face.getBounds();
                if (b.width > 0 && b.height > 0) {
                    try {
                        g2.setPaint(new TexturePaint(texture, b));
                        g2.fillPolygon(face);
                    } catch (Exception ex) {
                        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
                        g2.fillPolygon(face);
                    }
                } else {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
                    g2.fillPolygon(face);
                }
            } else {
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
                g2.fillPolygon(face);
            }
            g2.setColor(new Color(20, 20, 20, Math.min(alpha, 80)));
            g2.drawPolygon(face);
        }
    }

    private Polygon polygon(Point2D.Double... pts) {
        Polygon p = new Polygon();
        for (Point2D.Double pt : pts)
            p.addPoint((int) Math.round(pt.x), (int) Math.round(pt.y));
        return p;
    }

    private static final class Cell3 {
        final int x, y, z;
        Cell3(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Cell3 c)) return false;
            return x == c.x && y == c.y && z == c.z;
        }
        @Override public int hashCode() { return Objects.hash(x, y, z); }
    }

    private static final class Point3D {
        final int x, y;
        Point3D(int x, int y) { this.x = x; this.y = y; }
    }
}