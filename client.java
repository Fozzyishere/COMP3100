
import java.net.*;
import java.io.*;

public class client {
    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 50000);
        BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        String DATAString = "";
        int[] DATAValues = new int[2];

        // send hello
        dout.write(("HELO\n").getBytes());
        System.out.println(din.readLine());

        // send auth
        String username = System.getProperty("user.name");
        dout.write(("AUTH " + username + "\n").getBytes());
        System.out.println(din.readLine());

        // loop
        while (!din.readLine().equals("")) {
            dout.write(("REDY\n").getBytes());
            System.out.println(din.readLine());
            dout.write(("GETS All\n").getBytes());
            DATAString = din.readLine();
            System.out.println(din.readLine());

        }

        dout.close();
        s.close();
    }

    // extract numbers from a string
    static String extractNumber(String input) {
        return input.replaceAll("\\D+\\W+", "");
    }

    // store extracted number in an array
    static String[] storeNumber(String input) {
        String returnString = "";
        String[] returnArray = new String[2];
        int returnArrayCount = 0;
        char lastChar = input.charAt(input.length() - 1);
        for (int i = 0; i < input.length(); i++) {
            char inputScan = input.charAt(i);
            if (inputScan > 47 && inputScan < 58) {
                returnString = returnString + inputScan;
            }
            if (inputScan == 32) {
                returnArray[returnArrayCount] = returnString;
                returnString = "";
                returnArrayCount++;
            }
            if (lastChar != 32) {
                returnArray[returnArrayCount] = returnString;
            }
        }
        return returnArray;
    }

    // convert string array to int array
    static int[] convertStringToInt(String[] input) {
        int[] returnArray = new int[2];
        for (int i = 0; i < input.length; i++) {
            returnArray[i] = Integer.parseInt(input[i]);
        }
        return returnArray;
    }
}
