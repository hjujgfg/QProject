package PMBPI.Voice;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by egor on 1/7/14.
 */
public class Microphone {
    private TargetDataLine dataLine;
    private DataLine.Info info;
    private AudioFormat format;
    private AudioFileFormat.Type fileType;
    private File wavFile;
    public Microphone(String file) {
        initialize(file);
    }
    private void initialize(String file) {
        format = new AudioFormat(16000.0f, 16, 1, true, true);
        fileType = AudioFileFormat.Type.WAVE;
        info = new DataLine.Info(TargetDataLine.class, format);
        wavFile = new File(file);
    }
    public void start() {
        try {
            if (!AudioSystem.isLineSupported(info)) {
                System.out.print("Line not supported");
                System.exit(0);
            }
            dataLine = (TargetDataLine) AudioSystem.getLine(info);
            dataLine.open(format);
            dataLine.start();
            System.out.print("Start capturing");
            AudioInputStream ais = new AudioInputStream(dataLine);
            System.out.print("start recording");
            AudioSystem.write(ais, fileType, wavFile);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        dataLine.stop();
        dataLine.close();
        System.out.print("finished");
    }

}
