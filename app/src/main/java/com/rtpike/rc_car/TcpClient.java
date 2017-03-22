package com.rtpike.rc_car;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

    /*


    ! + cmd
    * + misc command


    Write to pins
    L + pin
    H + pin

    Input/Output mode, pins
    O + pin
    I + pin

    Read digital input
    R + pin

    Read analog pin
    A + pin

    */


/**
 * Description
 *
 * @author Catalin Prata
 *         Date: 2/12/13
 */
public class TcpClient {

    public class Constants {

        public static final String CLOSED_CONNECTION = "!";// kazy_closed_connection";
        public static final String LOGIN_NAME = "!";  //kazy_login_name";

    }

    Socket socket;
    private Handler mHandler;

    public static String SERVER_IP = "myEddy"; //your computer IP address
    public static int SERVER_PORT = 3300;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private static final int TIMEOUT  = 10000;  //10 sec

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public TcpClient(Handler mHandler, OnMessageReceived listener) {
        this.mHandler  = mHandler;
        this.mMessageListener = listener;
    }

    public TcpClient(OnMessageReceived listener, String SERVER_IP,int SERVER_PORT) {
        mMessageListener = listener;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public TcpClient(Handler mHandler, OnMessageReceived listener, String SERVER_IP,int SERVER_PORT) {
        this.mHandler  = mHandler;
        this.mMessageListener = listener;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }


    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send mesage that we are closing the connection
        //sendMessage(Constants.CLOSED_CONNECTION);

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;

    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);



            Log.d("TCP", "C: Connecting to " + serverAddr.toString() + " ...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);
            //socket.setSoTimeout(TIMEOUT);

            try {


                mHandler.sendEmptyMessage(MainActivity.CONNECTED);
                Log.d("TCP","Is the socket connected: " +  socket.isConnected());

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // send login name
                //sendMessage(Constants.LOGIN_NAME); //FIXME + PreferencesManager.getInstance().getUserName());

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                        mServerMessage = mBufferIn.readLine();

                        if (mServerMessage != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            mMessageListener.messageReceived(mServerMessage);
                        }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (IOException e) {
                Log.e("TCP", "S: IOException", e);
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
                mHandler.sendEmptyMessage(MainActivity.CONNECT_ERROR);
                e.printStackTrace();

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.


                socket.close();
                mHandler.sendEmptyMessage(MainActivity.DISCONNECTED);
            }

        } catch (Exception e) {
            mHandler.sendEmptyMessage(MainActivity.CONNECT_ERROR);
            Log.e("TCP", "C: Error", e);

        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }


    public boolean isConnected() {
        if (socket != null) {
            return socket.isConnected();
        } else
            return false;
    }

    public InetAddress getInetAddr() {
        return socket.getInetAddress();
    }

}
