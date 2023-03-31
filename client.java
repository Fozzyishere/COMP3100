import java.net.*;
import java.io.*;

public class client {

    // global var declaration
    static String serverComm;
    static DataOutputStream dout;
    static BufferedReader din;

    public static void main(String args[]) throws Exception {
        // main var initialisation
        Socket s = new Socket("localhost", 50000);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        dout = new DataOutputStream(s.getOutputStream());
        String jobID = "";
        String jobCore = "";
        String jobMemory = "";
        String jobDisk = "";
        String biggestServer = "";
        int serverCount = 0;
        

        // print server connect
        System.out.println("java server started, connection is on!");

        // send HELO
        sendToServer("HELO");
        recievedFromServer();

        // send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        recievedFromServer();


        //check serverComm for last server message
        System.out.println("last server message before doing job loop is: " + serverComm);

        // loop
        while(!serverComm.equals("NONE")){
            //send REDY
            sendToServer("REDY");
            recievedFromServer();

            //check if server message is NONE after doing jobs
            if(serverComm.equals("NONE")){
                //break out of loop and close client
                break;  
            }

            //spit JOB message
            //use split() instead of replaceAll() in old client_old.java
            //theses will be used later when sending GETS to server
            String[] jobStrings = serverComm.split(" ");
            jobID = jobStrings[2];
            jobCore = jobStrings[4];
            jobMemory = jobStrings[5];
            jobDisk = jobStrings[6];

            
        }

        dout.close();
        s.close();
    }

    // print an store recieved message from server
    static void recievedFromServer() throws IOException {
        serverComm = din.readLine();
        System.out.println("server said: " + serverComm);
    }

    // send message to server
    static void sendToServer(String input) throws IOException {
        input = input + "\n";
        dout.write(input.getBytes());
        System.out.println("sent to server: " + input);
        dout.flush();
    }

}