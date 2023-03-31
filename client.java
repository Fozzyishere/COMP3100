import java.net.*;
import java.io.*;

public class client {

    // global var declaration
    static String serverComm;
    static DataOutputStream dout;
    static BufferedReader din;

    public static void main(String args[]) throws Exception {
        // main var declaration
        Socket s = new Socket("localhost", 50000);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        dout = new DataOutputStream(s.getOutputStream());

        // print server connect
        System.out.println("java server started, connection is on!");

        // send HELO
        sendToServer("HELO");
        recievedFromServer();

        // send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        recievedFromServer();

        // loop

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