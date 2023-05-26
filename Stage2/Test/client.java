import java.net.*;
import java.io.*;
import java.util.*;

public class client {
    private static Socket s;
    private static DataOutputStream dout;
    private static BufferedReader din;

    private static String serverInput = "";

    // main method
    public static void main(String[] args) throws IOException {
        client CLIENT = new client("localhost", 50000);
        CLIENT.handshake();
        CLIENT.execAlgor();
        CLIENT.quit();
        CLIENT.close();
    }

    // init connection to server
    private client(String host, int port) throws IOException {
        s = new Socket(host, port);
        dout = new DataOutputStream(s.getOutputStream());
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    // start handshaking with ds-client
    private void handshake() throws IOException {
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

    private void execAlgor() throws IOException {
        if (!serverInput.equals("NONE")) {
            String initialJob = serverInput; // save initial job for scheduling

            // store parameters from command (JOBN, JOBP, JCPL etc.)
            String[] params = serverInput.split(" ");
            int jobCore = Integer.parseInt(params[params.length - 3]);
            int jobMemory = Integer.parseInt(params[params.length - 2]);
            int jobDisk = Integer.parseInt(params[params.length - 1]);

            // Send GETS Capable core memory disk
            sendToServer("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
            receivedFromServer();

            // store nRecs so we can store available server's data
            String[] DATAparams = serverInput.split(" ");
            int nRecs = Integer.parseInt(DATAparams[1]);

            // Send OK
            sendToServer("OK");

            String biggestServer = "";
            int biggestSeverCoreCount = 0;
            ArrayList<Integer> jobServerIDs = new ArrayList<Integer>();

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
    }

    private void quit() throws IOException{
                // Send QUIT
                sendToServer("QUIT");
                receivedFromServer();
    }

    private void close() throws IOException {
        // Close the socket and streams
        din.close();
        dout.close();
        s.close();
    }
    // print an store received message from server
    private void receivedFromServer() throws IOException {
        serverInput = din.readLine();
        System.out.println("server said: " + serverInput);
    }

    // send message to server
    private void sendToServer(String input) throws IOException {
        input = input + "\n";
        dout.write(input.getBytes());
        System.out.println("sent to server: " + input);
        dout.flush();
    }
}