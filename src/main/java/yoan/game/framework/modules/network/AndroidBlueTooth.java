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
    private final WeakReference<GLGame> gameActivity;
    /** Handler vers le thread de l'UI */
    private final Handler uiHandler;

    // String buffer for outgoing messages
    private StringBuffer outStringBuffer;
    /** Gestionnaire android de Blue Tooth */
    private BluetoothAdapter bluetoothAdapter = null;
    /** Gestionnaire d'accès aux thread du BlueTooth */
    private BlueToothService blueToothService;
    
    /**
     * Handler permettant de récupérer les retour du Blue Tooth manager
     * @author yoan
     */
    private static class UIHandler extends Handler {
    	 /** Weak reference vers l'activité du jeu sur lequel repose le handler */
    	private final WeakReference<GLGame> RefToGameActivity;
    	 
    	/** Constructeur pour récupérer la référence */
        public UIHandler(GLGame gameActivity) {
        	super(gameActivity.getMainLooper());
        	this.RefToGameActivity = new WeakReference<GLGame>(gameActivity);
        }
        
        
        
        //TODO Trouver comment utiliser ce handler!!
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
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // Affichage du nom du device connecté
                    Toast.makeText(gameActivity.getApplicationContext(), "Connected to " + msg.getData().getString(DEVICE_NAME), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(gameActivity.getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
                }
        	}
        }
    }
    
    public AndroidBlueTooth(GLGame gameActivity, BluetoothAdapter bluetoothAdapter) {
    	this.gameActivity = new WeakReference<GLGame>(gameActivity);;
    	uiHandler = new UIHandler(gameActivity);
    	this.bluetoothAdapter = bluetoothAdapter;
    }
    
    /**
     * Prépare l'utilisation du Bluetooth
     */
    @Override
    public void setupBlueTooth() {
    	if (this.gameActivity != null) {
    		GLGame glGame = gameActivity.get();
            
            // If the adapter is null, then Bluetooth is not supported
            if (bluetoothAdapter == null) {
                Toast.makeText(glGame, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                gameActivity.get().finish();
                return;
            }
            
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                glGame.startActivityForResult(enableIntent, GLGame.REQUEST_ENABLE_BT);
                return;
            // Otherwise, setup the chat session
            } else {
            	 // Initialize the BluetoothChatService to perform bluetooth connections
        		blueToothService = new BlueToothService(uiHandler);
            	// Performing this check in onResume() covers the case in which BT was
                // not enabled during onStart(), so we were paused to enable it...
                // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
                if (blueToothService != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (blueToothService.getState() == BlueToothService.STATE_NONE) {
                      // Start the Bluetooth chat services
                    	blueToothService.start();
                    }
                }
        
                // Initialize the buffer for outgoing messages
                outStringBuffer = new StringBuffer("");
            }
            
    	}
    }
	
    @Override
    public void stop() {
        // Stop the Bluetooth
        if (blueToothService != null) blueToothService.stop();
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (blueToothService.getState() != BlueToothService.STATE_CONNECTED) {
            Toast.makeText(gameActivity.get(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            blueToothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);
        }
    }
	
    /**
     * Lance la recherche et le choix de l'appareil
     */
    @Override
    public void searchDevice() {
    	Intent serverIntent = new Intent(gameActivity.get(), DeviceListActivity.class);
    	gameActivity.get().startActivityForResult(serverIntent, GLGame.REQUEST_CONNECT_DEVICE);
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
}