package yoan.game.framework.modules.graphics;

import yoan.game.framework.modules.graphics.Graphics.PixmapFormat;
import android.graphics.Bitmap;

/**
 * Implémentation Android de la gestion d'une image dans le framebuffer
 * @author yoan
 */
public class AndroidPixmap implements Pixmap {
	/** Bitmap gérée par Android */
	Bitmap bitmap;
	/** le format des pixels de l'image dans le framebuffer */
	PixmapFormat format;

	/**
	 * Constructeur d'image
	 * @param bitmap : Bitmap gérée par Android
	 * @param format : format des pixels de l'image
	 */
	public AndroidPixmap(Bitmap bitmap, PixmapFormat format){
		this.bitmap= bitmap;
		this.format= format;
	}

	/**
	 * Donne la largeur de l'image
	 * @return largeur en pixel
	 */
	public int getWidth(){
		return bitmap.getWidth();
	}

	/**
	 * Donne la hauteur de l'image
	 * @return hauteur en pixel
	 */
	public int getHeight(){
		return bitmap.getHeight();
	}

	/**
	 * Donne le format de l'image dans le framebuffer
	 * @return format d'image
	 */
	public PixmapFormat getFormat(){
		return format;
	}

	/**
	 * Libére la mémoire de l'image
	 */
	public void dispose(){
		bitmap.recycle();
	}
}