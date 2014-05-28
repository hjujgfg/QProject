package PMBPI.Voice;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Created by egor on 1/7/14.
 */
public class Microphone {
    private TargetDataLine dataLine;
    private DataLine.Info info;
    private AudioFormat format;
    private AudioFileFormat.Type fileType;
    private File wavFile;
    public boolean isRunning;
    public Microphone(String file) {
        initialize(file);
    }
    private void initialize(String file) {
        format = new AudioFormat(16000.0f, 16, 1, true, true);
        fileType = AudioFileFormat.Type.WAVE;
        info = new DataLine.Info(TargetDataLine.class, format);
        wavFile = new File(file);
        try {
            dataLine = (TargetDataLine) AudioSystem.getLine(info);
            dataLine.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /*public void start() {
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
    }*/
    public void start() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numBytesRead;
        byte[] data = new byte[dataLine.getBufferSize() / 5];
        dataLine.start();
        isRunning = true;
        while (isRunning) {
            numBytesRead = dataLine.read(data, 0, data.length);
            out.write(data, 0, numBytesRead);
        }
        byte[] audioData = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream outputAIS = new AudioInputStream(bais, format, audioData.length/format.getFrameSize());
        try {
            AudioSystem.write(outputAIS, AudioFileFormat.Type.WAVE, wavFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        dataLine.flush();
        dataLine.stop();
        dataLine.close();
        System.out.print("finished");
    }

}
