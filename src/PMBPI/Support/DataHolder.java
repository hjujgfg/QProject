package PMBPI.Support;

import PMBPI.Face.Eigenface;
import PMBPI.Face.ImageProcessor;
import PMBPI.Face.TrainingDataHolder;
import PMBPI.Voice.VoiceMain;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by egor on 5/14/14.
 */
public class DataHolder implements Serializable{

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
    ArrayList<Integer> personVoicesNumbers;

    //ImageProcessor imageProcessor;
    public static void main(String[] args) {
        DataHolder dh = new DataHolder();
        dh.setIMGDimensions(92, 112);
        dh.setSoundCapureParams(16, 16000);
        int train = 40;
        int test = 40;
        String[][] perFaces = new String[train][2];
        for (int i = 0; i < train; i ++) {
            for (int j = 0; j < 2; j ++) {
                perFaces[i][j] = "faces/s" + (i+1) + "/" + (j+1) + ".pgm";
            }
        }
        String[][] perVoices = new String[train][7];
        for (int i = 0; i < train; i ++) {
            for (int j = 0; j < 7; j ++) {
                perVoices[i][j] = "audio_data/16khz_16bit/"+(i+1)+"/"+(j) +".wav";
            }
        }
        for (int i = 0; i < train; i ++) {
            dh.addPerson("pers#" + (i+1), perFaces[i], perVoices[i]);
            System.out.println("adding person " + i);
        }

        String[][] perFacest = new String[test][3];
        for (int i = 0; i < test; i ++) {
            for (int j = 0; j < 3; j ++) {
                perFacest[i][j] = "faces/s" + (i+1) + "/" + (j+6) + ".pgm";
            }
        }
        String[][] perVoicest = new String[test][3];
        for (int i = 0; i < test; i ++) {
            for (int j = 0; j < 3; j ++) {
                perVoicest[i][j] = "audio_data/16khz_16bit/"+(i+1)+"/"+(j+6)+".wav";
            }
        }
        int[][] matr = dh.collectFaceMatrix();
        TrainingDataHolder tdh = Eigenface.train(matr, IMG_WIDTH, IMG_HEIGHT);
        tdh.save();

        double[][] results = new double[test * 3][];
        int j = 0;
        for (int i = 0; i < results.length; i +=3) {
            results[i] = dh.recognize(perFacest[j][0], perVoicest[j]);
            results[i + 1] = dh.recognize(perFacest[j][1], perVoicest[j]);
            results[i + 2] = dh.recognize(perFacest[j][2], perVoicest[j]);
            j ++;
            System.out.println("recognizing pers " + j);
        }
        int i = 0;
        int personcounter = 0;
        int errorsNumber = 0;
        for (double[] d : results) {
            System.out.println();
            System.out.println("test# " + i);
            /*int facepn = dh.personFacesNumbers.get((int)d[1]);
            int voicepn = dh.personVoicesNumbers.get((int)d[3]);
            System.out.print("P-face: " + dh.people.get(facepn).getName() + " " +d[0] + "\n");
            System.out.print("P-voice: " + dh.people.get(voicepn).getName() + " " + d[2] + "\n");
            System.out.println("---------------------------");*/
            int res = dh.enterpretResultsTest(d);
            System.out.print("result person: " + res + " personCounter: " + personcounter);
            if (personcounter != res) errorsNumber ++;
            i ++;
            if (i % 3 == 0) {
                personcounter ++;
                System.out.println("\n---------------------------------");
            }

            /*for (double dd : d) {
                System.out.print(dd + " ");
            }
            System.out.println("__________________________________________________");*/

        }
        /*for (int o : dh.personFacesNumbers) {
            System.out.print(o + " ");
        }
        System.out.println();
        for (int o : dh.personVoicesNumbers) {
            System.out.print(o + " ");
        }*/
        System.out.println();
        System.out.println("Number of errors: " + errorsNumber);
    }



    public DataHolder() {
        //imageProcessor = new ImageProcessor();
        people = new ArrayList<Person>();
        personFacesNumbers = new ArrayList<Integer>();
        personVoicesNumbers = new ArrayList<Integer>();
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    /*public Person enterpretResults(double[] results) {
        int facepn = personFacesNumbers.get((int)results[1]);
        int voicepn = personVoicesNumbers.get((int)results[3]);
        if (facepn == voicepn && results[2] <= 5 && results[0] <= 60000000) return people.get(voicepn);
        if (results[2] <= 5 && facepn == voicepn) return people.get(voicepn);
        return null;
    }*/
    public Person enterpretResults (double[] results) {
        int facepn = personFacesNumbers.get((int)results[1]);
        int voicepn = personVoicesNumbers.get((int)results[3]);
        if (results[0] > 15000000 || results[2] > 10) return null;
        if (facepn == voicepn && results[2] <= 5 && results[0] <= 10000000) return people.get(voicepn);
        if (results[2] <= 5 && facepn == voicepn) return people.get(voicepn);

        if (results[0] > 10000000 && results[2] < 5) return people.get(voicepn);
        if (results[2] > 7 && results[0] < 10000000) return people.get(facepn);

        if (facepn == voicepn) return people.get(voicepn);
        if (results[2] < 1.2) return people.get(voicepn);
        return null;
    }
    public int enterpretResultsTest(double[] results) {
        int facepn = personFacesNumbers.get((int)results[1]);
        int voicepn = personVoicesNumbers.get((int)results[3]);
        if (facepn == voicepn && results[2] <= 7 && results[0] <= 10000000) return voicepn;
        if (results[2] <= 7 && facepn == voicepn) return voicepn;
        if (results[0] > 10000000) return voicepn;
        if (results[2] > 7 && results[0] < 10000000) return facepn;
        if (facepn == voicepn) return voicepn;
        if (results[2] < 1.2) return voicepn;
        return -1;
    }
    public int addPerson(String name, File face, File voice) {
        if (face == null || voice == null) {
            return NULL_FILE_ERROR;
        }
        Person p = new Person(name, face.getPath(), voice.getPath());
        people.add(p);
        int[][] matr = collectFaceMatrix();
        TrainingDataHolder tmp = Eigenface.train(matr, IMG_WIDTH, IMG_HEIGHT);
        return tmp.save();
    }
    public int addPerson(String name, String[] faces, ArrayList<double[]> voices) {
        Person p = new Person(name);
        for (String s : faces) {
            p.addFace(s);
        }
        int [][] matr = collectFaceMatrix();
        TrainingDataHolder tdh = Eigenface.train(matr, IMG_WIDTH, IMG_HEIGHT);
        tdh.save();
        double[][] doubles = new double[voices.size()][];
        int i = 0;
        for (double[] d : voices) {
            doubles[i] = d;
            i ++;
        }
        double[] centroid = VoiceMain.calcCentroid(doubles);
        p.addVoice(centroid);
        p.setUserPic(new File(faces[0]));
        people.add(p);
        return DataHolder.SUCCESS;
    }



    public int addPerson(String name, String[] faces, String[] voices) {
        Person p = new Person(name);

        int res;
        for (String f : faces) {
            res = p.addFace(f);
            if (res != SUCCESS) return res;
        }
        p.setUserPic(new File(faces[0]));
        res = p.addMultVoicesForOneSample(voices);

        //int [][] matrix = collectFaceMatrix();

        //TrainingDataHolder trainingDataHolder = Eigenface.train(matrix, IMG_WIDTH, IMG_HEIGHT);
        //trainingDataHolder.save();
        if (res == SUCCESS) people.add(p);
        return res;
    }

    public int addFace(int personIndex, File face) {
        Person current = people.get(personIndex);
        int res = current.addFace(face.getPath());
        if (res == SUCCESS) {
            int[][] matr = collectFaceMatrix();
            TrainingDataHolder tmp = Eigenface.train(matr, IMG_WIDTH, IMG_HEIGHT);
            res = tmp.save();
        }
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

    private int[][] readFaceMatrix() {
        try {
            File facematrixholder = new File("data/faceMatrix.ser");
            FileInputStream fis = new FileInputStream(facematrixholder);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (int[][])ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public int removePerson(Person p) {
        p.clearData();
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
            return VOICE_PARAMS_ERROR;
        VOICE_SAMPLE_PER_FRAME = samplesPerFrame;
        VOICE_SAMPLING_RATE = samplingRate;
        return SUCCESS;
    }

    /*public int[][] collectFaceMatrix() {
        int[][] matrix;
        ArrayList<int[]> preMatrix = new ArrayList<int[]>();
        personFacesNumbers.clear();
        int personCounter = 0;
        for (Person p : people) {
            for (int[] t : p.getFaceMatrix()) {
                preMatrix.add(t);
                personFacesNumbers.add(personCounter);
            }
            personCounter++;
        }
        matrix = new int[preMatrix.size()][];
        int i = 0;
        for (int[] m : preMatrix) {
            matrix[i] = m;
            i++;
        }
        return matrix;
    }*/
    public int[][] collectFaceMatrix() {
        int[][] matrix;
        ArrayList<int[]> preMatrix = new ArrayList<int[]>();
        personFacesNumbers.clear();
        int personCounter = 0;
        for (Person p : people) {
            preMatrix.add(Eigenface.calcMean(p.getFaceMatrix()));
            personFacesNumbers.add(personCounter);
            personCounter++;
        }
        matrix = new int[preMatrix.size()][];
        int i = 0;
        for (int[] m : preMatrix) {
            matrix[i] = m;
            i++;
        }
        return matrix;
    }

    private double[][] collectVoicesMatrix() {
        double[][] matrix;
        ArrayList<double[]> preMatrix = new ArrayList<double[]>();
        personVoicesNumbers.clear();
        int personCounter = 0;
        for (Person p : people) {
            for (double[] t : p.getVoiceCentroids()){
                preMatrix.add(t);
                personVoicesNumbers.add(personCounter);
            }
            personCounter ++;
        }
        matrix = new double[preMatrix.size()][];
        int i = 0;
        for (double[] d : preMatrix) {
            matrix[i] = d;
            i++;
        }
        return matrix;
    }



    public double[] recognize(String face, String[] voice) {
        TrainingDataHolder saved = TrainingDataHolder.load();
        ImageProcessor processor = new ImageProcessor();
        int[] testing = processor.readFaceFromFile(face, IMG_WIDTH, IMG_HEIGHT);
        double[] faceRes = Eigenface.recognizeSingle(testing, saved);

        double[][] voicem = collectVoicesMatrix();

        double[] testCentr = VoiceMain.trainOnSeveralFiles(voice, VOICE_SAMPLE_PER_FRAME, VOICE_SAMPLING_RATE);
        double[] voiceRecRes = VoiceMain.identify(testCentr, voicem);
        double[] identResult = new double[faceRes.length + voiceRecRes.length];
        for (int i = 0; i < faceRes.length; i ++) {
            identResult[i] = faceRes[i];
        }
        for (int i = 0; i < voiceRecRes.length; i ++) {
            identResult[i+faceRes.length] = voiceRecRes[i];
        }
        return identResult;
    }

    public Person identify(String face, double[] testCentroid) {
        TrainingDataHolder saved = TrainingDataHolder.load();
        ImageProcessor processor = new ImageProcessor();
        int[] testing  = processor.readFaceFromFile(face, IMG_WIDTH, IMG_HEIGHT);
        double[] faceRes = Eigenface.recognizeSingle(testing, saved);
        double[][] voicem = collectVoicesMatrix();
        double[] voiceRecRes = VoiceMain.identify(testCentroid, voicem);
        double[] identResult = new double[faceRes.length + voiceRecRes.length];
        for (int i = 0; i < faceRes.length; i ++) {
            identResult[i] = faceRes[i];
        }
        for (int i = 0; i < voiceRecRes.length; i ++) {
            identResult[i+faceRes.length] = voiceRecRes[i];
        }
        return enterpretResults(identResult);
    }

    public Person identify(String face, String[] voices) {
        try {
            return enterpretResults(recognize(face, voices));
        } catch (Exception ex) {
            return null;
        }
    }

    public Person identify(String face, String voice) {
        String[] voices = {voice};
        try {
            return enterpretResults(recognize(face, voices));
        } catch (Exception ex) {
            return null;
        }
    }

    public Person identifyByImage(BufferedImage image) {
        ImageProcessor processor = new ImageProcessor();
        int[] face = processor.extractInnerImage(image, IMG_WIDTH, IMG_HEIGHT);
        //int [] face = processor.robustReadBufferedImage(image, IMG_WIDTH, IMG_HEIGHT);
        TrainingDataHolder saved = TrainingDataHolder.load();
        double[] res = Eigenface.recognizeSingle(face, saved);
        int facepn = personFacesNumbers.get((int)res[1]);
        return people.get(facepn);
    }

    public void save() {
        try {
            File f = new File("data/dataholder.ser");
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            fos.flush();
            fos.close();
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataHolder restore() {
        try {
            File f = new File("data/dataholder.ser");
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DataHolder dh = (DataHolder) ois.readObject();
            int[][] matrix = dh.collectFaceMatrix();
            TrainingDataHolder tdh = Eigenface.train(matrix, IMG_WIDTH, IMG_HEIGHT);
            tdh.save();
            return dh;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
