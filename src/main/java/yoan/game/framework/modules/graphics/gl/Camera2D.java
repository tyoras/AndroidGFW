package yoan.game.framework.modules.graphics.gl;

import javax.microedition.khronos.opengles.GL10;

import yoan.game.framework.util.math.Vector2;

/**
 * Caméra openGL ES en 2D
 * @author yoan
 */
public class Camera2D {
	/** Position du point vers lequel la caméra est centrée */
	public final Vector2 position;
	/** Niveau de zoom [0-1] : in, [1,n] : out */
	public float zoom;
	/** Largeur du frustum */
	public final float frustumWidth;
	/** Hauteur du frustum */
	public final float frustumHeight;
	/** Accès Android à OpenGL ES 1.0 */
	private final GLGraphics glGraphics;

	/**
	 * Constructeur avec paramètres
	 * @param glGraphics
	 * @param frustumWidth
	 * @param frustumHeight
	 */
	public Camera2D(GLGraphics glGraphics, float frustumWidth, float frustumHeight) {
		this.glGraphics = glGraphics;
		this.frustumWidth = frustumWidth;
		this.frustumHeight = frustumHeight;
		//on centre la caméra sur le centre du frustum
		this.position = new Vector2(frustumWidth / 2, frustumHeight / 2);
		this.zoom = 1.0f;
	}

	/**
	 * Modifie le viewport et la projection en fonction de la caméra
	 * et remet la matrice courante sur GL_MODELVIEW
	 */
	public void setViewportAndMatrices() {
		GL10 gl = glGraphics.getGL();
		gl.glViewport(0, 0, glGraphics.getWidth(), glGraphics.getHeight());
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		//placement au centre de la caméra, et taille de la projection en fonction du zoom
		gl.glOrthof(position.x - frustumWidth * zoom / 2,
					position.x + frustumWidth * zoom / 2,
					position.y - frustumHeight * zoom / 2,
					position.y + frustumHeight * zoom / 2,
					1, -1);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	/**
	 * Modifie les coordonnées d'un vecteur correspondant à un point sur l'écran
	 * en coordonnées dans le jeu
	 * @param touch : coordonnées écran à convertir en coordonnées du jeu
	 */
	public void touchToWorld(Vector2 touch) {
		touch.x = (touch.x / (float) glGraphics.getWidth()) * frustumWidth * zoom;
		//l'axe Y de l'écran est inversé
		touch.y = (1 - touch.y / (float) glGraphics.getHeight()) * frustumHeight * zoom;
		touch.add(position).sub(frustumWidth * zoom / 2, frustumHeight * zoom / 2);
	}
}