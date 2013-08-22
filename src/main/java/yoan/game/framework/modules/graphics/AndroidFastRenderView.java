package yoan.game.framework.modules.graphics;

import yoan.game.framework.modules.game.AndroidGame;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * View permettant d'effectuer le rendu dans un autre thread que le UI thread
 * @author yoan
 */
public class AndroidFastRenderView extends SurfaceView implements Runnable {
	/** accès au jeu */
	AndroidGame game;
	/** le frameBuffer interne */
	Bitmap framebuffer;
	/** le thread de rendu du jeu */
	Thread renderThread= null;
	/** gestionnaire de Surface */
	SurfaceHolder holder;
	/** indique si le thread de rendu est actif */
	volatile boolean running= false;
	
	/**
	 * Constructeur
	 * @param game : l'instance du jeu
	 * @param framebuffer : le framebuffer interne à afficher
	 */
	public AndroidFastRenderView(AndroidGame game, Bitmap framebuffer){
		super(game);
		this.game= game;
		this.framebuffer= framebuffer;
		this.holder= getHolder();
	}
	
	public AndroidFastRenderView(Context context){
		super(context);
	}
	
	/**
	 * Lance le thread de rendu
	 */
	public void resume(){
		running= true;
		renderThread= new Thread(this);
		renderThread.start();
	}
	
	/**
	 * Traitement du thread de rendu
	 */
	public void run(){
		Rect dstRect= new Rect();
		//temps au début de la frame de rendu
		long startTime= System.nanoTime();
		//boucle de rendu
		while(running){
			//on ne fait rien tant que la Surface n'est pas disponible
			if(!holder.getSurface().isValid()) continue;
			//durée de la précédente frame de rendu
			float deltaTime= (System.nanoTime() - startTime) / 1000000000.0f;
			startTime= System.nanoTime();
			//on transmet l'info au moteur de jeu
			game.getCurrentScreen().update(deltaTime);
			game.getCurrentScreen().present(deltaTime);
			//on récupère un canvas pour effectuer le rendu à l'écran
			Canvas canvas= holder.lockCanvas();
			//on récupère les dimensions réelles de l'écran
			canvas.getClipBounds(dstRect);
			//et on les applique au moment de dessiner le frame buffer interne dans celui de l'écran
			//on adapte automatiquement le frameBuffer interne aux dimensions réelles de l'écran
			canvas.drawBitmap(framebuffer, null, dstRect, null);
			//on affiche à l'écran
			holder.unlockCanvasAndPost(canvas);
		}
	}
	
	/**
	 * Stop le thread de rendu
	 */
	public void pause(){
		running= false;
		//on attend que le thread soit bien arrêté
		while(true){
			try{
				renderThread.join();
				return;
			}catch(InterruptedException e){
				// retry
			}
		}
	}
}