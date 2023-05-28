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
    private static int jobID;

    // store server data
    private static ArrayList<server> serverList = new ArrayList<server>();
    private static String SCHDSeverType = "";
    private static int SCHDServerID;

    // main method
    public static void main(String[] args) throws Exception {
        client CLIENT = new client("localhost", 50000);
        CLIENT.handshake();
        CLIENT.schedule();
        CLIENT.quit();
        CLIENT.close();
    }
    // do the scheduling
    private void schedule() throws Exception {
        serverInput = initialJob;
        while (!serverInput.equals("NONE")) {
            storeParams();
            if (initialJob.contains("JOBN")) {
                getsAvail();
                sendToServer(
                        "SCHD " + jobID + " " + SCHDSeverType + " " + SCHDServerID);
                receivedFromServer();
            }
            sendToServer("REDY");
            receivedFromServer();
        }
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
    }

    private void storeParams() throws Exception {
        sendToServer("REDY");
        receivedFromServer();
        // save initial job for scheduling
        initialJob = serverInput;
        // store parameters from command (JOBN, JOBP, JCPL etc.)
        if (initialJob.contains("JOBN")) {
            String[] jobParams;
            jobParams = serverInput.split(" ");
            jobCore = Integer.parseInt(jobParams[4]);
            jobMemory = Integer.parseInt(jobParams[5]);
            jobDisk = Integer.parseInt(jobParams[6]);
            jobID = Integer.parseInt(jobParams[2]);
        }
    }

    private void getsAvail() throws Exception {
        int nRecs = 0;
        // send get avail to get server info
        sendToServer("GETS Avail " + jobCore + " " + jobMemory + " " + jobDisk);
        receivedFromServer();
        String[] DATAParams = serverInput.split(" ");
        nRecs = Integer.parseInt(DATAParams[1]);
        sendToServer("OK");
        // no available server, fallback to capable
        if (nRecs == 0) {
            getsCapable();
        } else {
            for (int i = 0; i < nRecs; i++) {
                receivedFromServer();
                server currServer = new server(serverInput);
                serverList.add(currServer);
            }
            sendToServer("OK");
            receivedFromServer();
            // find first server available for schedule
            server firstServer = getFirstServer();
            SCHDSeverType = firstServer.getServerType();
            SCHDServerID = firstServer.getServerID();
            serverList.clear();
        }
    }

    private void getsCapable() throws Exception {
        int nRecs = 0;
        // send get capable to get server info
        sendToServer("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
        receivedFromServer();
        receivedFromServer();
        String[] DATAParams = serverInput.split(" ");
        System.out.println("before storing nRecs " + serverInput);
        nRecs = Integer.parseInt(DATAParams[1]);
        sendToServer("OK");
        // loop to store server data
        for (int i = 0; i < nRecs; i++) {
            receivedFromServer();
            server currServer = new server(serverInput);
            serverList.add(currServer);
        }
        sendToServer("OK");
        receivedFromServer();
        // filter servers using custom best fit conditions
        server server = filterServers();
        SCHDSeverType = server.getServerType();
        SCHDServerID = server.getServerID();
        serverList.clear();
    }

    private server filterServers() throws Exception {
        float minFitness = Float.MAX_VALUE;
        int minCoreCount = Integer.MAX_VALUE;
        server returnServer = null;

        // loop through serverlist to filter invalid servers
        for (server currServer : serverList) {
            // calculate fitness
            float minCore = (float) currServer.getServerCore() / jobCore;
            float minMemory = (float) currServer.getServerMemory() / jobMemory;
            float minDisk = (float) currServer.getServerDisk() / jobDisk;
            float fitness = minCore + minMemory + minDisk;
            // fitness < 1 means server is invalid
            if (fitness >= 1) {
                if (fitness < minFitness) {
                    minFitness = fitness;
                    minCoreCount = currServer.getServerCore();
                    returnServer = currServer;
                } else if (fitness == minFitness) {
                    if (currServer.getServerCore() < minCoreCount) {
                        minCoreCount = currServer.getServerCore();
                        returnServer = currServer;
                    } else if (currServer.getServerCore() == minCoreCount
                            && currServer.getServerID() < returnServer.getServerID()) {
                        returnServer = currServer;
                    }
                }
            }
        }
        return returnServer;
    }

    // get first server in the arraylist for immediate scheduling
    public server getFirstServer() {
        if (serverList.isEmpty()) {
            return null;
        }
        server firstServer = serverList.get(0);
        return firstServer;
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
        System.out.println("server send: " + serverInput);
    }

    // send message to server
    private void sendToServer(String input) throws Exception {
        input = input + "\n";
        dout.write(input.getBytes());
        System.out.println("server recieve: " + input);
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
    private String serverState;
    private int waitingJob;
    private int runningJob;

    public server(String i) throws Exception {
        String[] input = i.split(" ");
        this.serverType = input[0];
        this.serverID = Integer.parseInt(input[1]);
        this.serverCore = Integer.parseInt(input[4]);
        this.serverMemory = Integer.parseInt(input[5]);
        this.serverDisk = Integer.parseInt(input[6]);
        this.serverState = (input[2]);
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

    public String getServerState() throws Exception {
        return serverState;
    }

    public int getWaitingJob() throws Exception {
        return waitingJob;
    }

    public int getRunningJob() throws Exception {
        return runningJob;
    }
}
