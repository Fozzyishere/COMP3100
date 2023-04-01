import java.net.*;
import java.io.*;

public class broken_client {

    // global var declaration
    static String serverInput;
    static DataOutputStream dout;
    static BufferedReader din;

    public static void main(String args[]) throws Exception {
        // socket, input and output
        Socket s = new Socket("localhost", 50000);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        dout = new DataOutputStream(s.getOutputStream());

        //JOB related variables
        String jobCommand = "";
        String jobID = "";

        //server related variables
        String jobCore = "";
        String jobMemory = "";
        String jobDisk = "";
        String biggestServer = "";
        Integer serverCount = 0;
        int mostCores = 0;
        int lastScheduleServer = 0;

        // print server connect
        System.out.println("java server started, connection is on!");

        // send HELO
        sendToServer("HELO");
        receivedFromServer();

        // send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        receivedFromServer();

        // check serverInput for last server message
        System.out.println("last server message before doing job loop is: " + serverInput);

        // loop
        while (!serverInput.equals("NONE")) {
            // send REDY
            sendToServer("REDY");
            receivedFromServer();

            // check if server message is NONE
            if (serverInput.equals("NONE")) {
                // break out of loop
                break;
            }

            // spit messages
            // use split() instead of replaceAll() in old client_old.java
            String[] serverParams = serverInput.split(" ");
            jobCommand = serverParams[0];


            // if biggest server has not been found
            if (biggestServer.equals("")) {
                System.out.println("largest servers have not been found.");
                // storing JOB data
                jobID = serverParams[2];
                jobCore = serverParams[4];
                jobMemory = serverParams[5];
                jobDisk = serverParams[6];
                System.out.println("job data has been stored");
                System.out.println(jobID);
                System.out.println(jobCore);
                System.out.println(jobMemory);
                System.out.println(jobDisk);

                // request server with GETS
                sendToServer("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
                receivedFromServer();
                // store DATA info
                serverCount = Integer.parseInt(serverInput.split(" ")[1]); // get server number
                System.out.println("number of servers are: " + serverCount);

                // send OK
                sendToServer("OK");

                // receive servers
                for (int i = 0; i < serverCount; i++) {
                    receivedFromServer();
                    System.out.println("biggest server is : " + biggestServer);
                    String[] serverInfo = serverInput.split(" ");
                    Integer cores = Integer.parseInt(serverInfo[4]);
                    System.out.println("core for current server is:" + cores);

                    // if newer server has bigger core than the old one
                    if (cores > mostCores) {
                        biggestServer = serverInfo[0];
                        serverCount = 0; // reset counter
                        mostCores = cores;
                    }

                    // if same type of server is detected
                    if (serverInfo[0].equals(biggestServer)) {
                        serverCount++;
                    }
                }

                // send OK after all server is received
                sendToServer("OK");
                receivedFromServer();
            }

            // if command is new job
            if (jobCommand.equals("JOBN")) {
                if (lastScheduleServer == serverCount) {
                    // schedule from 0 if jobs still exists and we reach the last server
                    lastScheduleServer = 0;
                }
                System.out.println("last schedule server is: " + lastScheduleServer);

                // send SCHDs
                sendToServer("SCHD " + jobID + " " + biggestServer + " " + lastScheduleServer);
                lastScheduleServer++;
                receivedFromServer();
            }
            if(jobCommand.equals("JCPL")){
                System.out.println("job " + jobID + "is done.");
            }

            //break if server return ERR
            if(jobCommand.equals("ERR:")){
                break;
            }
        }
        // send QUIT
        sendToServer("QUIT");
        dout.flush();
        serverInput = din.readLine();
        System.out.println("closing connection. Go to sleep.");
        // closing and tidy up
        dout.close();
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