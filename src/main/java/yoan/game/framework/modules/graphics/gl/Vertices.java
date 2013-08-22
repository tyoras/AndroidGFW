package yoan.game.framework.modules.graphics.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Gestion Android des vertices pour OpenGL ES 1.0
 * @author yoan
 */
public class Vertices {
	/** Accès Android à OpenGL ES 1.0 */
	final GLGraphics glGraphics;
	/** Indique si les vertices ont des couleurs */
	final boolean hasColor;
	/** Indique si les vertices ont une texture */
	final boolean hasTexCoords;
	/** Taille d'un vertex en byte */
	final int vertexSize;
	/** Tableau contenant les coordonées des vertices et autres infos supplémentaires */
	final FloatBuffer vertices;
	/** tableau des indices des vertices */
	final ShortBuffer indices;
	
	/**
	 * Constructeur de vertices à partir des options
	 * @param glGraphics : gestionnaire des graphismes openGL ES
	 * @param maxVertices : nombre max de vertices stockés
	 * @param maxIndices : nombre max d'indices stockés
	 * @param hasColor : inidique si la couleur est gérée
	 * @param hasTexCoords : indique si une texture est gérée
	 */
	public Vertices(GLGraphics glGraphics, int maxVertices, int maxIndices, boolean hasColor, boolean hasTexCoords){
		this.glGraphics= glGraphics;
		this.hasColor= hasColor;
		this.hasTexCoords= hasTexCoords;
		//calcul de la taille d'un vertex : (2 coord + 0 ou 4 couleur + 0 ou 2 coord de texture) * taille d'un float en byte
		this.vertexSize= (2 + (hasColor ? 4 : 0) + (hasTexCoords ? 2 : 0)) * Float.SIZE / 8;
		//allocation du tableau de vertices
		ByteBuffer buffer= ByteBuffer.allocateDirect(maxVertices * vertexSize);
		buffer.order(ByteOrder.nativeOrder());
		vertices= buffer.asFloatBuffer();
		
		if(maxIndices > 0){
			//allocation du tableau des indices
			buffer= ByteBuffer.allocateDirect(maxIndices * Short.SIZE / 8);
			buffer.order(ByteOrder.nativeOrder());
			indices= buffer.asShortBuffer();
		}else{
			indices= null;
		}
	}
	
	/**
	 * Mise à jour des données des vertices
	 * @param vertices : données des vertices
	 * @param offset : indice de début des données à copier
	 * @param length : nombre de float à copier
	 */
	public void setVertices(float[] vertices, int offset, int length){
		this.vertices.clear();
		this.vertices.put(vertices, offset, length);
		this.vertices.flip();
	}

	/**
	 * Mise à jour des données des indices
	 * @param vertices : données des indices
	 * @param offset : indice de début des données à copier
	 * @param length : nombre de short à copier
	 */
	public void setIndices(short[] indices, int offset, int length){
		this.indices.clear();
		this.indices.put(indices, offset, length);
		this.indices.flip();
	}

	/**
	 * Binding des données des vertices
	 */
	public void bind(){
		GL10 gl= glGraphics.getGL();
		//binding des coordonnées des vertices
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		vertices.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		//si on gère la couleur
		if(hasColor){
			//binding des données des couleurs
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			vertices.position(2);
			gl.glColorPointer(4, GL10.GL_FLOAT, vertexSize, vertices);
		}
		//si on gère une texture 
		if(hasTexCoords){
			//binding des coordonnées de texture
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			vertices.position(hasColor ? 6 : 2);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		}
	}
	
	/**
	 * Dessine une forme primitive à l'aide d'un sous ensemble des données du tableau de vertices
	 * @param primitiveType : ID de la forme primitive à dessiner
	 * @param offset : indice de départ dans les tableaux de données en fonction de si les vertices sont indéxés ou non
	 * @param numVertices : nombre de vertex à dessiner
	 */
	public void draw(int primitiveType, int offset, int numVertices){
		GL10 gl= glGraphics.getGL();
		//si indexation des vertices
		if(indices != null){
			//on utilise les indices pour dessiner
			indices.position(offset);
			gl.glDrawElements(primitiveType, numVertices, GL10.GL_UNSIGNED_SHORT, indices);
		}else{
			//sinon, on utilise directement les coordonnées
			gl.glDrawArrays(primitiveType, offset, numVertices);
		}
	}
	
	/**
	 * Unbinding des données des vertices
	 */
	public void unbind(){
		GL10 gl= glGraphics.getGL();
		//unbinding si nécessaire
		if(hasTexCoords) gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		if(hasColor) gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
}