package yoan.game.framework.modules.graphics.gl;

import javax.microedition.khronos.opengles.GL10;

import yoan.game.framework.util.math.Vector2;

/**
 * Batch d'affichage de sprite
 * Remplit un buffer de commande d'affichage de sprite et effectue un seul appel à openGL ES pour effectuer l'affichage
 * @author yoan
 */
public class SpriteBatcher {
	/** Buffer de vertex des sprites */
	final float[] verticesBuffer;
	/** Indique la position du prochain espace libre dans le buffer */
	int bufferIndex;
	/** Contient la totalité des vertices et leurs indexs pour faire le rendu openGL à la fin d'un batch */
	final Vertices vertices;
	/** Nombre de sprite actuel dans le buffer */
	int numSprites;
	
	/**
	 * Constructeur avec paramètres
	 * @param glGraphics : Accès Android à OpenGL ES 1.0
	 * @param maxSprites : Nombre de sprite maximum dans le buffer
	 */
	public SpriteBatcher(GLGraphics glGraphics, int maxSprites){
		//4 floats par vertex et 4 vertices par sprite
		this.verticesBuffer= new float[maxSprites * 4 * 4];
		//4 vertices par sprite et 6 indices par sprite
		this.vertices= new Vertices(glGraphics, maxSprites * 4, maxSprites * 6, false, true);
		this.bufferIndex= 0;
		this.numSprites= 0;
		//on ordonne les indices pour faire des rectangles
		short[] indices= new short[maxSprites * 6];
		int len= indices.length;
		short j= 0;
		for(int i= 0; i < len; i+= 6, j+= 4){
			//0, 1, 2, 2, 3, 0
			indices[i + 0]= (short) (j + 0);
			indices[i + 1]= (short) (j + 1);
			indices[i + 2]= (short) (j + 2);
			indices[i + 3]= (short) (j + 2);
			indices[i + 4]= (short) (j + 3);
			indices[i + 5]= (short) (j + 0);
		}
		vertices.setIndices(indices, 0, indices.length);
	}
	
	/**
	 * Commence un nouveau batch sur un texture atlas
	 * @param texture : texture atlas
	 */
	public void beginBatch(Texture texture) {
		//chargement de la texture
		texture.bind();
		numSprites = 0;
		bufferIndex = 0;
	}
	
	/**
	 * Ajoute un sprite à dessiner
	 * @param x : abcisse du centre du sprite
	 * @param y : ordonnée du centre du sprite
	 * @param width : largeur du sprite
	 * @param height : hauteur du sprite
	 * @param region : région du texture atlas à utiliser
	 */
	public void drawSprite(float x, float y, float width, float height, TextureRegion region) {
		//pré-calcul
		float halfWidth = width / 2;
		float halfHeight = height / 2;
		//calcul du coin inférieur gauche
		float x1 = x - halfWidth;
		float y1 = y - halfHeight;
		//calcul du coin supérieur droit
		float x2 = x + halfWidth;
		float y2 = y + halfHeight;
		
		//coin inférieur gauche
		verticesBuffer[bufferIndex++] = x1;
		verticesBuffer[bufferIndex++] = y1;
		verticesBuffer[bufferIndex++] = region.u1;
		verticesBuffer[bufferIndex++] = region.v2;
		//coin inférieur droit
		verticesBuffer[bufferIndex++] = x2;
		verticesBuffer[bufferIndex++] = y1;
		verticesBuffer[bufferIndex++] = region.u2;
		verticesBuffer[bufferIndex++] = region.v2;
		//coin supérieur droit
		verticesBuffer[bufferIndex++] = x2;
		verticesBuffer[bufferIndex++] = y2;
		verticesBuffer[bufferIndex++] = region.u2;
		verticesBuffer[bufferIndex++] = region.v1;
		//coin supérieur gauche
		verticesBuffer[bufferIndex++] = x1;
		verticesBuffer[bufferIndex++] = y2;
		verticesBuffer[bufferIndex++] = region.u1;
		verticesBuffer[bufferIndex++] = region.v1;
		
		numSprites++;
	}
	
	/**
	 * Ajoute un sprite à dessiner avec une rotation
	 * @param x : abcisse du centre du sprite
	 * @param y : ordonnée du centre du sprite
	 * @param width : largeur du sprite
	 * @param height : hauteur du sprite
	 * @param angle : angle de rotation en degré
	 * @param region : région du texture atlas à utiliser
	 */
	public void drawSprite(float x, float y, float width, float height, float angle, TextureRegion region){
		//pré-calcul
		float halfWidth= width / 2;
		float halfHeight= height / 2;
		float rad= angle * Vector2.TO_RADIANS;
		float cos= (float) Math.cos(rad);
		float sin= (float) Math.sin(rad);
		
		//calcul du coin inférieur gauche
		float x1= -halfWidth * cos - (-halfHeight) * sin;
		float y1= -halfWidth * sin + (-halfHeight) * cos;
		//calcul du coin inférieur droit
		float x2= halfWidth * cos - (-halfHeight) * sin;
		float y2= halfWidth * sin + (-halfHeight) * cos;
		//calcul du coin supérieur droit
		float x3= halfWidth * cos - halfHeight * sin;
		float y3= halfWidth * sin + halfHeight * cos;
		//calcul du coin supérieur gauche
		float x4= -halfWidth * cos - halfHeight * sin;
		float y4= -halfWidth * sin + halfHeight * cos;
		
		//recentrage
		x1+= x;
		y1+= y;
		x2+= x;
		y2+= y;
		x3+= x;
		y3+= y;
		x4+= x;
		y4+= y;
		
		//coin inférieur gauche
		verticesBuffer[bufferIndex++]= x1;
		verticesBuffer[bufferIndex++]= y1;
		verticesBuffer[bufferIndex++]= region.u1;
		verticesBuffer[bufferIndex++]= region.v2;
		//coin inférieur droit
		verticesBuffer[bufferIndex++]= x2;
		verticesBuffer[bufferIndex++]= y2;
		verticesBuffer[bufferIndex++]= region.u2;
		verticesBuffer[bufferIndex++]= region.v2;
		//coin supérieur droit
		verticesBuffer[bufferIndex++]= x3;
		verticesBuffer[bufferIndex++]= y3;
		verticesBuffer[bufferIndex++]= region.u2;
		verticesBuffer[bufferIndex++]= region.v1;
		//coin supérieur gauche
		verticesBuffer[bufferIndex++]= x4;
		verticesBuffer[bufferIndex++]= y4;
		verticesBuffer[bufferIndex++]= region.u1;
		verticesBuffer[bufferIndex++]= region.v1;

		numSprites++;
	}
	
	/** 
	 * Termine le batch en cours en faisant un rendu openGL ES 
	 */
	public void endBatch() {
		vertices.setVertices(verticesBuffer, 0, bufferIndex);
		vertices.bind();
		//appel openGl ES
		vertices.draw(GL10.GL_TRIANGLES, 0, numSprites * 6);
		vertices.unbind();
	}
}