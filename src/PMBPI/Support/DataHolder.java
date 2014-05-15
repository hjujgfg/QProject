package PMBPI.Support;

import PMBPI.Face.Eigenface;
import PMBPI.Voice.VoiceMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by egor on 5/14/14.
 */
public class DataHolder {
    int[] mean;
    public static int IMG_WIDTH;
    public static int IMG_HEIGHT;
    public static int VOICE_SAMPLE_PER_FRAME;
    public static int VOICE_SAMPLING_RATE;

    public static final int NULL_FILE_ERROR = -1;
    public static final int WRITE_FACE_MATRIX_ERROR = 0;
    public static final int SUCCESS = 100;

    public static final int IMAGE_PARAMS_ERROR = 50;
    public static final int VOICE_PARAMS_ERROR = 60;

    ArrayList<Person> people;
    ArrayList<Integer> personFacesNumbers;

    ArrayList<int[]> trainingFaceMatrix;
    ArrayList<double[]> trainingVoices;


    public DataHolder() {
        people = new ArrayList<Person>();
        personFacesNumbers = new ArrayList<Integer>();
    }

    /*public int addPerson(String name, File face, File voice) {
        if (face == null && voice == null) {
            return -1;
        }
        Person p = new Person(name);
        if (face != null) {
            int [] newface = robustReadFace(face.getPath());
            trainingFaceMatrix.add(newface);
            *//*PersonFace item = new PersonFace(newface, p);
            trainingFaceMatrix.add(item);*//*
        }
        if (voice != null) {
            double[] newcentroid = robustReadVoice(voice.getPath());
            trainingVoices.add(newcentroid);
        }
        return 1;
    }*/

    public int addPerson(String name, File face, File voice) {
        if (face == null || voice == null) {
            return NULL_FILE_ERROR;
        }
        Person p = new Person(name, face.getPath(), voice.getPath());
        people.add(p);
        int[][] matr = collectFaceMatrix();
        int res = writeFaceMatrix(matr);

        return res;
    }
    private int writeFaceMatrix(int[][] matr) {
        try {
            File faceMatrixHolder = new File("data/faceMatrix.ser");
            FileOutputStream fos = new FileOutputStream(faceMatrixHolder, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(matr);
            fos.close();
            oos.close();
            return SUCCESS;
        } catch (Exception e) {
            return WRITE_FACE_MATRIX_ERROR;
        }
    }
    public int removePerson(Person p) {
        people.remove(p);
        int[][] matr = collectFaceMatrix();
        int res = writeFaceMatrix(matr);

        return res;
    }

    public int setIMGDimensions(int width, int height) {
        if (width < 92 || width > 180 || height < 92 || height > 250)
            return IMAGE_PARAMS_ERROR;
        IMG_WIDTH = width;
        IMG_HEIGHT = height;
        return SUCCESS;
    }

    public int setSoundCapureParams(int samplesPerFrame, int samplingRate) {
        if (samplesPerFrame < 8 || samplesPerFrame > 32 || samplingRate < 8000 || samplingRate > 32000)
            return  VOICE_PARAMS_ERROR;
        VOICE_SAMPLE_PER_FRAME = samplesPerFrame;
        VOICE_SAMPLING_RATE = samplingRate;
        return SUCCESS;
    }

    private int[][] collectFaceMatrix() {
        int[][] matrix;
        int totalCount;
        ArrayList<int[]> preMatrix = new ArrayList<int[]>();
        int personCounter = 0;
        for (Person p : people) {
            for (int[] t : p.getFaceMatrix()) {
                preMatrix.add(t);
                personFacesNumbers.add(personCounter);
            }
            personCounter ++;
        }
        matrix = new int[preMatrix.size()][];
        int i = 0;
        for (int[] m : preMatrix) {
            matrix[i] = m;
            i++;
        }
        return matrix;
    }




   /* private class PersonFace {
        int[] face;
        Person p;
        PersonFace (int [] f, Person per) {
            face = f;
            p = per;
        }
    }

    private class PersonVoice {
        double [] voiceSample;
        Person p;
        PersonVoice(double [] voice, Person p) {
            voiceSample = voice;
            this.p = p;
        }
    }*/
}
