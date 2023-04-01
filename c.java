import java.net.*;
import java.io.*;
import java.util.*;

public class c {

    static String serverInput;
    static DataOutputStream dout;
    static BufferedReader din;

    public static void main(String[] args) throws UnknownHostException, IOException {
        // local variables
        String initialJob = ""; // store initial JOB command
        String jobID = ""; // store JOB info
        String jobCore = ""; // store JOB info
        String jobMemory = ""; // store JOB info
        String jobDisk = ""; // store JOB info
        Integer nRecs = 0; // store nRecs from DATA command
        String biggestServer = ""; // store name of biggest server
        Integer biggestCoreCount = 0; // store biggest server's core count
        ArrayList <String> postDATAServerIDs = new ArrayList<String>(); //store serverID received after DATA command
        Integer serverCore = 0; //store server core for comparison



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
        receivedFromServer(); // received either JOBN, JCPL, NONE

        // if server said there're jobs to schedule
        if (serverInput.contains("NONE")) {
            initialJob = serverInput;

            // storing JOB data
            String[] jobParams = serverInput.split(" ");
            jobID = jobParams[2];
            jobCore = jobParams[4];
            jobMemory = jobParams[5];
            jobDisk = jobParams[6];

            // send GETS
            sendToServer("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
            receivedFromServer();

            // get data from DATA command
            String[] DATAParams = serverInput.split(" ");
            nRecs = Integer.parseInt(DATAParams[1]);

            // send OK
            sendToServer("OK");
            receivedFromServer();

            //loop through server info received
            for(int i = 0; i < nRecs; i++){
                receivedFromServer();
                String[] postDATAServerParam = serverInput.split(" ");
                serverCore = Integer.parseInt(postDATAServerParam[4]);

                //find the biggest server
                if(serverCore > biggestCoreCount){
                    biggestCoreCount = serverCore;
                    biggestServer = postDATAServerParam[0];
                    postDATAServerIDs.add(postDATAServerParam[1]);
                }
                if(postDATAServerParam[0].equals(biggestServer)){
                    postDATAServerIDs.add(postDATAServerParam[1]);
                }
                continue;
            }
        }


        //send OK
        sendToServer("OK");
        receivedFromServer();
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