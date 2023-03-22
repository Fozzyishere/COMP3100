import java.net.*;
import java.io.*;

class client {
    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 50000);
        BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String str = "", str2 = "";
        while (!str.equals("stop")) {
            str = br.readLine();
            dout.write((str + "\n").getBytes());
            dout.flush();
            str2 = din.readLine();
            System.out.println("Server says: " + str2);
        }

        dout.close();
        s.close();
    }
}