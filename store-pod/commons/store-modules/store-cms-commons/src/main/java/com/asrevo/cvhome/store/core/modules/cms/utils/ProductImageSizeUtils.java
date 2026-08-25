package com.asrevo.cvhome.store.core.modules.cms.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Utility class for image resize functions
 *
 * @author Carl Samson
 */
public final class ProductImageSizeUtils {

    private ProductImageSizeUtils() {
    }

    public static BufferedImage resizeWithRatio(BufferedImage image, int destinationWidth, int destinationHeight) {

        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();

        if (destinationWidth == 0) {
            destinationWidth = image.getWidth();
        }
        if (destinationHeight == 0) {
            destinationHeight = image.getHeight();
        }

        int fHeight = destinationHeight;
        int fWidth = destinationWidth;

        if (image.getHeight() > destinationHeight || image.getWidth() > destinationWidth) {
            if (image.getHeight() > image.getWidth()) {
                float sum = (float) image.getWidth() / (float) image.getHeight();
                fWidth = Math.round(destinationWidth * sum);
            } else if (image.getWidth() > image.getHeight()) {
                float sum = (float) image.getHeight() / (float) image.getWidth();
                fHeight = Math.round(destinationHeight * sum);
            }
        }

        BufferedImage resizedImage = new BufferedImage(fWidth, fHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(image, 0, 0, fWidth, fHeight, null);
        g.dispose();

        return resizedImage;
    }

}
