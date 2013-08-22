package yoan.game.framework.modules.graphics.gl;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

/**
 * Gestion des graphismes du jeu via OpenGL ES
 * @author yoan
 */
public class GLGraphics {
	/** la vue Android pour OpenGL ES */
	GLSurfaceView glView;
	/** Interface vers les fonctions C de openGL ES 1.0 */
	private GL10 gl;

	/**
	 * Constructeur à partir de la GLView
	 * @param glView : vue spéciale pour OpenGL ES
	 */
	public GLGraphics(GLSurfaceView glView){
		this.glView= glView;
	}

	/**
	 * Récupère l'instance de GL10
	 * @return GL10
	 */
	public GL10 getGL(){
		return gl;
	}

	/**
	 * Valorise l'instance de GL10
	 * @param gl : GL10
	 */
	public void setGL(GL10 gl){
		this.gl= gl;
	}

	/**
	 * Donne la largeur de l'écran
	 * @return largeur en pixel
	 */
	public int getWidth(){
		return glView.getWidth();
	}

	/**
	 * Donne la hauteur de l'écran
	 * @return hauteur en pixel
	 */
	public int getHeight(){
		return glView.getHeight();
	}
}