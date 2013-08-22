package yoan.game.framework.modules.graphics.gl;

/**
 * Gestion d'une font via openGL ES
 * @author yoan
 */
public class Font {
	/** Texture contenant l'atlas de glyphe */
	public final Texture texture;
	/** Largeur d'une glyphe */
    public final int glyphWidth;
    /** Hauteur d'une glyphe */
    public final int glyphHeight;
    /** Tableau comprenant la texture de chacune des glyphes (commence au  32ème char ASCII)*/
    public final TextureRegion[] glyphs = new TextureRegion[96];   
    
    /**
     * Constructeur paramétré
     * @param texture : texture contenant l'atlas des glyphes
     * @param offsetX : abcisse du coin supérieur gauche de l'atlas de glyphe
     * @param offsetY : ordonnée du coin supérieur gauche de l'atlas de glyphe
     * @param glyphsPerRow : nombre de glyphes par ligne
     * @param glyphWidth : largeur d'une glyphe
     * @param glyphHeight : hauteur d'une glyphe
     */
    public Font(Texture texture, int offsetX, int offsetY, int glyphsPerRow, int glyphWidth, int glyphHeight) {        
        this.texture = texture;
        this.glyphWidth = glyphWidth;
        this.glyphHeight = glyphHeight;
        int x = offsetX;
        int y = offsetY;
        //on determine la région de chacune des glyphes dans la texture
        for(int i = 0; i < 96; i++) {
            glyphs[i] = new TextureRegion(texture, x, y, glyphWidth, glyphHeight);
            x += glyphWidth;
            if(x == offsetX + glyphsPerRow * glyphWidth) {
                x = offsetX;
                y += glyphHeight;
            }
        }        
    }
    
    /**
     * Dessine un texte avec la font
     * @param batcher : le batcher de sprite pour l'affichage
     * @param text : le texte à afficher
     * @param x : abcisse du centre du premier char
     * @param y : ordonnée du centre du premier char
     */
    public void drawText(SpriteBatcher batcher, String text, float x, float y) {
        int len = text.length();
        //on dessine chacune des glyphes sur une seule ligne
        for(int i = 0; i < len; i++) {
        	//notre index de glyphe commence à ' '
            int c = text.charAt(i) - ' ';
            //si char inconnu, on ne fait rien
            if(c < 0 || c > glyphs.length - 1) 
                continue;
            TextureRegion glyph = glyphs[c];
            batcher.drawSprite(x, y, glyphWidth, glyphHeight, glyph);
            x += glyphWidth;
        }
    }
}