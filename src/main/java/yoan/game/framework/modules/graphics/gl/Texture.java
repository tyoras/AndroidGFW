package yoan.game.framework.modules.graphics.gl;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import yoan.game.framework.modules.fileio.FileIO;
import yoan.game.framework.modules.game.gl.GLGame;
import yoan.game.framework.util.math.shape.RectangleShape;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

/**
 * Gestion Android des textures pour OpenGL ES 1.0
 * @author yoan
 */
public class Texture implements RectangleShape {
	/** Accès Android à OpenGL ES 1.0 */
	GLGraphics glGraphics;
	/** Gestionnaire de fichier */
	FileIO fileIO;
	/** Nom du fichier image de la texture */
	String fileName;
	/** Identifiant de la texture dans le GPU */
	int textureId;
	/** Filtre de minification de la texture */
	int minFilter;
	/** Filtre de magnification de la texture */
	int magFilter;
	/** Largeur de la texture */
	int width;
	/** Hauteur de la texture */
	int height;

	/** 
	 * Constructeur à partir du nom du fichier
	 * @param glGame : l'instance du jeu
	 * @param fileName : nom du fichier image de la texture
	 */
	public Texture(GLGame glGame, String fileName){
		this.glGraphics= glGame.getGLGraphics();
		this.fileIO= glGame.getFileIO();
		this.fileName= fileName;
		//chargement de la texture
		load();
	}

	/**
	 * Charge la texture dans la mémoire du GPU
	 */
	private void load(){
		GL10 gl= glGraphics.getGL();
		//génération de l'ID de la texture dans la memoire GPU
		int[] textureIds= new int[1];
		gl.glGenTextures(1, textureIds, 0);
		textureId= textureIds[0];
		
		
		InputStream in= null;
		try{
			//lecture de la bitmap de la texture à partir de l'asset
			in= fileIO.readAsset(fileName);
			Bitmap bitmap= BitmapFactory.decodeStream(in);
			width= bitmap.getWidth();
			height= bitmap.getHeight();
			//binding sur l'ID généré
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			//puis upload de la bitmap vers le GPU
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			//mise en place des filtres de min et mag
			setFilters(GL10.GL_NEAREST, GL10.GL_NEAREST);
			//on ne pointe plus sur cet ID de texture
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		}catch(IOException e){
			throw new RuntimeException("Couldn't load texture '" + fileName + "'", e);
		}finally{
			if(in != null) try{
				in.close();
			}catch(IOException e){}
		}
	}

	/**
	 * Re-chargement d'une texture
	 */
	public void reload(){
		load();
		bind();
		setFilters(minFilter, magFilter);
		glGraphics.getGL().glBindTexture(GL10.GL_TEXTURE_2D, 0);
	}

	/**
	 * Mise en place des filtres de minification et magnification (rescaling de la texture)
	 * Necessite de faire préalablement un binding 
	 * @param minFilter : LINEAR ou NEAREST
	 * @param magFilter : LINEAR ou NEAREST
	 */
	public void setFilters(int minFilter, int magFilter){
		this.minFilter= minFilter;
		this.magFilter= magFilter;
		GL10 gl= glGraphics.getGL();
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
	}

	/**
	 * Binding de la texture dans la mémoire GPU
	 */
	public void bind(){
		GL10 gl= glGraphics.getGL();
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	}

	/**
	 * Efface la texture de la mémoire GPU
	 */
	public void dispose(){
		GL10 gl= glGraphics.getGL();
		//on ne pointe plus sur cet ID de texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		//et on l'efface du GPU
		int[] textureIds= {textureId};
		gl.glDeleteTextures(1, textureIds, 0);
	}

	@Override
	public float getWidth(){
		return width;
	}

	@Override
	public float getHeight(){
		return height;
	}
}