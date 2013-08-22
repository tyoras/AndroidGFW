package yoan.game.framework.modules.graphics;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

/**
 * Implémentation Android de la gestion des graphismes du jeu
 * @author yoan
 */
public class AndroidGraphics implements Graphics {
	/** Accès au gestionnaire d'assets */
	AssetManager assets;
	/** Frame buffer artificiel pour préparer les images */
	Bitmap frameBuffer;
	/** Canvas pour modifier le frameBuffer artificiel */
	Canvas canvas;
	/** instance de Paint à réutiliser */
	Paint paint;
	/** instance de Rect  pour les coordonnées de source à réutiliser */
	Rect srcRect= new Rect();
	/** instance de Rect pour les coordonnées de destination à réutiliser */
	Rect dstRect= new Rect();
	
	/**
	 * Constructeur
	 * @param assets : accès au gestionnaire d'assets
	 * @param frameBuffer : Bitmap Android utilisée comme frameBuffer artificiel
	 */
	public AndroidGraphics(AssetManager assets, Bitmap frameBuffer){
		this.assets= assets;
		this.frameBuffer= frameBuffer;
		this.canvas= new Canvas(frameBuffer);
		this.paint= new Paint();
	}
	
	/**
	 * Charge une image au format donné à partir d'un fichier
	 * @param fileName : le fichier image
	 * @param format : le format de l'image créée
	 * @return l'image chargée 
	 */
	public Pixmap newPixmap(String fileName, PixmapFormat format){
		Config config= null;
		switch (format) {
			case RGB565 :
				config= Config.RGB_565;
				break;
			case ARGB4444 :
				config= Config.ARGB_4444;
				break;
			default :
				config= Config.ARGB_8888;
				break;
		}
		Options options= new Options();
		options.inPreferredConfig= config;
		InputStream in= null;
		Bitmap bitmap= null;
		try{
			in= assets.open(fileName);
			bitmap= BitmapFactory.decodeStream(in, null, options);
			if(bitmap == null) throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");
		}catch(IOException e){
			throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(IOException e){}
			}
		}
		//on vérifie quel format a utilisé la BitmapFactory d'Android 
		config = bitmap.getConfig();
		switch (config) {
			case RGB_565 :
				format= PixmapFormat.RGB565;
				break;
			case ARGB_4444 :
				format= PixmapFormat.ARGB4444;
				break;
			default :
				format= PixmapFormat.ARGB8888;
				break;
		}
		//et on renvoie AndroidPixmap généré à partir du Bitmap
		return new AndroidPixmap(bitmap, format);
	}
	
	/**
	 * Remplit le framebuffer avec la couleur donnée
	 * @param color
	 */
	public void clear(int color){
		//extraction des bits coorespondant aux RGB dans le int représentant la couleur
		canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8, (color & 0xff));
	}
	
	/**
	 * Fixe la couleur d'un pixel dans le frameBuffer
	 * @param x : abcisse du pixel
	 * @param y : ordonnée du pixel
	 * @param color : la couleur à appliquer au pixel
	 */
	public void drawPixel(int x, int y, int color) {
		paint.setColor(color);
		canvas.drawPoint(x, y, paint);
	}
	
	/**
	 * Dessine un ligne partant du pixel (x,y) jusqu'au pixel (x2,y2)
	 * @param x : abcisse du pixel de départ
	 * @param y : ordonnée du pixel de départ
	 * @param x : abcisse du pixel d'arrivée
	 * @param y : ordonnée du pixel d'arrivée
	 * @param color : couleur de la ligne
	 */
	public void drawLine(int x, int y, int x2, int y2, int color) {
		paint.setColor(color);
		canvas.drawLine(x, y, x2, y2, paint);
	}
	
	/**
	 * Dessine un rectange avec le coin supérieur gauche au pixel (x,y)
	 * @param x : abcisse du coin supérieur gauche du rectangle
	 * @param y : ordonnée du coin supérieur gauche du rectangle
	 * @param width : largeur du rectangle
	 * @param height : hauteur du rectangle
	 * @param color : couleur du rectangle
	 */
	public void drawRect(int x, int y, int width, int height, int color){
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		//on calcule le coin inférieur droit
		canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
	}
	
	/**
	 * Dessine une partie d'une image avec le coin supérieur gauche au pixel (x,y)
	 * @param pixmap : image à dessiner
	 * @param x : abcisse du coin supérieur gauche de l'image dessinée
	 * @param y : ordonnée du coin supérieur gauche de l'image dessinée
	 * @param srcX : abcisse du coin supérieur gauche de la partie de l'image à dessiner
	 * @param srcY : ordonnée du coin supérieur gauche de la partie de l'image à dessiner
	 * @param srcWidth : largeur du morceau de l'image à dessiner
	 * @param srcHeight : hauteur du morceau de l'image à dessiner
	 */
	public void drawPixmap(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight){
		srcRect.left= srcX;
		srcRect.top= srcY;
		srcRect.right= srcX + srcWidth - 1;
		srcRect.bottom= srcY + srcHeight - 1;
		dstRect.left= x;
		dstRect.top= y;
		dstRect.right= x + srcWidth - 1;
		dstRect.bottom= y + srcHeight - 1;
		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, srcRect, dstRect, null);
	}
	
	/**
	 * Dessine une image avec le coin supérieur gauche au pixel (x,y)
	 * @param pixmap : image à dessiner
	 * @param x : abcisse du coin supérieur gauche de l'image dessinée
	 * @param y : ordonnée du coin supérieur gauche de l'image dessinée
	 */
	public void drawPixmap(Pixmap pixmap, int x, int y){
		canvas.drawBitmap(((AndroidPixmap) pixmap).bitmap, x, y, null);
	}
	
	/**
	 * Donne la largeur de l'écran
	 * @return largeur en pixel
	 */
	public int getWidth(){
		return frameBuffer.getWidth();
	}

	/**
	 * Donne la hauteur de l'écran
	 * @return hauteur en pixel
	 */
	public int getHeight(){
		return frameBuffer.getHeight();
	}
}