import java.net.*;
import java.io.*;

public class client {

    // global var declaration
    static String serverInput;
    static DataOutputStream dout;
    static BufferedReader din;

    public static void main(String args[]) throws Exception {
        // main var initialisation
        Socket s = new Socket("localhost", 50000);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        dout = new DataOutputStream(s.getOutputStream());
        String jobCommand = "";
        String jobID = "";
        String jobCore = "";
        String jobMemory = "";
        String jobDisk = "";
        String biggestServer = "";
        Integer serverCount = 0;
        int biggestCore = 0;

        // print server connect
        System.out.println("java server started, connection is on!");

        // send HELO
        sendToServer("HELO");
        recievedFromServer();

        // send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        recievedFromServer();

        // check serverInput for last server message
        System.out.println("last server message before doing job loop is: " + serverInput);

        // loop
        while (!serverInput.equals("NONE")) {
            // send REDY
            sendToServer("REDY");
            recievedFromServer();

            // check if server message is NONE
            if (serverInput.equals("NONE")) {
                // break out of loop
                break;
            }

            // spit messages
            // use split() instead of replaceAll() in old client_old.java
            // theses will be used later when sending GETS to server
            String[] serverParams = serverInput.split(" ");
            jobCommand = serverParams[0];

            // if biggest server has not been found
            if (biggestServer.equals("")) {
                System.out.print("largest servers have not been found.");
                // storing JOB data
                jobID = serverParams[serverParams.length - 5];
                jobCore = serverParams[serverParams.length - 3];
                jobMemory = serverParams[serverParams.length - 2];
                jobDisk = serverParams[serverParams.length -1];
                System.out.println("job data has been stored");
                System.out.println(jobID);
                System.out.println(jobCore);
                System.out.println(jobMemory);
                System.out.println(jobDisk);

                // request server with GETS
                sendToServer("GETS Capable " + jobCore + " " + jobMemory + "" + jobDisk);
                recievedFromServer();
                // store DATA info
                serverCount = Integer.parseInt(serverInput.split(" ")[1]); //get server number
                System.out.println("number of servers are: " + serverInput.split(" ")[1]);

                //send OK
                sendToServer("OK");

                //recive server
                for(int i = 0; i < serverCount; i++){
                    recievedFromServer();
                    String[] serverInfo = serverInput.split(" ");
                    Integer cores = Integer.parseInt(serverInfo[4]);

                    //if newer server has bigger core than the old one
                    if(cores > biggestCore){
                        biggestServer = serverInfo[0];
                        serverCount = 0;    //reset counter
                        biggestCore = cores;
                    }

                    //if same type of server is detected
                    if(serverInfo[0].equals(biggestServer)){
                        serverCount++;
                    }
                }

                //send OK after all server is recieved
                sendToServer("OK");

                //should recieve "." (please be dot for fucc sakes)
                recievedFromServer();
            }

        }

        dout.close();
        s.close();
    }

    // print an store recieved message from server
    static void recievedFromServer() throws IOException {
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