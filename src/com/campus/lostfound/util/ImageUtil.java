package com.campus.lostfound.util;

import com.campus.lostfound.config.AppConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Image processing, file copying, and thumbnail generation.
 */
public final class ImageUtil {
    private ImageUtil() {}

    /**
     * Copies an external image file into the project data/images directory.
     * Returns the relative path or filename saved.
     */
    public static String saveImageToApp(File sourceFile) throws IOException {
        File imagesDir = new File(AppConfig.IMAGES_DIR);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        String extension = "";
        String name = sourceFile.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            extension = name.substring(dotIndex);
        } else {
            extension = ".jpg";
        }

        String targetFileName = "item_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        File targetFile = new File(imagesDir, targetFileName);

        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return targetFileName;
    }

    /**
     * Loads an image icon scaled to target dimensions, or returns a styled placeholder if missing.
     */
    public static ImageIcon loadScaledImage(String imageFileName, int width, int height) {
        if (imageFileName != null && !imageFileName.trim().isEmpty()) {
            File file = new File(AppConfig.IMAGES_DIR, imageFileName);
            if (!file.exists()) {
                // Try direct file path
                file = new File(imageFileName);
            }

            if (file.exists()) {
                try {
                    BufferedImage original = ImageIO.read(file);
                    if (original != null) {
                        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (IOException ignored) {}
            }
        }

        return createPlaceholderIcon(width, height);
    }

    public static ImageIcon createPlaceholderIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(241, 245, 249));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(203, 213, 225));
        g2.drawRect(0, 0, width - 1, height - 1);

        g2.setColor(new Color(148, 163, 184));
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(11, height / 7)));
        FontMetrics fm = g2.getFontMetrics();
        String text = "No Image";
        int tx = (width - fm.stringWidth(text)) / 2;
        int ty = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, tx, ty);
        g2.dispose();

        return new ImageIcon(image);
    }
}
