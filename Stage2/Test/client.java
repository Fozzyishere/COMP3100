import java.net.*;
import java.io.*;
import java.util.*;

public class client {
    //vars
    static String serverInput;
    static DataOutputStream dout;
    static BufferedReader din;
    static Socket s;

    public static void main(String[] args) throws IOException{

    }

    //setting up socket , input and output streams
    static void setSocket(Socket s, DataOutputStream dout, BufferedReader din){
        try {
            s = new Socket("localhost", 50000);
            dout = new DataOutputStream(s.getOutputStream());
            din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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