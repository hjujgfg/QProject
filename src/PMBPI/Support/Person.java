package PMBPI.Support;

import PMBPI.Face.Eigenface;
import PMBPI.Face.ImageProcessor;
import PMBPI.Voice.VoiceMain;
import com.sun.media.imageio.plugins.pnm.PNMImageWriteParam;
import javafx.scene.chart.XYChart;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by egor on 5/14/14.
 */
public class Person {
    public static final int FACE = 1;
    public static final int VOICE = 2;

    private String name;
    public int ID;
    private ArrayList<int[]> trainingFaces;
    private ArrayList<double[]> trainingVoices;
    private File userPic;

    public Person(String name) {
        this.name = name;
        trainingFaces = new ArrayList<int[]>();
        trainingVoices = new ArrayList<double[]>();
    }
    public void setUserPic(File f) {
        this.userPic = f;
    }
    public Person(String name, String facePath, String voicePath) {
        this.name = name;
        userPic = new File(facePath);
        trainingFaces = new ArrayList<int[]>();
        trainingVoices = new ArrayList<double[]>();
        int newfaceResult = addFace(facePath);
        int newVoiceResult = addVoice(voicePath);
        if (newfaceResult != DataHolder.SUCCESS)
            throw new IllegalArgumentException("Img");
        if (newVoiceResult != DataHolder.SUCCESS)
            throw new IllegalArgumentException("Aud");
    }


    public int addFace(String path) {
        try {
            ImageProcessor processor = new ImageProcessor();
            trainingFaces.add(processor.readFaceFromFile(path, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT));
            return DataHolder.SUCCESS;
        } catch (NullPointerException e) {
            return DataHolder.IMAGE_PARAMS_ERROR;
        }
    }

    public int addVoice(String path) {
        try {
            double [] d = VoiceMain.getCentroidOfRecording(path, DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
            trainingVoices.add(d);
            return DataHolder.SUCCESS;
        } catch (NullPointerException e ) {
            return DataHolder.NULL_FILE_ERROR;
        }
    }

    public int addMultVoicesForOneSample(String[] voices) {
        try {
            double [] d = VoiceMain.trainOnSeveralFiles(voices, DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
            trainingVoices.add(d);
            return DataHolder.SUCCESS;
        } catch (Exception e) {
            return DataHolder.NULL_FILE_ERROR;
        }
    }




    public int getFacesNumber() {
        return trainingFaces.size();
    }

    public int getVoicesNumber() {
        return trainingVoices.size();
    }

    public String getName() {
        return name;
    }

    public int[][] getFaceMatrix() {
        int[][] matrix = new int[trainingFaces.size()][];
        int i = 0;
        for (int[] t : trainingFaces) {
            matrix[i] = t;
            i++;
        }
        return matrix;
    }

    public double[][] getVoiceCentroids() {
        double[][] cluster = new double[trainingVoices.size()][];
        int i = 0;
        for (double[] t : trainingVoices) {
            cluster[i] = t;
            i++;
        }
        return cluster;
    }

    public int saveAudio() {
        try {
            File f = new File("data/"+name+".ser");
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(trainingVoices);
            fos.flush();
            fos.close();
            oos.close();
            return DataHolder.SUCCESS;
        } catch (FileNotFoundException e) {
            return DataHolder.NULL_FILE_ERROR;
        } catch (IOException e) {
            return DataHolder.NULL_FILE_ERROR;
        }
    }

    public int loadAudioData() {
        try {
            FileInputStream fis = new FileInputStream("data/"+name+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            trainingVoices = (ArrayList<double[]>)ois.readObject();
            return DataHolder.SUCCESS;
        } catch (FileNotFoundException e) {
            return DataHolder.NULL_FILE_ERROR;
        } catch (IOException e) {
            return DataHolder.NULL_FILE_ERROR;
        } catch (ClassNotFoundException e) {
            return DataHolder.NULL_FILE_ERROR;
        }
    }

    public int clearData() {
        File f = new File("data/"+name+".ser");
        try {
            boolean bb = f.delete();
            if (bb) return DataHolder.SUCCESS;
            return DataHolder.NULL_FILE_ERROR;
        } catch (Exception e){
            return DataHolder.NULL_FILE_ERROR;
        }
    }
}
