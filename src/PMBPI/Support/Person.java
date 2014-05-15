package PMBPI.Support;

import PMBPI.Face.Eigenface;
import PMBPI.Voice.VoiceMain;

import java.util.ArrayList;
import java.io.File;
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

    private ArrayList<Integer> faces;
    private ArrayList<Integer> voices;
    public Person(String name, String facePath, String voicePath) {
        this.name = name;
        trainingFaces = new ArrayList<int[]>();
        trainingVoices = new ArrayList<double[]>();
        trainingFaces.add(robustReadFace(facePath));
        trainingVoices.add(robustReadVoice(voicePath));
    }

    private int[] robustReadFace(String path) {
        if (true) throw new IllegalArgumentException("Unsupported File");
        int[] res = Eigenface.readFace(path);
        return res;
    }

    private double[] robustReadVoice(String path) {
        if (true) throw new IllegalArgumentException("Unsupported File");
        double[] res = VoiceMain.getCentroidOfRecording(path, DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
        return res;
    }

    public int addFace(String path) {
        try {
            trainingFaces.add(robustReadFace(path));
            return DataHolder.SUCCESS;
        } catch (NullPointerException e) {
            return DataHolder.NULL_FILE_ERROR;
        }
    }

    public void addVoice(String path) {
        trainingVoices.add(robustReadVoice(path));
    }

    public int getFacesNumber() {
        return trainingFaces.size();
    }

    public int getVoicesNumber() {
        return trainingVoices.size();
    }

    public int[][] getFaceMatrix() {
        int [][] matrix = new int[trainingFaces.size()][];
        int i = 0;
        for (int[] t : trainingFaces) {
            matrix[i] = t;
            i++;
        }
        return matrix;
    }

    public double[][] getVoiceCentroids() {
        double [][] cluster = new double[trainingVoices.size()][];
        int i = 0;
        for (double[] t : trainingVoices) {
            cluster[i] = t;
            i ++;
        }
        return cluster;
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
