package yoan.game.framework.modules.graphics.gl;

/**
 * Représente une région d'un texture atlas
 * @author yoan
 */
public class TextureRegion {
	/** Coordonées [0-1] du coin supérieur gauche dans le texture atlas */
	public final float u1, v1;
	/** Coordonées [0-1] du coin inférieur droit dans le texture atlas */
	public final float u2, v2;
	/** Texture atlas */
	public final Texture texture;

	/**
	 * Constructeur avec paramètres
	 * @param texture : texture atlas
	 * @param x : abcisse en pixel du coin supérieur gauche dans le texture atlas
	 * @param y : ordonnée en pixel du coin supérieur gauche dans le texture atlas
	 * @param width : largeur de la région
	 * @param height : hauteur de la région
	 */
	public TextureRegion(Texture texture, float x, float y, float width, float height){
		this.u1= x / texture.width;
		this.v1= y / texture.height;
		this.u2= this.u1 + width / texture.width;
		this.v2= this.v1 + height / texture.height;
		this.texture= texture;
	}
}