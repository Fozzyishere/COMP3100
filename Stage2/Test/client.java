import java.net.*;
import java.io.*;
import java.util.*;

public class client{
    private static Socket s;
    private static DataOutputStream dout;
    private static BufferedReader din;

    // init connection to server
    private client(String host,int port) throws Exception {
        s = new Socket(host, port);
        dout = new DataOutputStream(s.getOutputStream());
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));
   }

}