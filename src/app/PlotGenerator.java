package app;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

// draws simple line charts to png with Java2D (no libraries)
// both axes are log scale, because sizes grow 10 times and times are very different
public class PlotGenerator {
    private static final Color[] COLORS = {
            new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c),
            new Color(0xff7f0e), new Color(0x9467bd), new Color(0x8c564b)
    };
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;
    // space around the plot area
    private static final int LEFT = 100;
    private static final int RIGHT = 40;
    private static final int TOP = 60;
    private static final int BOTTOM = 70;

    public static void logLogChart(String title, String xLabel, String yLabel,
                                   double[] x, Map<String, double[]> series, File out) throws IOException {
        System.setProperty("java.awt.headless", "true");
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // find min and max values (only > 0 because of log)
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (double[] ys : series.values()) {
            for (double y : ys) {
                if (y > 0) {
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (double v : x) {
            minX = Math.min(minX, v);
            maxX = Math.max(maxX, v);
        }
        // axis from one power of 10 to another
        int x0 = (int) Math.floor(Math.log10(minX));
        int x1 = (int) Math.ceil(Math.log10(maxX));
        int y0 = (int) Math.floor(Math.log10(minY));
        int y1 = (int) Math.ceil(Math.log10(maxY));
        if (x1 == x0) {
            x1++;
        }
        if (y1 == y0) {
            y1++;
        }
        int plotW = WIDTH - LEFT - RIGHT;
        int plotH = HEIGHT - TOP - BOTTOM;

        // grid lines and labels for every power of 10
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        for (int k = x0; k <= x1; k++) {
            int px = LEFT + (int) Math.round((double) (k - x0) / (x1 - x0) * plotW);
            g.setColor(new Color(0xe0e0e0));
            g.drawLine(px, TOP, px, TOP + plotH);
            g.setColor(Color.DARK_GRAY);
            String label = format(Math.pow(10, k));
            g.drawString(label, px - fm.stringWidth(label) / 2, TOP + plotH + 20);
        }
        for (int k = y0; k <= y1; k++) {
            int py = TOP + plotH - (int) Math.round((double) (k - y0) / (y1 - y0) * plotH);
            g.setColor(new Color(0xe0e0e0));
            g.drawLine(LEFT, py, LEFT + plotW, py);
            g.setColor(Color.DARK_GRAY);
            String label = format(Math.pow(10, k));
            g.drawString(label, LEFT - 10 - fm.stringWidth(label), py + 5);
        }
        g.setColor(Color.BLACK);
        g.drawRect(LEFT, TOP, plotW, plotH);

        // lines
        int index = 0;
        for (Map.Entry<String, double[]> s : series.entrySet()) {
            g.setColor(COLORS[index % COLORS.length]);
            g.setStroke(new BasicStroke(2.5f));
            double[] ys = s.getValue();
            int prevX = -1;
            int prevY = -1;
            for (int i = 0; i < x.length; i++) {
                if (ys[i] <= 0) {
                    continue;
                }
                int px = LEFT + (int) Math.round((Math.log10(x[i]) - x0) / (x1 - x0) * plotW);
                int py = TOP + plotH - (int) Math.round((Math.log10(ys[i]) - y0) / (y1 - y0) * plotH);
                if (prevX >= 0) {
                    g.drawLine(prevX, prevY, px, py);
                }
                g.fillOval(px - 5, py - 5, 10, 10);
                prevX = px;
                prevY = py;
            }
            // legend in top left corner
            int ly = TOP + 22 + index * 22;
            g.fillRect(LEFT + 15, ly - 8, 22, 4);
            g.setColor(Color.BLACK);
            g.drawString(s.getKey(), LEFT + 45, ly);
            index++;
        }

        // title and axis names
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 35);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        g.drawString(xLabel, LEFT + (plotW - fm.stringWidth(xLabel)) / 2, HEIGHT - 20);
        // y label is rotated
        AffineTransform old = g.getTransform();
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -(TOP + (plotH + fm.stringWidth(yLabel)) / 2), 25);
        g.setTransform(old);

        g.dispose();
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
    }

    // 1000.0 -> "1000", 0.01 -> "0.01"
    private static String format(double v) {
        return new BigDecimal(String.valueOf(v)).stripTrailingZeros().toPlainString();
    }
}
