import java.net.*;
import java.io.*;
import java.util.*;

public class client {
    private static Socket s;
    private static DataOutputStream dout;
    private static BufferedReader din;

    //store server's message
    private static String serverInput = "";
    private static String initialJob = "";

    //store params from command JOBN, JOBP, JCPL etc.
    private static int jobCore;
    private static int jobMemory;
    private static int jobDisk;
    private static String[] params;
    private static String[] DATAParams;

    //store other data
    private static int nRecs;
    private static ArrayList<Integer> jobServerIDs = new ArrayList<Integer>();
    private static String biggestServer = "";
    private static int biggestSeverCoreCount = 0;


    // main method
    public static void main(String[] args) throws Exception {
        client CLIENT = new client("localhost", 50000);
        CLIENT.handshake();
        if (!serverInput.equals("NONE")) {
            CLIENT.storeParams();
            CLIENT.getnRecs();
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
        params = serverInput.split(" ");
        jobCore = Integer.parseInt(params[params.length - 3]);
        jobMemory = Integer.parseInt(params[params.length - 2]);
        jobDisk = Integer.parseInt(params[params.length - 1]);
    }

    private void getnRecs() throws Exception {
        // Send GETS Capable core memory disk
        sendToServer("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
        receivedFromServer();

        // store nRecs so we can store available server's data
        DATAParams = serverInput.split(" ");
        nRecs = Integer.parseInt(DATAParams[1]);

        // Send OK
        sendToServer("OK");
    }

    private void execAlgor() throws Exception {
        // loop to store server's data
        for (int i = 0; i < nRecs; i++) {
            receivedFromServer();
            String[] serverInfo = serverInput.split(" ");
            int serverCore = Integer.parseInt(serverInfo[4]);

            // Find the biggest server
            // TODO: untangle this else if codeblock
            if (serverCore > biggestSeverCoreCount) {
                biggestSeverCoreCount = serverCore;
                biggestServer = serverInfo[0];
                jobServerIDs = new ArrayList<Integer>();
                jobServerIDs.add(Integer.parseInt(serverInfo[1]));
            } else if (serverCore < biggestSeverCoreCount) {
                continue;
            } else {
                if (serverInfo[0].equals(biggestServer)) {
                    // Add the server ID to the list
                    jobServerIDs.add(Integer.parseInt(serverInfo[1]));
                } else {
                    continue;
                }
            }
        }

        // Send OK
        sendToServer("OK");
        receivedFromServer();

        int index = 0;
        serverInput = initialJob; // change input to intitial job for LRR loop
        while (!serverInput.equals("NONE")) {
            params = serverInput.split(" ");
            Integer jobID = Integer.parseInt(params[2]);

            // If the message contains JOBN, schedule job
            if (serverInput.contains("JOBN")) {
                sendToServer("SCHD " + jobID + " " + biggestServer + " " + jobServerIDs.get(index));
                index++;

                if (jobServerIDs.size() == index) {
                    index = 0;
                }
                receivedFromServer();
            }

            // Send REDY
            sendToServer("REDY");
            receivedFromServer();
        }
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