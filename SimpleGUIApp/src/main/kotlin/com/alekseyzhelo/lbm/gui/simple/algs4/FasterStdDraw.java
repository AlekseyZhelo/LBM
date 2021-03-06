package com.alekseyzhelo.lbm.gui.simple.algs4;

/******************************************************************************
 * Compilation:  javac StdDraw.java
 * Execution:    java StdDraw
 * Dependencies: none
 * <p>
 * Standard drawing library. This class provides a basic capability for
 * creating drawings with your programs. It uses a simple graphics model that
 * allows you to create drawings consisting of points, lines, and curves
 * in a window on your computer and to save the drawings to a file.
 * <p>
 * Todo
 * ----
 * -  Add support for gradient fill, etc.
 * -  Fix setCanvasSize() so that it can only be called once.
 * -  On some systems, drawing a line (or other shape) that extends way
 * beyond canvas (e.g., to infinity) dimensions does not get drawn.
 * <p>
 * Remarks
 * -------
 * -  don't use AffineTransform for rescaling since it inverts
 * images and strings
 * -  careful using setFont in inner loop within an animation -
 * it can cause flicker
 ******************************************************************************/

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * <i>Standard draw</i>. This class provides a basic capability for
 * creating drawings with your programs. It uses a simple graphics model that
 * allows you to create drawings consisting of points, lines, and curves
 * in a window on your computer and to save the drawings to a file.
 * <p>
 * For additional documentation, see <a href="http://introcs.cs.princeton.edu/15inout">Section 1.5</a> of
 * <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public final class FasterStdDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    private static double precalcXScale = 0;
    private static double precalcYScale = 0;
    private static double precalcAbsXScale = 0;
    private static double precalcAbsYScale = 0;
    private static double precalcFactorX = 0;
    private static double precalcFactorY = 0;

    private static Rectangle2D.Double drawRect = new Rectangle2D.Double(0, 0, 0, 0);

    /**
     * The color black.
     */
    public static final Color BLACK = Color.BLACK;

    /**
     * The color blue.
     */
    public static final Color BLUE = Color.BLUE;

    /**
     * The color cyan.
     */
    public static final Color CYAN = Color.CYAN;

    /**
     * The color dark gray.
     */
    public static final Color DARK_GRAY = Color.DARK_GRAY;

    /**
     * The color gray.
     */
    public static final Color GRAY = Color.GRAY;

    /**
     * The color green.
     */
    public static final Color GREEN = Color.GREEN;

    /**
     * The color light gray.
     */
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;

    /**
     * The color magenta.
     */
    public static final Color MAGENTA = Color.MAGENTA;

    /**
     * The color orange.
     */
    public static final Color ORANGE = Color.ORANGE;

    /**
     * The color pink.
     */
    public static final Color PINK = Color.PINK;

    /**
     * The color red.
     */
    public static final Color RED = Color.RED;

    /**
     * The color white.
     */
    public static final Color WHITE = Color.WHITE;

    /**
     * The color yellow.
     */
    public static final Color YELLOW = Color.YELLOW;

    /**
     * Shade of blue used in Introduction to Programming in Java.
     * It is Pantone 300U. The RGB values are approximately (9, 90, 166).
     */
    public static final Color BOOK_BLUE = new Color(9, 90, 166);

    /**
     * Shade of light blue used in Introduction to Programming in Java.
     * The RGB values are approximately (103, 198, 243).
     */
    public static final Color BOOK_LIGHT_BLUE = new Color(103, 198, 243);

    /**
     * Shade of red used in Algorithms 4th edition.
     * It is Pantone 1805U. The RGB values are approximately (150, 35, 31).
     */
    public static final Color BOOK_RED = new Color(150, 35, 31);

    // default colors
    private static final Color DEFAULT_PEN_COLOR = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // current pen color
    private static Color penColor;

    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 512;
    private static int width = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;

    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private static double penRadius;

    // show we draw immediately or wait until next show?
    private static boolean defer = false;

    // boundary of drawing canvas, 0% border
    // private static final double BORDER = 0.05;
    private static final double BORDER = 0.00;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    private static double xmin, ymin, xmax, ymax;

    // for synchronization
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();

    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // current font
    private static Font font;

    // double buffered graphics
    private static BufferedImage offscreenImage, onscreenImage;
    private static Graphics2D offscreen, onscreen;

    // singleton for callbacks: avoids generation of extra .class files
    private static FasterStdDraw std = new FasterStdDraw();

    // the frame for drawing to the screen
    private static JFrame frame;

    // mouse state
    private static boolean mousePressed = false;
    private static double mouseX = 0;
    private static double mouseY = 0;

    // queue of typed key characters
    private static LinkedList<Character> keysTyped = new LinkedList<Character>();

    // set of key codes currently pressed down
    private static TreeSet<Integer> keysDown = new TreeSet<Integer>();

    // time in milliseconds (from currentTimeMillis()) when we can draw again
    // used to control the frame rate
    private static long nextDraw = -1;

    // singleton pattern: client can't instantiate
    private FasterStdDraw() {
    }


    // static initializer
    static {
        init();
    }

    /**
     * Sets the window size to the default size 512-by-512 pixels.
     * This method must be called before any other commands.
     */
    public static void setCanvasSize() {
        setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Sets the window size to <tt>w</tt>-by-<tt>h</tt> pixels.
     * This method must be called before any other commands.
     *
     * @param w the width as a number of pixels
     * @param h the height as a number of pixels
     * @throws IllegalArgumentException if the width or height is 0 or negative
     */
    public static void setCanvasSize(int w, int h) {
        if (w < 1 || h < 1) throw new IllegalArgumentException("width and height must be positive");
        width = w;
        height = h;
        precalcAbsXScale = Math.abs(xmax - xmin);
        precalcAbsYScale = Math.abs(ymax - ymin);
        if (precalcAbsXScale != 0 && precalcAbsYScale != 0) {
            precalcFactorX = width / precalcAbsXScale;
            precalcFactorY = width / precalcAbsYScale;
        }
        init();
    }

    // init
    private static void init() {
        if (frame != null) frame.setVisible(false);
        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        onscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        offscreen = offscreenImage.createGraphics();
        onscreen = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
//        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        offscreen.addRenderingHints(hints);

        // frame stuff
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);

        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);

        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Standard Draw");
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    // create the menu bar (changed to private)
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem(" Save...   ");
        menuItem1.addActionListener(std);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);
        return menuBar;
    }


    /***************************************************************************
     *  User and screen coordinate systems.
     ***************************************************************************/

    /**
     * Sets the x-scale to be the default (between 0.0 and 1.0).
     */
    public static void setXscale() {
        setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
    }

    /**
     * Sets the y-scale to be the default (between 0.0 and 1.0).
     */
    public static void setYscale() {
        setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
    }

    /**
     * Sets the x-scale.
     *
     * @param min the minimum value of the x-scale
     * @param max the maximum value of the x-scale
     */
    public static void setXscale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
            precalcAbsXScale = Math.abs(xmax - xmin);
            precalcXScale = xmax - xmin;
            precalcFactorX = width / precalcAbsXScale;
        }
    }

    /**
     * Sets the y-scale.
     *
     * @param min the minimum value of the y-scale
     * @param max the maximum value of the y-scale
     */
    public static void setYscale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
            precalcAbsYScale = Math.abs(ymax - ymin);
            precalcYScale = ymax - ymin;
            precalcFactorY = width / precalcAbsYScale;
        }
    }

    /**
     * Sets both the x-scale and y-scale.
     *
     * @param min the minimum value of the x- and y-scales
     * @param max the maximum value of the x- and y-scales
     */
    public static void setScale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
        }
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    private static double scaleX(double x) {
        return width * (x - xmin) / (xmax - xmin);
    }

    private static double scaleY(double y) {
        return height * (ymax - y) / (ymax - ymin);
    }

    private static double factorX(double w) {
        return w * width / Math.abs(xmax - xmin);
    }

    private static double factorY(double h) {
        return h * height / Math.abs(ymax - ymin);
    }

    private static double userX(double x) {
        return xmin + x * (xmax - xmin) / width;
    }

    private static double userY(double y) {
        return ymax - y * (ymax - ymin) / height;
    }


    /**
     * Clears the screen to the default color (white).
     */
    public static void clear() {
        clear(DEFAULT_CLEAR_COLOR);
    }

    /**
     * Clears the screen to the given color.
     *
     * @param color the color to make the background
     */
    public static void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        draw();
    }

    /**
     * Gets the current pen radius.
     *
     * @return the current value of the pen radius
     */
    public static double getPenRadius() {
        return penRadius;
    }

    /**
     * Sets the pen size to the default (.002).
     */
    public static void setPenRadius() {
        setPenRadius(DEFAULT_PEN_RADIUS);
    }

    /**
     * Sets the radius of the pen to the given size.
     *
     * @param r the radius of the pen
     * @throws IllegalArgumentException if <tt>r</tt> is negative
     */
    public static void setPenRadius(double r) {
        if (r < 0) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = r;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Gets the current pen color.
     *
     * @return the current pen color
     */
    public static Color getPenColor() {
        return penColor;
    }

    /**
     * Set the pen color to the default color (black).
     */
    public static void setPenColor() {
        setPenColor(DEFAULT_PEN_COLOR);
    }

    /**
     * Sets the pen color to the given color.
     * <p>
     * The predefined pen colors are
     * <tt>StdDraw.BLACK</tt>, <tt>StdDraw.BLUE</tt>, <tt>StdDraw.CYAN</tt>,
     * <tt>StdDraw.DARK_GRAY</tt>, <tt>StdDraw.GRAY</tt>, <tt>StdDraw.GREEN</tt>,
     * <tt>StdDraw.LIGHT_GRAY</tt>, <tt>StdDraw.MAGENTA</tt>, <tt>StdDraw.ORANGE</tt>,
     * <tt>StdDraw.PINK</tt>, <tt>StdDraw.RED</tt>, <tt>StdDraw.WHITE</tt>, and
     * <tt>StdDraw.YELLOW</tt>.
     *
     * @param color the Color to make the pen
     */
    public static void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Sets the pen color to the given RGB color.
     *
     * @param red   the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue  the amount of blue (between 0 and 255)
     * @throws IllegalArgumentException if the amount of red, green, or blue are outside prescribed range
     */
    public static void setPenColor(int red, int green, int blue) {
        if (red < 0 || red >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue < 0 || blue >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        setPenColor(new Color(red, green, blue));
    }

    /**
     * Gets the current font.
     *
     * @return the current font
     */
    public static Font getFont() {
        return font;
    }

    /**
     * Sets the font to the default font (sans serif, 16 point).
     */
    public static void setFont() {
        setFont(DEFAULT_FONT);
    }

    /**
     * Sets the font to the given value.
     *
     * @param f the font
     */
    public static void setFont(Font f) {
        font = f;
    }


    /***************************************************************************
     *  Drawing geometric shapes.
     ***************************************************************************/

    /**
     * Draw a line from (x0, y0) to (x1, y1).
     *
     * @param x0 the x-coordinate of the starting point
     * @param y0 the y-coordinate of the starting point
     * @param x1 the x-coordinate of the destination point
     * @param y1 the y-coordinate of the destination point
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }

    /**
     * Draw one pixel at (x, y).
     *
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Draw a point at (x, y).
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     */
    public static void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);

        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (scaledPenRadius <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - scaledPenRadius / 2, ys - scaledPenRadius / 2,
                scaledPenRadius, scaledPenRadius));
        draw();
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y).
     *
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws IllegalArgumentException if r is negative
     */
    public static void filledSquare(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * r);
        double hs = factorY(2 * r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            drawRect.setRect(xs - ws / 2, ys - hs / 2, ws, hs);
            offscreen.fill(drawRect);
        }
        draw();
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y).
     *
     * @param x       the x-coordinate of the center of the square
     * @param y       the y-coordinate of the center of the square
     * @param doubleR double radius is the length of any side of the square
     * @throws IllegalArgumentException if r is negative
     */
    public static void deferredFilledSquare(double x, double y, double doubleR) {
        double xs = width * (x - xmin) / precalcXScale;
        double ys = height * (ymax - y) / precalcYScale;
        double ws = doubleR * precalcFactorX;
        double hs = doubleR * precalcFactorY;
        drawRect.setRect(xs - ws / 2, ys - hs / 2, ws, hs);
        offscreen.fill(drawRect);
    }

    public static void deferredFilledSquareTest(double x, double y, double doubleR) {
        drawRect.setRect(
                width * (x - xmin) / precalcXScale,
                height * (ymax - y) / precalcYScale,
                doubleR * precalcFactorX,
                doubleR * precalcFactorY
        );
        offscreen.fill(drawRect);
    }

    /**
     * Thanks to http://stackoverflow.com/a/27461352
     * Draw an arrow line between two points
     *
     * @param x1 x-position of first point
     * @param y1 y-position of first point
     * @param x2 x-position of second point
     * @param y2 y-position of second point
     * @param d  the width of the arrow
     * @param h  the height of the arrow
     */
    public static void drawArrowLine(int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        offscreen.drawLine(x1, y1, x2, y2);
        offscreen.fillPolygon(xpoints, ypoints, 3);
    }

    /**
     * Thanks to http://stackoverflow.com/a/27461352
     * Draw an arrow line between two points
     *
     * @param x1 x-position of first point
     * @param y1 y-position of first point
     * @param x2 x-position of second point
     * @param y2 y-position of second point
     * @param d  the width of the arrow
     * @param h  the height of the arrow
     */
    public static void drawArrowLineTest(double x1, double y1, double x2, double y2, double d, double h) {
        x1 = width * (x1 - xmin) / precalcXScale;
        x2 = width * (x2 - xmin) / precalcXScale;
        y1 = height * (ymax - y1) / precalcYScale;
        y2 = height * (ymax - y2) / precalcYScale;
        d = d * precalcFactorX;
        h = h * precalcFactorY;

        int dx = (int) (x2 - x1), dy = (int) (y2 - y1);
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {(int) x2, (int) xm, (int) xn};
        int[] ypoints = {(int) y2, (int) ym, (int) yn};

        offscreen.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        offscreen.fillPolygon(xpoints, ypoints, 3);
    }

    /***************************************************************************
     *  Drawing text.
     ***************************************************************************/

    /**
     * Write the given text string in the current font, centered on (x, y).
     *
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     */
    public static void text(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws / 2.0), (float) (ys + hs));
        draw();
    }

    /**
     * Write the given text string in the current font, centered on (x, y) and
     * rotated by the specified number of degrees.
     *
     * @param x       the center x-coordinate of the text
     * @param y       the center y-coordinate of the text
     * @param s       the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public static void text(double x, double y, String s, double degrees) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, s);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);
    }


    /**
     * Write the given text string in the current font, left-aligned at (x, y).
     *
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    public static void textLeft(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) xs, (float) (ys + hs));
        draw();
    }

    /**
     * Write the given text string in the current font, right-aligned at (x, y).
     *
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    public static void textRight(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));
        draw();
    }


    /**
     * Display on screen, pause for t milliseconds, and turn on
     * <em>animation mode</em>: subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will not be displayed on screen until the next call to <tt>show()</tt>.
     * This is useful for producing animations (clear the screen, draw a bunch of shapes,
     * display on screen for a fixed amount of time, and repeat). It also speeds up
     * drawing a huge number of shapes (call <tt>show(0)</tt> to defer drawing
     * on screen, draw the shapes, and call <tt>show(0)</tt> to display them all
     * on screen at once).
     *
     * @param t number of milliseconds
     */
    public static void show(int t) {
        // sleep until the next time we're allowed to draw
        long millis = System.currentTimeMillis();
        if (millis < nextDraw) {
            try {
                Thread.sleep(nextDraw - millis);
            } catch (InterruptedException e) {
                System.out.println("Error sleeping");
            }
            millis = nextDraw;
        }

        defer = false;
        draw();
        defer = true;

        // when are we allowed to draw again
        nextDraw = millis + t;
    }

    /**
     * Display on-screen and turn off animation mode:
     * subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will be displayed on screen when called. This is the default.
     */
    public static void show() {
        defer = false;
        nextDraw = -1;
        draw();
    }

    // draw onscreen if defer is false
    private static void draw() {
        if (defer) return;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }


    /***************************************************************************
     *  Save drawing to a file.
     ***************************************************************************/

    /**
     * Save onscreen image to file - suffix must be png, jpg, or gif.
     *
     * @param filename the name of the file with one of the required suffixes
     */
    public static void save(String filename) {
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        // png files
        if (suffix.toLowerCase().equals("png")) {
            try {
                ImageIO.write(onscreenImage, suffix, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // need to change from ARGB to RGB for jpeg
        // reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
        else if (suffix.toLowerCase().equals("jpg")) {
            WritableRaster raster = onscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[]{0, 1, 2});
            DirectColorModel cm = (DirectColorModel) onscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
                    cm.getRedMask(),
                    cm.getGreenMask(),
                    cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false, null);
            try {
                ImageIO.write(rgbBuffer, suffix, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }


    /**
     * This method cannot be called directly.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(FasterStdDraw.frame, "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            FasterStdDraw.save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }


    /***************************************************************************
     *  Mouse interactions.
     ***************************************************************************/

    /**
     * Returns true if the mouse is being pressed.
     *
     * @return <tt>true</tt> if the mouse is being pressed; <tt>false</tt> otherwise
     */
    public static boolean mousePressed() {
        synchronized (mouseLock) {
            return mousePressed;
        }
    }

    /**
     * Returns the x-coordinate of the mouse.
     *
     * @return the x-coordinate of the mouse
     */
    public static double mouseX() {
        synchronized (mouseLock) {
            return mouseX;
        }
    }

    /**
     * Returns the y-coordinate of the mouse.
     *
     * @return y-coordinate of the mouse
     */
    public static double mouseY() {
        synchronized (mouseLock) {
            return mouseY;
        }
    }


    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = FasterStdDraw.userX(e.getX());
            mouseY = FasterStdDraw.userY(e.getY());
            mousePressed = true;
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        synchronized (mouseLock) {
            mousePressed = false;
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = FasterStdDraw.userX(e.getX());
            mouseY = FasterStdDraw.userY(e.getY());
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = FasterStdDraw.userX(e.getX());
            mouseY = FasterStdDraw.userY(e.getY());
        }
    }


    /***************************************************************************
     *  Keyboard interactions.
     ***************************************************************************/

    /**
     * Returns true if the user has typed a key.
     *
     * @return <tt>true</tt> if the user has typed a key; <tt>false</tt> otherwise
     */
    public static boolean hasNextKeyTyped() {
        synchronized (keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    /**
     * The next key that was typed by the user.
     * <p>
     * This method returns a Unicode character corresponding to the key
     * typed (such as <tt>'a'</tt> or <tt>'A'</tt>).
     * It cannot identify action keys (such as F1 and arrow keys)
     * or modifier keys (such as control).
     *
     * @return the next key typed by the user
     */
    public static char nextKeyTyped() {
        synchronized (keyLock) {
            return keysTyped.removeLast();
        }
    }

    /**
     * Returns true if the given key is being pressed.
     * <p>
     * This method takes the keycode (corresponding to a physical key)
     * as an argument. It can handle action keys
     * (such as F1 and arrow keys) and modifier keys (such as shift and control).
     * See {@link KeyEvent} for a description of key codes.
     *
     * @param keycode the key to check if it is being pressed
     * @return <tt>true</tt> if <tt>keycode</tt> is currently being pressed;
     * <tt>false</tt> otherwise
     */
    public static boolean isKeyPressed(int keycode) {
        synchronized (keyLock) {
            return keysDown.contains(keycode);
        }
    }


    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.add(e.getKeyCode());
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.remove(e.getKeyCode());
        }
    }

}
/******************************************************************************
 * Copyright 2002-2015, Robert Sedgewick and Kevin Wayne.
 * <p>
 * This file is part of algs4.jar, which accompanies the textbook
 * <p>
 * Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 * Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 * http://algs4.cs.princeton.edu
 * <p>
 * <p>
 * algs4.jar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * algs4.jar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
