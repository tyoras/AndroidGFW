package yoan.game.framework.modules.game;

import yoan.game.framework.modules.audio.AndroidAudio;
import yoan.game.framework.modules.audio.Audio;
import yoan.game.framework.modules.fileio.AndroidFileIO;
import yoan.game.framework.modules.fileio.FileIO;
import yoan.game.framework.modules.graphics.AndroidFastRenderView;
import yoan.game.framework.modules.graphics.AndroidGraphics;
import yoan.game.framework.modules.graphics.Graphics;
import yoan.game.framework.modules.input.AndroidInput;
import yoan.game.framework.modules.input.Input;
import yoan.game.framework.modules.screen.Screen;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

public abstract class AndroidGame extends Activity implements Game {
	/** Gestion du thread de rendu */
	AndroidFastRenderView renderView;
	/** Gestion des graphismes */
	Graphics graphics;
	/** Gestion des sons */
	Audio audio;
	/** Gestion des entrées */
	Input input;
	/** Gestion des fichiers */
	FileIO fileIO;
	/** Représentation de l'écran courant du jeu */
	Screen screen;
	/** Accès au blocage de la veille */
	WakeLock wakeLock;
	
	//Mode paysage
	//TODO [1280x800] constantes à modifier
	/** largeur fixée de l'affichage du jeu */
	public static final int TARGETED_WIDTH = 480;
	/** hauteur fixée de l'affichage du jeu */
	public static final int TARGETED_HEIGHT = 320;
	
	/**
	 * Création de l'activité du jeu
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//on met l'application en fullScreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		boolean isLandscape= getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		//définition de la taille du frameBuffer interne
		int frameBufferWidth= isLandscape ? TARGETED_WIDTH : TARGETED_HEIGHT;
		int frameBufferHeight= isLandscape ? TARGETED_HEIGHT : TARGETED_WIDTH;
		Bitmap frameBuffer= Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Config.RGB_565);
		//calcul des coefficient pour adapter le framebuffer à la taille réelle de l'écran
		float scaleX= (float) frameBufferWidth / getWindowManager().getDefaultDisplay().getWidth();
		float scaleY= (float) frameBufferHeight / getWindowManager().getDefaultDisplay().getHeight();
		renderView 	= new AndroidFastRenderView(this, frameBuffer);
		graphics	= new AndroidGraphics(getAssets(), frameBuffer);
		fileIO		= new AndroidFileIO(this);
		audio		= new AndroidAudio(this);
		input		= new AndroidInput(this, renderView, scaleX, scaleY);
		screen		= getStartScreen();
		setContentView(renderView);
		//blocage de la mise en veille
		PowerManager powerManager= (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock= powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
	}
	
	/**
	 * (Re)démarrage de l'activité du jeu
	 */
	@Override
	public void onResume(){
		super.onResume();
		wakeLock.acquire();
		screen.resume();
		//démarrage du thread de rendu
		renderView.resume();
	}
	
	/**
	 * Mise en pause de l'activité du jeu
	 */
	@Override
	public void onPause(){
		super.onPause();
		wakeLock.release();
		//arret du thread de rendu en premier car il peut accéder à l'instance courante de Screen
		renderView.pause();
		screen.pause();
		if(isFinishing()) screen.dispose();
	}
	
	/** 
	 * Accès au gestionnaire des entrées
	 * @return Input 
	 */
	public Input getInput(){
		return input;
	}

	/** 
	 * Accès au gestionnaire des fichiers
	 * @return FileIO 
	 */
	public FileIO getFileIO(){
		return fileIO;
	}

	/** 
	 * Accès au gestionnaire des graphismes
	 * @return Graphics 
	 */
	public Graphics getGraphics(){
		return graphics;
	}

	/** 
	 * Accès au gestionnaire des sons
	 * @return Audio 
	 */
	public Audio getAudio(){
		return audio;
	}
	
	/**
	 * Remplace l'écran courant du jeu par un nouvel écran
	 */
	public void setScreen(Screen screen){
		if(screen == null) throw new IllegalArgumentException("Screen must not be null");
		//arrêt de l'écran courant
		this.screen.pause();
		this.screen.dispose();
		//démarrage du nouvel écran
		screen.resume();
		screen.update(0);
		this.screen= screen;
	}
	
	/**
	 * Accès à l'écran courant du jeu
	 */
	public Screen getCurrentScreen() {
		return screen;
	}
}