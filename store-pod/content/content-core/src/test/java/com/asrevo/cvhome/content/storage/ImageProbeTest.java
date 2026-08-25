package com.asrevo.cvhome.content.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dimensions are read from the header, and anything that is not a raster image the JDK knows must come back empty
 * rather than throw — an upload of a PDF or a sanitised SVG goes through the same code path.
 */
class ImageProbeTest {

    @Test
    void aRealImageReportsItsPixelSize() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(24, 12, BufferedImage.TYPE_INT_RGB), "png", out);

        assertThat(ImageProbe.dimensions(out.toByteArray()))
                .contains(new ImageProbe.Dimensions(24, 12));
    }

    @Test
    void bytesThatAreNotAnImageComeBackEmpty() {
        assertThat(ImageProbe.dimensions("not an image at all".getBytes(StandardCharsets.UTF_8))).isEmpty();
        assertThat(ImageProbe.dimensions(new byte[0])).isEmpty();
    }

}
