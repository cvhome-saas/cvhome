package com.asrevo.cvhome.store.core.modules.cms.utils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductImageCropUtils {

    // o is width, 1 is height
    @Setter
    @Getter
    private boolean cropeable = true;

    @Getter
    private double cropAreaWidth = 0;

    @Getter
    private double cropAreaHeight = 0;

    private BufferedImage originalFile = null;

    public ProductImageCropUtils(BufferedImage file, int largeImageWidth, int largeImageHeight) {

        try {

            this.originalFile = file;

            int width = originalFile.getWidth();
            int height = originalFile.getHeight();

            determineCropeable(width, largeImageWidth, height, largeImageHeight);
            determineCropArea(width, largeImageWidth, height, largeImageHeight);

        } catch (Exception e) {
            log.error("Image Utils error in constructor", e);
        }
    }

    private void determineCropeable(int width, int specificationsWidth, int height, int specificationsHeight) {
        // height
        int y = height - specificationsHeight;
        // width
        int x = width - specificationsWidth;

        if (x < 0 || y < 0) {
            setCropeable(false);
        }

        if (x == 0 && y == 0) {
            setCropeable(false);
        }

        if ((height % specificationsHeight) == 0 && (width % specificationsWidth) == 0) {
            setCropeable(false);
        }
    }

    private void determineCropArea(int width, int specificationsWidth, int height, int specificationsHeight) {

        cropAreaWidth = specificationsWidth;
        cropAreaHeight = specificationsHeight;

        double factorWidth = (double) width / specificationsWidth;
        double factorHeight = (double) height / specificationsHeight;

        double factor = Math.min(factorWidth, factorHeight);

        double w = factor * specificationsWidth;
        double h = factor * specificationsHeight;

        if (w == h) {
            setCropeable(false);
        }

        cropAreaWidth = w;

        if (cropAreaWidth > width) {
            cropAreaWidth = width;
        }

        cropAreaHeight = h;

        if (cropAreaHeight > height) {
            cropAreaHeight = height;
        }

    }

    public BufferedImage getCroppedImage() {
        Rectangle goal = new Rectangle((int) this.getCropAreaWidth(), (int) this.getCropAreaHeight());
        Rectangle clip = goal.intersection(new Rectangle(originalFile.getWidth(), originalFile.getHeight()));


        return originalFile.getSubimage(clip.x, clip.y, clip.width, clip.height);
    }

}
