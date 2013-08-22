package yoan.game.framework.modules.game.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import yoan.game.framework.modules.audio.AndroidAudio;
import yoan.game.framework.modules.audio.Audio;
import yoan.game.framework.modules.fileio.AndroidFileIO;
import yoan.game.framework.modules.fileio.FileIO;
import yoan.game.framework.modules.game.Game;
import yoan.game.framework.modules.graphics.Graphics;
import yoan.game.framework.modules.graphics.gl.GLGraphics;
import yoan.game.framework.modules.input.AndroidInput;
import yoan.game.framework.modules.input.Input;
import yoan.game.framework.modules.screen.Screen;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

/**
 * 
 * @author yoan
 */
public abstract class GLGame extends Activity implements Game, Renderer {
	/** Enum des états du jeu */
	enum GLGameState{
		/** le jeu est initialisé */
		Initialized, 
		/** la partie est en cours */
		Running, 
		/** la partie est en pause */
		Paused, 
		/** la partie est finie */
		Finished, 
		/** le jeu est en veille */
		Idle
	}

	/** Gestion Android du thread de rendu OpenGL ES*/
	GLSurfaceView glView;
	/** Gestion des graphismes via OpenGL ES */
	GLGraphics glGraphics;
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
	/** Etat courant du jeu */
	GLGameState state= GLGameState.Initialized;
	/** Objet de synchronisation du thread de rendu et de l'UI lors d'un changment d'état */
	Object stateChanged= new Object();
	/** Temps au démarrage du jeu */
	long startTime= System.nanoTime();
	
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
		//mise en place de la vue
		glView= new GLSurfaceView(this);
		glView.setRenderer(this);
		setContentView(glView);
		glGraphics= new GLGraphics(glView);
		fileIO= new AndroidFileIO(this);
		audio= new AndroidAudio(this);
		//pas de scaling pour l'input
		input= new AndroidInput(this, glView, 1, 1);
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
		//démarrage du thread de rendu
		glView.onResume();
		wakeLock.acquire();
	}
	
	/**
	 * Appelé à la création de la surface GL
	 * @param gl : l'instance courante de GL10
	 * @param config : la config actuelle d'OpenGL ES
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config){
		//on garde l'instance de GL10
		glGraphics.setGL(gl);
		//on verrouille le changement d'état 
		synchronized(stateChanged){
			//si le jeu initialisé on va sur l'écran de démarrage
			if(state == GLGameState.Initialized) screen= getStartScreen();
			//transition vers l'état Running
			state= GLGameState.Running;
			//initialisation de l'écran
			screen.resume();
			startTime= System.nanoTime();
		}
	}
	
	/**
	 * Appelé si la taille de la vue change
	 * @param gl : l'instance courante de GL10
	 * @param width : nouvelle largeur de la vue
	 * @param height : nouvelle hauteur de la vue
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) { /* inutilisé */ }
	
	/**
	 * Appelé à chaque frame du thread de rendu
	 * Gère en fonction de l'état du jeu
	 * @param gl : l'instance courante de GL10
	 */
	public void onDrawFrame(GL10 gl){
		GLGameState state= null;
		// l'état peut aussi être accéder par l'UI thread dans onPause, donc synchro
		synchronized(stateChanged){
			state= this.state;
		}
		//gestion en focntion de l'état courant
		switch (state){
			case Running : 
				//durée de la précédente frame de rendu
				float deltaTime= (System.nanoTime() - startTime) / 1000000000.0f;
				startTime= System.nanoTime();
				//on transmet l'info au moteur de jeu
				screen.update(deltaTime);
				screen.present(deltaTime);
				break;
			case Paused :
				//on met l'écran en pause
				screen.pause();
				//et on change l'état du jeu
				synchronized(stateChanged){
					this.state= GLGameState.Idle;
					stateChanged.notifyAll();
				}
				break;
			case Finished :
				//arret de l'écran
				screen.pause();
				screen.dispose();
				//et on change l'état du jeu
				synchronized(stateChanged){
					this.state= GLGameState.Idle;
					stateChanged.notifyAll();
				}
				break;
			default :
				break;
		}
	}
	
	/**
	 * Mise en pause de l'activité du jeu
	 */
	@Override
	public void onPause(){
		//on verrouille le changement d'état 
		synchronized(stateChanged){
			//si l'activité se finit le jeu est fini
			if(isFinishing()) state= GLGameState.Finished;
			//sinon en pause
			else state= GLGameState.Paused;
			//on attend une notification indiquant que le thread de rendu a bien géré le nouvel état avant de faire la suite
			while(true){
				try{
					stateChanged.wait();
					break;
				}catch(InterruptedException e){}
			}
		}
		wakeLock.release();
		glView.onPause();
		super.onPause();
	}
	
	/** 
	 * Accès au gestionnaire des graphismes openGL ES
	 * @return Graphics 
	 */
	public GLGraphics getGLGraphics() {
		return glGraphics;
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
		throw new IllegalStateException("We are using OpenGL!");
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