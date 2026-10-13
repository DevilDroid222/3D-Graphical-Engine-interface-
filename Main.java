import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
            JFrame frame = new JFrame("Grid3D - Budowanie mostów");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            Grid3D grid = new Grid3D();
            frame.add(grid, BorderLayout.CENTER);

            // ========== DOLNY PASEK STEROWANIA (oryginalne menu) ==========
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            controls.setBackground(new Color(240, 242, 245));
            controls.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 222, 229)));

            // --- Kolor klocka ---
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

            // --- Tryb budowy ---
            JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            modePanel.setOpaque(false);
            modePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Tryb budowy"));

            JComboBox<Grid3D.BuildMode> modes = new JComboBox<>(Grid3D.BuildMode.values());
            modes.setPreferredSize(new Dimension(130, 28));
            modes.setFont(new Font("SansSerif", Font.PLAIN, 12));
            modes.addActionListener(e ->
                    grid.setBuildMode((Grid3D.BuildMode) modes.getSelectedItem()));
            modePanel.add(modes);

            // --- Wysokość (ściany / kształtów) ---
            JPanel wallHeightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            wallHeightPanel.setOpaque(false);
            wallHeightPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Wysokość"));

            JSpinner wallHeightSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 40, 1));
            wallHeightSpinner.setPreferredSize(new Dimension(50, 28));
            wallHeightSpinner.addChangeListener(e ->
                    grid.setWallHeight((Integer) wallHeightSpinner.getValue()));
            wallHeightPanel.add(wallHeightSpinner);

            // --- Promień (dla nowych kształtów) ---
            JPanel radiusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            radiusPanel.setOpaque(false);
            radiusPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Promień"));

            JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
            radiusSpinner.setPreferredSize(new Dimension(50, 28));
            radiusSpinner.addChangeListener(e ->
                    grid.setShapeRadius((Integer) radiusSpinner.getValue()));
            radiusPanel.add(radiusSpinner);

            // --- Akcje ---
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            actionPanel.setOpaque(false);
            actionPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 215)), "Akcje"));

            JButton undo = createStyledButton("Cofnij (Ctrl+Z)", new Color(108, 117, 125));
            undo.addActionListener(e -> grid.undo());

            JButton clear = createStyledButton("Wyczyść", new Color(220, 53, 69));
            clear.addActionListener(e -> grid.clearBlocks());

            actionPanel.add(undo);
            actionPanel.add(clear);

            // Dodanie wszystkich sekcji do paska
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
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        button.setToolTipText(text);
        button.addActionListener(e -> grid.setSelectedColorAndTexture(color, null));
        panel.add(button);
    }

    private static void addCustomColorButton(JPanel panel, Grid3D grid) {
        JButton button = new JButton("+");
        button.setPreferredSize(new Dimension(32, 32));
        button.setBackground(new Color(230, 235, 240));
        button.setForeground(new Color(50, 50, 50));
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Własny kolor (Paleta RGB)");
        button.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(panel, "Wybierz unikalny kolor", grid.getSelectedColor());
            if (chosen != null) {
                grid.setSelectedColorAndTexture(chosen, null);
            }
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
        button.setToolTipText("Wgraj teksturę (obrazek) dla klocka");
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Wybierz obrazek tekstury");
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        grid.setSelectedColorAndTexture(Color.WHITE, img);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Nie udało się wczytać wybranego obrazka.", "Błąd", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Wystąpił błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
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

                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics metrics = g2.getFontMetrics(getFont());
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 32));

        return button;
    }
}

class Grid3D extends JPanel {

    private static final int SIZE = 40;
    private static final int MAX_HEIGHT = 200;
    private static final double CELL_SIZE = 24.0;

    enum BuildMode {
        SZEREG("Szereg"),
        PLATFORMA("Platforma"),
        SCIANA("Ściana"),
        SCHODY("Schody"),
        STOZEK("Stożek"),
        PIRAMIDA("Piramida"),
        CYLINDER("Cylinder"),
        KULA("Kula"),
        TRAPEZ("Trapez");

        private final String label;

        BuildMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private double zoom = 1.0;
    private double rotationAngle = 0.0;
    private double panX = 0.0;
    private double panY = 0.0;

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

    private boolean rotating;
    private boolean panning;
    private boolean building;
    private boolean erasing;

    private Cell3 anchor;
    private Cell3 lastPathCell;
    private Cell3 previewTarget;
    private List<Cell3> preview = new ArrayList<>();
    private Cell3 chainEnd;

    public Grid3D() {
        setBackground(new Color(248, 249, 250));
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastMousePosition = e.getPoint();

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

                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }

                if (e.isShiftDown()) {
                    panning = true;
                    return;
                }

                // Tryby kształtów 3D – kliknięcie od razu buduje cały kształt
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
                    anchor = start;
                    chainEnd = start;
                    lastPathCell = start;
                    previewTarget = start;
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
                    anchor = start;
                    chainEnd = start;
                    lastPathCell = start;
                    previewTarget = start;
                }

                updatePreview(e.getPoint());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePosition == null) {
                    lastMousePosition = e.getPoint();
                    return;
                }

                int dx = e.getX() - lastMousePosition.x;
                int dy = e.getY() - lastMousePosition.y;

                if (rotating) {
                    rotationAngle += dx * 0.01;
                } else if (panning) {
                    panX += dx;
                    panY += dy;
                } else if (building && !isShapeMode()) {
                    updatePreview(e.getPoint());
                } else if (erasing) {
                    removeAtScreen(e.getPoint());
                }

                lastMousePosition = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (building && !isShapeMode()) {
                    updatePreview(e.getPoint());
                    commitPreview();
                }

                rotating = false;
                panning = false;
                building = false;
                erasing = false;

                anchor = null;
                chainEnd = null;
                lastPathCell = null;
                previewTarget = null;
                preview.clear();
                lastMousePosition = null;

                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldZoom = zoom;

                if (e.getWheelRotation() < 0) {
                    zoom *= 1.1;
                } else {
                    zoom /= 1.1;
                }

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

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");

        getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
    }

    private boolean isShapeMode() {
        return buildMode == BuildMode.STOZEK
                || buildMode == BuildMode.PIRAMIDA
                || buildMode == BuildMode.CYLINDER
                || buildMode == BuildMode.KULA
                || buildMode == BuildMode.TRAPEZ;
    }

    // ============================================================
    //  GENEROWANIE KSZTAŁTÓW 3D
    // ============================================================

    private void placeShapeAt(Point mouse) {
        Cell3 hit = findVisibleBlock(mouse);
        int baseX, baseY, baseZ;

        if (hit != null) {
            baseX = hit.x;
            baseY = hit.y;
            baseZ = topHeight(hit.x, hit.y);
        } else {
            Point3D ground = screenToGridAtZ(mouse, 0);
            if (!inBounds(ground.x, ground.y)) return;
            baseX = ground.x;
            baseY = ground.y;
            baseZ = topHeight(ground.x, ground.y);
        }

        if (baseZ >= MAX_HEIGHT) return;

        List<Cell3> shapeBlocks = switch (buildMode) {
            case STOZEK   -> generateCone(baseX, baseY, baseZ, shapeRadius, wallHeight);
            case PIRAMIDA -> generatePyramid(baseX, baseY, baseZ, shapeRadius, wallHeight);
            case CYLINDER -> generateCylinder(baseX, baseY, baseZ, shapeRadius, wallHeight);
            case KULA     -> generateSphere(baseX, baseY, baseZ, shapeRadius);
            case TRAPEZ   -> generateTrapezoid(baseX, baseY, baseZ, shapeRadius, wallHeight);
            default       -> List.of();
        };

        for (Cell3 c : shapeBlocks) {
            if (inBounds(c) && !blocks.containsKey(c)) {
                blocks.put(c, new BlockData(selectedColor, selectedTexture));
            }
        }
    }

    private List<Cell3> generateCone(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> result = new ArrayList<>();
        for (int h = 0; h < height; h++) {
            double t = (height <= 1) ? 0.0 : (double) h / (height - 1);
            int r = Math.max(0, (int) Math.round(radius * (1.0 - t)));
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (dx * dx + dy * dy <= r * r + r * 0.3) {
                        result.add(new Cell3(cx + dx, cy + dy, cz + h));
                    }
                }
            }
        }
        return result;
    }

    private List<Cell3> generatePyramid(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> result = new ArrayList<>();
        for (int h = 0; h < height; h++) {
            double t = (height <= 1) ? 0.0 : (double) h / (height - 1);
            int r = Math.max(0, (int) Math.round(radius * (1.0 - t)));
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    result.add(new Cell3(cx + dx, cy + dy, cz + h));
                }
            }
        }
        return result;
    }

    private List<Cell3> generateCylinder(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> result = new ArrayList<>();
        for (int h = 0; h < height; h++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx * dx + dy * dy <= radius * radius + radius * 0.3) {
                        result.add(new Cell3(cx + dx, cy + dy, cz + h));
                    }
                }
            }
        }
        return result;
    }

    private List<Cell3> generateSphere(int cx, int cy, int cz, int radius) {
        List<Cell3> result = new ArrayList<>();
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= r2 + radius * 0.4) {
                        result.add(new Cell3(cx + dx, cy + dy, cz + dz + radius));
                    }
                }
            }
        }
        return result;
    }

    private List<Cell3> generateTrapezoid(int cx, int cy, int cz, int radius, int height) {
        List<Cell3> result = new ArrayList<>();
        int topRadius = Math.max(1, radius / 2);

        for (int h = 0; h < height; h++) {
            double t = (height <= 1) ? 0.0 : (double) h / (height - 1);
            int currentR = (int) Math.round(radius + (topRadius - radius) * t);

            for (int dx = -currentR; dx <= currentR; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    double yLimit = radius * (1.0 - t * 0.4);
                    if (Math.abs(dy) <= yLimit) {
                        result.add(new Cell3(cx + dx, cy + dy, cz + h));
                    }
                }
            }
        }
        return result;
    }

    // ============================================================

    public void setSelectedColorAndTexture(Color color, BufferedImage texture) {
        this.selectedColor = color;
        this.selectedTexture = texture;
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public void setBuildMode(BuildMode mode) {
        if (mode != null) {
            buildMode = mode;
        }
    }

    public void setWallHeight(int height) {
        this.wallHeight = height;
    }

    public void setShapeRadius(int radius) {
        this.shapeRadius = radius;
    }

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
        while (undoStack.size() > 100) {
            undoStack.removeLast();
        }
    }

    private void cancelBuild() {
        building = false;
        anchor = null;
        chainEnd = null;
        lastPathCell = null;
        previewTarget = null;
        preview.clear();
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    private boolean inBounds(Cell3 c) {
        return inBounds(c.x, c.y) && c.z >= 0 && c.z < MAX_HEIGHT;
    }

    private int topHeight(int x, int y) {
        int highest = -1;
        for (Cell3 c : blocks.keySet()) {
            if (c.x == x && c.y == y) {
                highest = Math.max(highest, c.z);
            }
        }
        return highest + 1;
    }

    private boolean areNeighbors(Cell3 a, Cell3 b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        int dz = Math.abs(a.z - b.z);

        if (buildMode == BuildMode.SCHODY) {
            return dx <= 1 && dy <= 1 && dz <= 1 && (dx + dy + dz > 0);
        }
        return (dx + dy + dz) == 1;
    }

    private boolean hasNeighbor(Cell3 c) {
        if (buildMode == BuildMode.SCHODY) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (blocks.containsKey(new Cell3(c.x + dx, c.y + dy, c.z + dz))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            return blocks.containsKey(new Cell3(c.x + 1, c.y, c.z))
                    || blocks.containsKey(new Cell3(c.x - 1, c.y, c.z))
                    || blocks.containsKey(new Cell3(c.x, c.y + 1, c.z))
                    || blocks.containsKey(new Cell3(c.x, c.y - 1, c.z))
                    || blocks.containsKey(new Cell3(c.x, c.y, c.z + 1))
                    || blocks.containsKey(new Cell3(c.x, c.y, c.z - 1));
        }
    }

    private Point3D screenToGridAtZ(Point mouse, int z) {
        double unit = CELL_SIZE * zoom;

        double sx = (mouse.x - getWidth() / 2.0 - panX) / unit;
        double sy = (mouse.y - getHeight() / 2.0 - panY) / unit + z;

        double rx = sy + sx * 0.5;
        double ry = sy - sx * 0.5;

        double cos = Math.cos(rotationAngle);
        double sin = Math.sin(rotationAngle);

        double x = rx * cos + ry * sin;
        double y = -rx * sin + ry * cos;

        return new Point3D((int) Math.floor(x), (int) Math.floor(y));
    }

    private void updatePreview(Point mouse) {
        if (anchor == null) {
            preview.clear();
            return;
        }

        Point3D p = screenToGridAtZ(mouse, anchor.z);

        if (!inBounds(p.x, p.y)) {
            preview.clear();
            return;
        }

        previewTarget = new Cell3(p.x, p.y, anchor.z);
        preview = createBuildPath(anchor, previewTarget);
    }

    private List<Cell3> createBuildPath(Cell3 start, Cell3 end) {
        List<Cell3> result = new ArrayList<>();

        int x = start.x;
        int y = start.y;

        while (x != end.x) {
            x += Integer.compare(end.x, x);
            result.add(new Cell3(x, y, start.z));
        }

        while (y != end.y) {
            y += Integer.compare(end.y, y);
            result.add(new Cell3(x, y, start.z));
        }

        if (buildMode == BuildMode.SCHODY) {
            for (int i = 0; i < result.size(); i++) {
                Cell3 c = result.get(i);
                int z = Math.min(MAX_HEIGHT - 1, start.z + i + 1);
                result.set(i, new Cell3(c.x, c.y, z));
            }
        }

        return result;
    }

    private void commitPreview() {
        if (anchor == null || preview.isEmpty()) {
            return;
        }

        Cell3 previous = chainEnd;

        for (Cell3 candidate : preview) {
            List<Cell3> toPlace = new ArrayList<>();

            if (buildMode == BuildMode.PLATFORMA) {
                int minX = Math.min(anchor.x, candidate.x);
                int maxX = Math.max(anchor.x, candidate.x);
                int minY = Math.min(anchor.y, candidate.y);
                int maxY = Math.max(anchor.y, candidate.y);

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        toPlace.add(new Cell3(x, y, anchor.z));
                    }
                }
            } else if (buildMode == BuildMode.SCIANA) {
                for (int z = anchor.z; z < anchor.z + wallHeight && z < MAX_HEIGHT; z++) {
                    toPlace.add(new Cell3(candidate.x, candidate.y, z));
                }
            } else {
                toPlace.add(candidate);
            }

            for (Cell3 c : toPlace) {
                if (!inBounds(c) || blocks.containsKey(c)) {
                    continue;
                }

                boolean validPlacement = (c.z == 0) || (previous != null && areNeighbors(previous, c)) || hasNeighbor(c);

                if (validPlacement) {
                    blocks.put(c, new BlockData(selectedColor, selectedTexture));
                    previous = c;
                }
            }
        }

        repaint();
    }

    private void removeAtScreen(Point mouse) {
        Cell3 hit = findVisibleBlock(mouse);

        if (hit != null) {
            blocks.remove(hit);
            repaint();
            return;
        }

        Point3D p = screenToGridAtZ(mouse, 0);

        if (!inBounds(p.x, p.y)) {
            return;
        }

        int z = topHeight(p.x, p.y) - 1;

        if (z >= 0) {
            blocks.remove(new Cell3(p.x, p.y, z));
            repaint();
        }
    }

    private Cell3 findVisibleBlock(Point mouse) {
        Cell3 result = null;
        double bestDepth = Double.NEGATIVE_INFINITY;

        for (Cell3 c : blocks.keySet()) {
            for (Polygon face : getCubeFaces(c.x, c.y, c.z)) {
                if (face.contains(mouse)) {
                    double d = depth(c.x + 0.5, c.y + 0.5, c.z + 0.5);

                    if (result == null || d > bestDepth) {
                        result = c;
                        bestDepth = d;
                    }
                }
            }
        }

        return result;
    }

    private Point2D.Double project(double x, double y, double z) {
        double cos = Math.cos(rotationAngle);
        double sin = Math.sin(rotationAngle);

        double rx = x * cos - y * sin;
        double ry = x * sin + y * cos;

        double unit = CELL_SIZE * zoom;

        double sx = (rx - ry) * unit;
        double sy = (rx + ry) * unit * 0.5 - z * unit;

        return new Point2D.Double(
                getWidth() / 2.0 + sx + panX,
                getHeight() / 2.0 + sy + panY);
    }

    private double depth(double x, double y, double z) {
        double cos = Math.cos(rotationAngle);
        double sin = Math.sin(rotationAngle);

        double rx = x * cos - y * sin;
        double ry = x * sin + y * cos;

        return rx + ry + z;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawBlocks(g2);
        drawPreview(g2);

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2) {
        Stroke old = g2.getStroke();

        g2.setColor(new Color(200, 205, 210));
        g2.setStroke(new BasicStroke(
                1f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f,
                new float[]{4f, 4f}, 0f));

        for (int i = 0; i <= SIZE; i++) {
            Point2D.Double a = project(i, 0, 0);
            Point2D.Double b = project(i, SIZE, 0);

            g2.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);

            Point2D.Double c = project(0, i, 0);
            Point2D.Double d = project(SIZE, i, 0);

            g2.drawLine((int) c.x, (int) c.y, (int) d.x, (int) d.y);
        }

        g2.setStroke(old);
    }

    private void drawBlocks(Graphics2D g2) {
        List<Map.Entry<Cell3, BlockData>> entries = new ArrayList<>(blocks.entrySet());

        entries.sort(Comparator.comparingDouble(e -> {
            Cell3 c = e.getKey();
            return depth(c.x + 0.5, c.y + 0.5, c.z + 0.5);
        }));

        for (Map.Entry<Cell3, BlockData> e : entries) {
            Cell3 c = e.getKey();
            BlockData data = e.getValue();
            drawCube(g2, c.x, c.y, c.z, data.color, data.texture, 255);
        }
    }

    private void drawPreview(Graphics2D g2) {
        if (!building || preview.isEmpty()) {
            return;
        }

        for (Cell3 c : preview) {
            if (!inBounds(c)) continue;
            if (blocks.containsKey(c)) continue;
            drawCube(g2, c.x, c.y, c.z, selectedColor, selectedTexture, 100);
        }
    }

    private Polygon[] getCubeFaces(int x, int y, int z) {
        Point2D.Double p1 = project(x, y, z + 1);
        Point2D.Double p2 = project(x + 1, y, z + 1);
        Point2D.Double p3 = project(x + 1, y + 1, z + 1);
        Point2D.Double p4 = project(x, y + 1, z + 1);

        Point2D.Double b1 = project(x, y, z);
        Point2D.Double b2 = project(x + 1, y, z);
        Point2D.Double b3 = project(x + 1, y + 1, z);
        Point2D.Double b4 = project(x, y + 1, z);

        return new Polygon[]{
                polygon(p1, p2, p3, p4),
                polygon(b1, b2, b3, b4),
                polygon(p1, p2, b2, b1),
                polygon(p4, p3, b3, b4),
                polygon(p1, p4, b4, b1),
                polygon(p2, p3, b3, b2)
        };
    }

    private void drawCube(Graphics2D g2, int x, int y, int z, Color color, BufferedImage texture, int alpha) {
        Polygon[] faces = getCubeFaces(x, y, z);

        Color baseC = color;
        Color[] colors = {
                baseC.brighter(),
                baseC.darker().darker(),
                baseC,
                baseC.darker(),
                baseC.brighter().darker(),
                baseC.darker()
        };

        double[] depths = {
                depth(x + 0.5, y + 0.5, z + 1),
                depth(x + 0.5, y + 0.5, z),
                depth(x + 0.5, y, z + 0.5),
                depth(x + 0.5, y + 1, z + 0.5),
                depth(x, y + 0.5, z + 0.5),
                depth(x + 1, y + 0.5, z + 0.5)
        };

        Integer[] order = {0, 1, 2, 3, 4, 5};
        Arrays.sort(order, Comparator.comparingDouble(i -> depths[i]));

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1f));

        for (int i : order) {
            Polygon face = faces[i];
            Color c = colors[i];

            if (texture != null) {
                Rectangle bounds = face.getBounds();
                if (bounds.width > 0 && bounds.height > 0) {
                    try {
                        TexturePaint tp = new TexturePaint(texture, bounds);
                        g2.setPaint(tp);
                        g2.fillPolygon(face);
                    } catch (Exception e) {
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

        g2.setStroke(oldStroke);
    }

    private Polygon polygon(Point2D.Double... points) {
        Polygon p = new Polygon();
        for (Point2D.Double point : points) {
            p.addPoint((int) Math.round(point.x), (int) Math.round(point.y));
        }
        return p;
    }

    private static final class Cell3 {
        final int x, y, z;

        Cell3(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell3)) return false;
            Cell3 c = (Cell3) o;
            return x == c.x && y == c.y && z == c.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private static final class Point3D {
        final int x, y;

        Point3D(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}