package PMBPI.Face;

import PMBPI.Support.DataHolder;

import java.io.*;

/**
 * Created by egor on 5/19/14.
 */
public class TrainingDataHolder implements Serializable {
    public double [][] weights;
    public double [][] eigenvectors;
    public int [] mean;
    public TrainingDataHolder (double[][] weights, double[][] eigenvectors, int[] mean) {
        this.weights = weights;
        this.eigenvectors = eigenvectors;
        this.mean = mean;
    }
    public int save() {
        try {
            File store = new File("data/faceTrainingData.ser");
            FileOutputStream fos = new FileOutputStream(store);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            return DataHolder.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return DataHolder.NULL_FILE_ERROR;
        }
    }

    public static TrainingDataHolder load() {
        try {
            File store = new File("data/faceTrainingData.ser");
            FileInputStream fis = new FileInputStream(store);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (TrainingDataHolder)ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
