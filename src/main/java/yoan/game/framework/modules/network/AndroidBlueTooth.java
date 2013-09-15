package yoan.game.framework.modules.network;

import java.lang.ref.WeakReference;

import yoan.game.framework.R;
import yoan.game.framework.modules.game.gl.GLGame;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * 
 * @author yoan
 */
public class AndroidBlueTooth implements BlueTooth {
	// Types des messages renvoyés par le BlueTooth manager
	/** Changement d'état du BlueTooth */
    public static final int MESSAGE_STATE_CHANGE = 1;
    /** Leture d'un message */
    public static final int MESSAGE_READ = 2;
    /** Envoi d'un message */
    public static final int MESSAGE_WRITE = 3;
    /** Demande du nom de l'appareil connecté */
    public static final int MESSAGE_DEVICE_NAME = 4;
    /** Affichage d'un message */
    public static final int MESSAGE_TOAST = 5;

    // Identifiants des infos présentes dans les messages renvoyés par le BlueTooth manager
    /** Nom de l'appareil */
    public static final String DEVICE_NAME = "device_name";
    /** Message à afficher */
    public static final String TOAST = "toast";

    /** Weak reference vers l'activité du jeu */
    private final WeakReference<GLGame> gameActivityRef;
    /** Handler vers le thread de rendu openGL */
    private final Handler glThreadHandler;
    
    /** Dernier message reçu */
    private static String lastMsg;
    
    /** Gestionnaire android de Blue Tooth */
    private BluetoothAdapter bluetoothAdapter = null;
    /** Gestionnaire d'accès aux thread du BlueTooth */
    private BlueToothService blueToothService;
    
    /**
     * Handler permettant de récupérer les retour du BlueTooth manager
     * @author yoan
     */
    private static class GLThreadHandler extends Handler {
    	 /** Weak reference vers l'activité du jeu sur lequel repose le handler */
    	private final WeakReference<GLGame> RefToGameActivity;
    	 
    	/** Constructeur pour récupérer la référence */
        public GLThreadHandler(GLGame gameActivity) {
        	super(gameActivity.getMainLooper());
        	this.RefToGameActivity = new WeakReference<GLGame>(gameActivity);
        }
        
        @Override
        public void handleMessage(Message msg) {
        	Activity gameActivity = RefToGameActivity.get();
        	if (gameActivity != null) {
                switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                    case BlueToothService.STATE_CONNECTED:
                        break;
                    case BlueToothService.STATE_CONNECTING:
                        break;
                    case BlueToothService.STATE_LISTEN:
                    case BlueToothService.STATE_NONE:
                        break;
                    }
                    break;
                case MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // récupération du dernier message
                    lastMsg = new String(readBuf, 0, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // Affichage du nom du device connecté
                    Toast.makeText(gameActivity.getApplicationContext(), "Connecté à " + msg.getData().getString(DEVICE_NAME), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(gameActivity.getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
                }
        	}
        }
    }
    
    public AndroidBlueTooth(GLGame gameActivity, BluetoothAdapter bluetoothAdapter) {
    	this.gameActivityRef = new WeakReference<GLGame>(gameActivity);
    	//mise en place du handler sur le thread appelant
    	glThreadHandler = new GLThreadHandler(gameActivity);
    	this.bluetoothAdapter = bluetoothAdapter;
    }
    
    /**
     * Prépare l'utilisation du Bluetooth
     */
    @Override
    public void setupBlueTooth() {
    	if (this.gameActivityRef != null) {
    		GLGame glGame = gameActivityRef.get();
            
            //vérification du support du bluetooth
            if (bluetoothAdapter == null) {
                Toast.makeText(glGame, "Bluetooth non disponible", Toast.LENGTH_LONG).show();
                gameActivityRef.get().finish();
                return;
            }
            
            //demande d'activation si nécessaire
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                glGame.startActivityForResult(enableIntent, GLGame.REQUEST_ENABLE_BT);
                return;
            } else {
        		blueToothService = new BlueToothService(glThreadHandler);
        		// si le service n'est pas déjà démarré
        		if (blueToothService != null && blueToothService.getState() == BlueToothService.STATE_NONE) {
                  	// on le démarre
                	blueToothService.start();
                }
            }
    	}
    }
	
    /**
     * Stop l'utilisation du bluetooth
     */
    @Override
    public void stop() {
        if (blueToothService != null) blueToothService.stop();
    }

    /**
     * Lance la recherche et le choix de l'appareil
     */
    @Override
    public void searchDevice() {
    	Intent serverIntent = new Intent(gameActivityRef.get(), DeviceListActivity.class);
    	gameActivityRef.get().startActivityForResult(serverIntent, GLGame.REQUEST_CONNECT_DEVICE);
    }
    
    /**
     * Effectue la connexion avec un appareil donnée par son adresse MAC
     * @param macAdress : adresse MAC sur laquelle on veut se connecter
     */
    @Override
    public void connectDevice(String macAdress) {
        //récupération des infos du device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAdress);
        //connexion
        blueToothService.connect(device);
    }

    /**
     * Indique si le Bluetooth est en cours d'utilisation
     */
	@Override
	public boolean isRunning(){
		return blueToothService != null;
	}

	/**
	 * Fournit le nom du device connecté s'il y en a un
	 */
	@Override
	public String getConnectedDeviceName(){
		return blueToothService != null ? blueToothService.connectedDeviceName : null;
	}
	
	/**
	 * Récupération du dernier message reçu
	 */
	@Override
	public String getLastMsg() {
		return lastMsg;
	}
	
	/**
	 * Ecrit un message vers l'appareil connecté
	 */
	@Override
	public void write(String msg) {
		// on vérfie qu'on est bien connecté
        if (blueToothService.getState() != BlueToothService.STATE_CONNECTED) {
            Toast.makeText(gameActivityRef.get(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // s'il y a un message
        if (msg.length() > 0) {
            byte[] msgBytes = msg.getBytes();
            blueToothService.write(msgBytes);
        }
	}
}