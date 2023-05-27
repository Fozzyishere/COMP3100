import java.net.*;
import java.io.*;
import java.util.*;

public class client {
    private static Socket s;
    private static DataOutputStream dout;
    private static BufferedReader din;

    // store server's message
    private static String serverInput = "";
    private static String initialJob = "";

    // store params from command JOBN, JOBP, JCPL etc.
    private static int jobCore;
    private static int jobMemory;
    private static int jobDisk;

    // store other data (server info, other vars, etc.)
    private static int nRecs;
    private static ArrayList<server> serverList = new ArrayList<server>();

    // main method
    public static void main(String[] args) throws Exception {
        client CLIENT = new client("localhost", 50000);
        CLIENT.handshake();
        if (!serverInput.equals("NONE")) {
            CLIENT.storeParams();
            CLIENT.getCapable();
            CLIENT.filterServers(jobCore, jobMemory, jobDisk);
            CLIENT.execAlgor();
        }
        CLIENT.quit();
        CLIENT.close();
    }

    // init connection to server
    private client(String host, int port) throws Exception {
        s = new Socket(host, port);
        dout = new DataOutputStream(s.getOutputStream());
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    // start handshaking with ds-client
    private void handshake() throws Exception {
        // Send HELO
        sendToServer("HELO");
        receivedFromServer();

        // Send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        receivedFromServer();

        // Send REDY
        sendToServer("REDY");
        receivedFromServer();
    }

    private void storeParams() throws Exception {
        // save initial job for scheduling
        initialJob = serverInput;
        // store parameters from command (JOBN, JOBP, JCPL etc.)
        String[] params;
        params = serverInput.split(" ");
        jobCore = Integer.parseInt(params[4]);
        jobMemory = Integer.parseInt(params[5]);
        jobDisk = Integer.parseInt(params[6]);
    }

    private void getCapable() throws Exception {
        // send get capable to get server info
        sendToServer("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
        receivedFromServer();
        String[] DATAParams;
        DATAParams = serverInput.split(" ");
        nRecs = Integer.parseInt(DATAParams[1]);
        sendToServer("OK");

        // loop to store server data
        for (int i = 0; i < nRecs; i++) {
            receivedFromServer();
            server currServer = new server(serverInput);
            serverList.add(currServer);
        }
    }
    

    private server filterServers(int jobCore, int jobMemory, int jobDisk) throws Exception {
        /*
         * filtering parameters:
         * 1. sufficient resources (handled by GETS Capable)
         * 2. No running job and wating job existing at the same time
         * 3. fitness value (f) is smallest
         * 
         * fitness value can be calculated by: required/capable
         * required can be acquired through JOBN
         * capable can be accquired through GETS
         * 
         */

        server returnServer = null;;
        float prevFitness = Float.MAX_VALUE;
        for (int i = 0; i < nRecs; i++) {
            returnServer = serverList.get(i);
            float currFitness = (float) jobCore/serverList.get(i).getServerCore();
            if(returnServer.getWaitingJob() != returnServer.getRunningJob() && currFitness < prevFitness){
                returnServer = serverList.get(i);
                prevFitness = currFitness;
            }
        }
        return returnServer;
    }

    // do the algorithm
    private void execAlgor() throws Exception {

    }

    private void quit() throws Exception {
        // Send QUIT
        sendToServer("QUIT");
        receivedFromServer();
    }

    private void close() throws Exception {
        // Close the socket and streams
        din.close();
        dout.close();
        s.close();
    }

    // print an store received message from server
    private void receivedFromServer() throws Exception {
        serverInput = din.readLine();
        System.out.println("server said: " + serverInput);
    }

    // send message to server
    private void sendToServer(String input) throws Exception {
        input = input + "\n";
        dout.write(input.getBytes());
        System.out.println("sent to server: " + input);
        dout.flush();
    }
}

// Class to help manage server info easier
class server {
    private String serverType;
    private int serverID;
    private int serverCore;
    private int serverMemory;
    private int serverDisk;
    private int serverState;
    private int waitingJob;
    private int runningJob;

    public server(String i) throws Exception {
        String[] input = i.split(" ");
        this.serverType = input[0];
        this.serverID = Integer.parseInt(input[1]);
        this.serverCore = Integer.parseInt(input[4]);
        this.serverMemory = Integer.parseInt(input[5]);
        this.serverDisk = Integer.parseInt(input[6]);
        this.serverState = Integer.parseInt(input[2]);
        this.waitingJob = Integer.parseInt(input[7]);
        this.runningJob = Integer.parseInt(input[8]);
    }

    /* Getters */
    public String getServerType() throws Exception {
        return serverType;
    }

    public int getServerCore() throws Exception {
        return serverCore;
    }

    public int getServerID() throws Exception {
        return serverID;
    }

    public int getServerMemory() throws Exception {
        return serverMemory;
    }

    public int getServerDisk() throws Exception {
        return serverDisk;
    }

    public int getServerState() throws Exception {
        return serverState;
    }
    public int getWaitingJob() throws Exception {
        return waitingJob;
    }
    public int getRunningJob() throws Exception {
        return runningJob;
    }
}
