package yoan.game.framework.modules.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author yoan
 */
public class BlueToothService {
	// Debugging
    private static final String TAG = "BlueToothService";
    private static final boolean D = true;

    /** Nom de la server socket pour le SDP */
    private static final String SDP_NAME = "GameFrameworkBlueToothManager";

    /** UUID pour les applications basées sur ce framework */
    private static final UUID ANDROID_GAME_FRAMEWORK_UUID = UUID.fromString("bd4c53d2-c32c-4881-bfd6-eec816471d51");
    
    /** Gestionnaire android de bluetooth */
    private final BluetoothAdapter adapter;
    /** Handler vers le thread appelant */
    private final Handler handler;
    /** Thread d'attente de connexion avec un autre device */
    private AcceptThread acceptThread;
    /** Thread de connexion avec un autre device */
    private ConnectThread connectionThread;
    /** Thread de communication une fois la connexion effectuée*/
    private CommunicationThread communicationThread;
    /** Nom du device connecté */
    String connectedDeviceName;
    /** état actuel de la connexion */
    private int currentState;

    /* Etats de la connexion */
    /** Aucune action en cours */
    public static final int STATE_NONE = 0;       
    /** En attente de connexion */
    public static final int STATE_LISTEN = 1;     
    /** Connexion en cours */
    public static final int STATE_CONNECTING = 2; 
    /** Communication possible */
    public static final int STATE_CONNECTED = 3; 

    /**
     * Constructeur d'initialisation du service
     * @param handler : handler pour communiquer avec le thread appelant
     */
    public BlueToothService(Handler handler) {
    	adapter = BluetoothAdapter.getDefaultAdapter();
    	currentState = STATE_NONE;
        this.handler = handler;
        connectedDeviceName = null;
    }

    /**
     * Change l'état courant et prévient le thread appelant
     * @param newState : nouvel état
     */
    private synchronized void setState(int newState) {
        if(D) Log.d(TAG, "setState() " + stateToString(currentState) + " -> " + stateToString(newState));
        currentState = newState;

        // prévient le thread appelant du changement
        handler.obtainMessage(AndroidBlueTooth.MESSAGE_STATE_CHANGE, currentState, -1).sendToTarget();
    }
    
    private String stateToString(int state) {
    	switch(state) {
    		case STATE_NONE :
    			return "Rien";
    		case STATE_LISTEN :
    			return "En attente";
    		case STATE_CONNECTING :
    			return "Connexion";
    		case STATE_CONNECTED :
    			return "Communication";
    		default :
    			return "Etat inconnu";
    	}
    }

    /**
     * @return état courant du service
     */
    public synchronized int getState() {
        return currentState;
    }

    /**
     * Démarre le service en mode Attente de connexion
     * */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Arret des autres opérations en cours
        if (connectionThread != null) {connectionThread.cancel(); connectionThread = null;}
        if (communicationThread != null) {communicationThread.cancel(); communicationThread = null;}

        setState(STATE_LISTEN);

        // démarrage du thread d'attente de connexion
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    /**
     * Démarre le thread qui effectue la connexion
     * @param device  le device sur lequel on se connecte
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connexion à: " + device);

        // Arret des autres opérations en cours
        if (currentState == STATE_CONNECTING) {
            if (connectionThread != null) {connectionThread.cancel(); connectionThread = null;}
        }
        if (communicationThread != null) {communicationThread.cancel(); communicationThread = null;}

        // démarrage du thread de connexion
        connectionThread = new ConnectThread(device);
        connectionThread.start();
        
        setState(STATE_CONNECTING);
    }

    /**
     * Démarre le thread de communication entre les devices connectés
     * @param socket : socket de la connexion bluetooth
     * @param device : le device connecté
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connecté à " + device.getName());

        // Arret des autres opérations en cours
        if (connectionThread != null) {connectionThread.cancel(); connectionThread = null;}
        if (communicationThread != null) {communicationThread.cancel(); communicationThread = null;}
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // démarrage du thread de communication
        communicationThread = new CommunicationThread(socket);
        communicationThread.start();

        // On renvoie le nom du device connecté au thread appelant
        Message msg = handler.obtainMessage(AndroidBlueTooth.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(AndroidBlueTooth.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
        connectedDeviceName = device.getName();
    }

    /**
     * Stop les threads du service
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
        }

        if (communicationThread != null) {
            communicationThread.cancel();
            communicationThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        setState(STATE_NONE);
        connectedDeviceName = null;
    }

    /**
     * Demande asynchrone d'envoi de message via le thread de communication
     * @param out : bytes à envoyer
     * @see CommunicationThread#write(byte[])
     */
    public void write(byte[] out) {
        CommunicationThread comThread;
        // copie synchronisée du thread
        synchronized (this) {
            if (currentState != STATE_CONNECTED) return;
            comThread = communicationThread;
        }
        comThread.write(out);
    }

    /**
     * Indique qu'une connexion a échoué et redémmare le service
     */
    private void connectionFailed() {
        //on informe le thread appelant
        Message msg = handler.obtainMessage(AndroidBlueTooth.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AndroidBlueTooth.TOAST, "Connexion echouée");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // redémarrage du service
        BlueToothService.this.start();
    }

    /**
     * Indique que la connexion est perdue
     */
    private void connectionLost() {
    	//on informe le thread appelant
        Message msg = handler.obtainMessage(AndroidBlueTooth.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AndroidBlueTooth.TOAST, "Connexion perdue");
        msg.setData(bundle);
        connectedDeviceName = null;
        handler.sendMessage(msg);

        // redémarrage du service
        BlueToothService.this.start();
    }

    /**
     * Thread d'attente de connexion
     */
    private class AcceptThread extends Thread {
        /** socket d'écoute */
        private final BluetoothServerSocket listenSocket;
        
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // création de la socket sur le serveur local
            try {
            	tmp = adapter.listenUsingRfcommWithServiceRecord(SDP_NAME, ANDROID_GAME_FRAMEWORK_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
            }
            listenSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Début du thread d'écoute de connexion " + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // tant qu'on est pas connecté
            while (currentState != STATE_CONNECTED) {
                try {
                    //appel bloquant tant qu'il n'y a pas de connexion ou d'erreur
                    socket = listenSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket accept() failed", e);
                    break;
                }

                // si on a récupérer une socket de connexion
                if (socket != null) {
                    synchronized (BlueToothService.this) {
                        switch (currentState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal => on démarre le thread de connexion
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Soit pas encore pret, soit déjà connecté
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "Fin du thread d'écoute de connexion");

        }

        /** Arret de la socket d'écoute */
        public void cancel() {
            if (D) Log.d(TAG, "Socket cancel " + this);
            try {
            	listenSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
            }
        }
    }


    /**
     * Thread de connexion
     * Il établit la connexion entre les devices à partir de la socket récupérée
     */
    private class ConnectThread extends Thread {
    	/** socket de communication avec le device */
        private final BluetoothSocket socket;
        /** device à connecter */
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;

            //création de la socket de communication
            try {
                tmp = device.createRfcommSocketToServiceRecord(ANDROID_GAME_FRAMEWORK_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            socket = tmp;
        }

        public void run() {
            Log.i(TAG, "Début du thread de connexion");
            setName("ConnectThread");

            //Arret du dicovery car ça ralentit la communication
            adapter.cancelDiscovery();

            // on établit la connexion à la socket
            try {
            	//appel bloquant tant qu'il n'y a pas de connexion ou d'erreur
            	socket.connect();
            } catch (IOException e) {
                // fermeture de la socket
                try {
                	socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Arret du thread de connexion
            synchronized (BlueToothService.this) {
                connectionThread = null;
            }

            // on lance le thread de communication
            connected(socket, device);
        }

        /** Arret de la socket */
        public void cancel() {
            try {
            	socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * Thread de communication
     * Gère les entrées/sortie sur la socket de communication
     */
    private class CommunicationThread extends Thread {
    	/** Socket de communication */
        private final BluetoothSocket comSocket;
        /** Flux d'entrée sur la socket */
        private final InputStream inStream;
        /** Flux de sortie sur la socket */
        private final OutputStream outStream;

        public CommunicationThread(BluetoothSocket socket) {
            Log.d(TAG, "Création du thread de communication");
            comSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Récupération des flux d'entrée/sortie
            try {
            	tmpIn = comSocket.getInputStream();
                tmpOut = comSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "Début du thread de communication");
            byte[] buffer = new byte[1024];
            int bytes;

            // écoute tant qu'on est connecté
            while (true) {
                try {
                    //lecture du flux d'entrée
                    bytes = inStream.read(buffer);
                    Log.w(TAG, "lu : " + new String(buffer, 0, bytes));
                    // envoie du message au thread appelant
                    handler.obtainMessage(AndroidBlueTooth.MESSAGE_READ, bytes, -1, buffer)
                           .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "déconnecté", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Ecrit sur le flux de sortie de la socket
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
            	outStream.write(buffer);
                Log.i(TAG, buffer.toString());
                // on prévient le thread appelant que le message est envoyé
                handler.obtainMessage(AndroidBlueTooth.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /** Arret de la socket */
        public void cancel() {
            try {
            	comSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}