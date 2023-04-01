import java.net.*;
import java.io.*;
import java.util.*;

public class c {
    static String serverInput;
    static DataOutputStream dout;
    static BufferedReader din;

    public static void main(String[] args) throws UnknownHostException, IOException {
        // socket, input and output setup
        Socket s = new Socket("localhost", 50000);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        dout = new DataOutputStream(s.getOutputStream());

        // send HELO
        sendToServer("HELO");
        receivedFromServer();

        // send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        receivedFromServer();

        // send REDY
        sendToServer("REDY");
        receivedFromServer();

        // if jobs to schedule available
        if (!serverInput.equals("NONE")) {
            String initialJob = serverInput; // save initial job for LRR scheduling

            // Store data from either JOBN, JOBP or JCPL
            String[] jobParams = serverInput.split(" ");
            int jobCores = Integer.parseInt(jobParams[4]);
            int jobMemory = Integer.parseInt(jobParams[5]);
            int jobDisk = Integer.parseInt(jobParams[6]);

            // send GETS
            sendToServer("GETS Capable " + jobCores + " " + jobMemory + " " + jobDisk);
            receivedFromServer();
            String[] DATAParams = serverInput.split(" "); // get nRecs for the loop to get server
            int nRecs = Integer.parseInt(DATAParams[1]);

            // send OK
            sendToServer("OK"); // not execute receive method because that will be used in later loop

            String biggestServer = "";
            int biggestCoreCount = 0;
            ArrayList<Integer> jobServerIDs = new ArrayList<Integer>();

            // loop to store server data
            for (int i = 0; i < nRecs; i++) {
                receivedFromServer();
                String[] serverParams = serverInput.split(" ");
                int serverCoreCount = Integer.parseInt(serverParams[4]);

                // find biggest server
                if (serverCoreCount > biggestCoreCount) {
                    biggestCoreCount = serverCoreCount;
                    biggestServer = serverParams[0];
                    jobServerIDs = new ArrayList<Integer>();
                    jobServerIDs.add(Integer.parseInt(serverParams[1]));
                }
                if (serverCoreCount < biggestCoreCount) {
                    continue;
                }
                if (serverParams[0].equals(biggestServer)) {
                    jobServerIDs.add(Integer.parseInt(serverParams[1]));
                }
                continue;
            }
            // send OK
            sendToServer("OK");
            receivedFromServer();

            int index = 0; // create index for LRR loop

            serverInput = initialJob; // change input to original job for LRR loop

            while (!serverInput.equals("NONE")) {
                jobParams = serverInput.split(" ");
                int jobID = Integer.parseInt(jobParams[2]);

                // if message contains JOBN, schedule job
                if (serverInput.contains("JOBN")) {
                    sendToServer("SCHD " + jobID + " " + biggestServer + " " + jobServerIDs.get(index));
                    index++;

                    if (jobServerIDs.size() == index) {
                        index = 0;
                    }
                    receivedFromServer();
                }

                // send REDY
                sendToServer("REDY");
                receivedFromServer();
            }
        }
        // send QUIT
        sendToServer("QUIT");
        dout.flush();
        receivedFromServer();
        System.out.println("closing connection. Go to sleep.");

        // closing and tidy up
        dout.close();
        din.close();
        s.close();
    }

    // print an store received message from server
    static void receivedFromServer() throws IOException {
        serverInput = din.readLine();
        System.out.println("server said: " + serverInput);
    }

    // send message to server
    static void sendToServer(String input) throws IOException {
        input = input + "\n";
        dout.write(input.getBytes());
        System.out.println("sent to server: " + input);
        dout.flush();
    }
}
