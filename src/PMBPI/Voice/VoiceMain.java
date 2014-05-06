package PMBPI.Voice;

import PMBPI.Voice.feature.FeatureVector;
import org.apache.commons.math3.complex.Complex;

import java.io.File;

/**
 * Created by egor on 5/6/14.
 */
public class VoiceMain {

    public static void main(String[] args) {
        WaveData wd = new WaveData();
        File testFile = new File("audio_data/male_audio/M_1298636292/1298636292_1.wav");
        float[] amplitude = wd.extractAmplitudeFromFile(testFile);
        PreProcess pp = new PreProcess(amplitude, 16, 8000);
        //float[] tt = new float[]
        FeatureExtract fe = new FeatureExtract(pp.framedSignal, 8000, 16);
        fe.makeMfccFeatureVector();
        FeatureVector fv = fe.getFeatureVector();
        double [][] mfccs = fv.getMfccFeature();
        for (double[] d : mfccs) {
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            System.out.println();
        }
    }
}
