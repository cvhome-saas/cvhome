package com.asrevo.cvhome.store.core.modules.cms.utils;

import java.awt.*;
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

			// get original image size

			int width = originalFile.getWidth();
			int height = originalFile.getHeight();

			determineCropeable(width, largeImageWidth, height, largeImageHeight);

			// this.determineBaseline(width, height);

			determineCropArea(width, largeImageWidth, height, largeImageHeight);

		}
		catch (Exception e) {
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

		double factorWidth = Integer.valueOf(width).doubleValue() / Integer.valueOf(specificationsWidth).doubleValue();
		double factorHeight = Integer.valueOf(height).doubleValue()
				/ Integer.valueOf(specificationsHeight).doubleValue();

		double factor = Math.min(factorWidth, factorHeight);

		// crop factor
		/*
		 * double factor = 1; if (this.getCropeBaseline() == 0) {// width factor = new
		 * Integer(width).doubleValue() / new Integer(specificationsWidth).doubleValue();
		 * } else {// height factor = new Integer(height).doubleValue() / new
		 * Integer(specificationsHeight).doubleValue(); }
		 */

		double w = factor * specificationsWidth;
		double h = factor * specificationsHeight;

		if (w == h) {
			setCropeable(false);
		}

		cropAreaWidth = w;

		if (cropAreaWidth > width)
			cropAreaWidth = width;

		cropAreaHeight = h;

		if (cropAreaHeight > height)
			cropAreaHeight = height;

		/*
		 * if(factor>1) { //determine croping section for(double i=factor;i>1;i--) {
		 * //multiply specifications by factor int newWidth = (int)(i *
		 * specificationsWidth); int newHeight = (int)(i * specificationsHeight); //check
		 * if new size >= original image if(width>=newWidth && height>=newHeight) {
		 * cropAreaWidth = newWidth; cropAreaHeight = newHeight; break; } } }
		 */

	}

	public BufferedImage getCroppedImage() {

		// out if croppedArea == 0 or file is null

		Rectangle goal = new Rectangle((int) this.getCropAreaWidth(), (int) this.getCropAreaHeight());

		// Then intersect it with the dimensions of your image:

		Rectangle clip = goal.intersection(new Rectangle(originalFile.getWidth(), originalFile.getHeight()));

		// Now, clip corresponds to the portion of bi that will fit within your goal. In
		// this case
		// 100 x50.

		// Now get the subImage using the value of clip.

		return originalFile.getSubimage(clip.x, clip.y, clip.width, clip.height);
	}

}
