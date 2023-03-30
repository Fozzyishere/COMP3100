import java.net.*;
import java.io.*;

class client {

    //main
    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 50000);
        BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str = "", str2 = "";
        int[] DATAInt = new int[2];

        while (!str.equals("QUIT")) {
            str = br.readLine();
            dout.write((str + "\n").getBytes());
            dout.flush();
            str2 = din.readLine();
            if(str.contains("OK") && str2.contains("DATA")){
                DATAInt = convertStringToInt(storeNumber(extractNumber(str2)));
                int nRecs = DATAInt[0];
                for(int i = 0; i < nRecs; i++){
                    din.readLine();
                }
            }
        }
        dout.close();
        s.close();
    }


    //extract numbers from a string
    static String extractNumber(String input){
        return input.replaceAll("\\D+\\W+", "");
    }

    //store extracted number in an array
    static String[] storeNumber(String input){
        String returnString = "";
        String[] returnArray = new String[2];
        int returnArrayCount = 0;
        for (int i = 0; i < input.length(); i++){
            char inputScan = input.charAt(i);
            if (inputScan > 47 && inputScan < 58){
                returnString = returnString + inputScan;
            }
            if(inputScan == 32){
                returnArray[returnArrayCount] = returnString;
                returnString = "";
                returnArrayCount++;
            }
            returnArray[returnArrayCount] = returnString;
        }
        return returnArray;
    }

    //convert string array to int array
    static int[] convertStringToInt(String[] input){
        int[] returnArray = new int[2];
        for(int i = 0; i < input.length; i++){
            returnArray[i] = Integer.parseInt(input[i]);
        }
        return returnArray;
    }
}