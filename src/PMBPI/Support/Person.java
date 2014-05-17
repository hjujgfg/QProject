package PMBPI.Support;

import PMBPI.Face.Eigenface;
import PMBPI.Face.ImageProcessor;
import PMBPI.Voice.VoiceMain;
import com.sun.media.imageio.plugins.pnm.PNMImageWriteParam;

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

    private ArrayList<Integer> faces;
    private ArrayList<Integer> voices;

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

    private double[] robustReadVoice(String path) {
        if (true) throw new IllegalArgumentException("Unsupported File");
        return VoiceMain.getCentroidOfRecording(path, DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
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



    public int getFacesNumber() {
        return trainingFaces.size();
    }

    public int getVoicesNumber() {
        return trainingVoices.size();
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

   /* public Person(String name, int newIndex, int type) {
        new Person(name);
        switch (type) {
            case FACE:
                faces.add(newIndex);
                break;
            case VOICE:
                voices.add(newIndex);
                break;
            default: break;
        }
    }*/

    /*public void addIndex(int newIndex, int type) {
        switch (type) {
            case FACE:
                faces.add(newIndex);
                break;
            case VOICE:
                voices.add(newIndex);
                break;
            default: break;
        }
    }

    public void updateIndices(int removedIndex, int type) {
        switch (type) {
            case FACE:
                for (int i = 0; i < faces.size(); i ++) {
                    faces.set(i, faces.get(i) - 1);
                }
                break;
            case VOICE:
                for (int i = 0; i < voices.size(); i ++) {
                    voices.set(i, voices.get(i) - 1);
                }
                break;
            default: break;
        }
    }*/
}
