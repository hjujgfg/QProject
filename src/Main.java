/**
 * Created by egor on 1/7/14.
 */
import PMBPI.Voice.Microphone;

import javax.sound.sampled.TargetDataLine;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static boolean flag;
    public static void main(String[] args){
        flag = true;
        System.out.print("Hello again!");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            final Microphone m = new Microphone();
            String t = br.readLine();
            if (t.equals("k"))
                flag = false;
            Thread tr = new Thread(new Runnable() {
                @Override
                public void run() {
                    m.start();
                }
            });
            tr.start();
            while (!t.equals("k")) {
                System.out.print(t);
                t = br.readLine();
            }
            m.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
