import java.net.*;
import java.io.*;
public class c {

    static String serverInput;
    static DataOutputStream dout;
    static BufferedReader din;
    public static void main (String[] args) throws UnknownHostException, IOException{

        //socket, input and output setup
        Socket s = new Socket("localhost", 50000);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        dout = new DataOutputStream(s.getOutputStream());

        //send HELO
        sendToServer("HELO");
        receivedFromServer();

        // send AUTH
        sendToServer("AUTH " + System.getProperty("user.name"));
        receivedFromServer();

        //send REDY
        sendToServer("REDY");
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