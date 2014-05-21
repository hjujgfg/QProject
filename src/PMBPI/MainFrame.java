package PMBPI;

import PMBPI.Face.Eigenface;
import PMBPI.Face.ImageProcessor;
import PMBPI.Face.PGMReaderAlt;
import PMBPI.Face.TrainingDataHolder;
import PMBPI.Support.DataHolder;
import PMBPI.Support.Person;
import PMBPI.Voice.Microphone;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.BasicConfigurator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;



/**
 * Created by egor on 5/14/14.
 */
public class MainFrame extends JFrame{
    private JPanel topPanel;
    private JPanel MainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel CameraPlace;
    private JButton catureImageButton;
    private JPanel IdentifiedFacePanel;
    private JPanel dataSetPanel;
    private JLabel voceCaptureLabel;
    private JPanel testSignalPanel;
    static final int CAM_DIM_WIDTH = 176;
    static final int CAM_DIM_HEIGHT = 144;
    static final int PREFERRED_SIZE_W = 92;
    static final int PREFERRED_SIZE_H = 112;

    final Webcam webcam;
    DataHolder dataHolder;
    Thread realTimeFaceIdentificationThread;
    Thread soundCaptureThread;
    public MainFrame() {
        super("PMBPI Project");
        BasicConfigurator.configure();
        setContentPane(MainPanel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                webcam.close();
                try {
                    realTimeFaceIdentificationThread.interrupt();
                    dataHolder.save();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        dataHolder = DataHolder.restore();
        if (dataHolder == null) {
            dataHolder = new DataHolder();
            trainDataHolder(dataHolder);
        }
        dataHolder.setIMGDimensions(92, 112);
        dataHolder.setSoundCapureParams(16, 16000);

        String[] voice = {"audio_data/16khz_16bit/1/7.wav"};
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(176, 144));
        CameraPlace.setLayout(new GridBagLayout());
        final WebcamPanel webcamPanel = new WebcamPanel(webcam, new Dimension(176, 144), true);
        MyWebCamPainter painter = new MyWebCamPainter();
        webcamPanel.setPainter(painter);
        CameraPlace.add(webcamPanel);

        setVisible(true);
        catureImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage image = webcam.getImage();
                try {
                    ImageIO.write(image, "PNG", new File("test.png"));
                    ImageIcon icon = new ImageIcon("test.png");
                    ImageProcessor processor = new ImageProcessor();
                    processor.writePGM(image, new File("test.pgm"));
                    image = processor.rescaleImageBrightness(image, 1.3f, 15);
                    BufferedImage gray = processor.convertToGrayScale(image);
                    Eigenface.writeToPGM(processor.extractInnerImage(gray, 92, 112), 92, 112, "teteteteteest.pgm");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        IdentifiedFacePanel.setLayout(new GridBagLayout());
        realTimeFaceIdentificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        BufferedImage image = webcam.getImage();
                        ImageProcessor processor = new ImageProcessor();
                        image = processor.rescaleImageBrightness(image, 1.3f, 15);
                        BufferedImage gray = processor.convertToGrayScale(image);
                        Person p = dataHolder.identifyByImage(gray);
                        IdentifiedFacePanel.removeAll();
                        IdentifiedFacePanel.add(new PreviewPanel(p.getUserPic()));
                        IdentifiedFacePanel.add(new CepstraPreviewPanel(p.getVoiceCentroids()[0], 200, 100));
                        IdentifiedFacePanel.revalidate();
                        IdentifiedFacePanel.repaint();

                    }
                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        });

        realTimeFaceIdentificationThread.start();

        soundCaptureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                    
            }
        });
        dataSetPanel.setLayout(new GridBagLayout());
        for (Person p : dataHolder.getPeople()) {
            dataSetPanel.add(new PreviewPanel(p.getUserPic()));
        }

    }
    private void trainDataHolder(DataHolder dh) {
        dh.setIMGDimensions(92, 112);
        dh.setSoundCapureParams(16, 16000);
        int train = 3;
        int test = 5;
        String[][] perFaces = new String[train][2];
        for (int i = 0; i < train; i ++) {
            for (int j = 0; j < 2; j ++) {
                perFaces[i][j] = "faces/s" + (i+1) + "/" + (j+1) + ".pgm";
            }
        }
        String[][] perVoices = new String[train][7];
        for (int i = 0; i < train; i ++) {
            for (int j = 0; j < 7; j ++) {
                perVoices[i][j] = "audio_data/16khz_16bit/"+(i+1)+"/"+j+".wav";
            }
        }
        for (int i = 0; i < train; i ++) {
            dh.addPerson("pers#" + (i+1), perFaces[i], perVoices[i]);
        }
        int[][] matr = dh.collectFaceMatrix();
        TrainingDataHolder tdh = Eigenface.train(matr, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT);
        tdh.save();
    }
    private class MyWebCamPainter implements WebcamPanel.Painter {
        @Override
        public void paintPanel(WebcamPanel webcamPanel, Graphics2D graphics2D) {
            webcamPanel.getDefaultPainter().paintPanel(webcamPanel, graphics2D);
            int x = CAM_DIM_WIDTH - PREFERRED_SIZE_W;
            x /= 2;
            int y = CAM_DIM_HEIGHT - PREFERRED_SIZE_H;
            y /= 2;
            graphics2D.drawRect(x, y, PREFERRED_SIZE_W, PREFERRED_SIZE_H);
        }

        @Override
        public void paintImage(WebcamPanel webcamPanel, BufferedImage image, Graphics2D graphics2D) {
            webcamPanel.getDefaultPainter().paintImage(webcamPanel, image, graphics2D);
            int x = CAM_DIM_WIDTH - PREFERRED_SIZE_W;
            x /= 2;
            int y = CAM_DIM_HEIGHT - PREFERRED_SIZE_H;
            y /= 2;
            graphics2D.drawRect(x, y, PREFERRED_SIZE_W, PREFERRED_SIZE_H);
            graphics2D.drawLine(x, y + PREFERRED_SIZE_H / 2, x + PREFERRED_SIZE_W, y + PREFERRED_SIZE_H / 2);
        }
    }
    public static void main(String[] args) {
        new MainFrame();
    }
    private class PreviewPanel extends JPanel{

        private BufferedImage image;
        File pathToImage;
        public File getPathToImage() {
            return pathToImage;
        }

        public PreviewPanel(File f) {
            try {
                PGMReaderAlt readerAlt = new PGMReaderAlt(f);
                image = readerAlt.read();
                this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                pathToImage = f;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
        }
    }

    private class CepstraPreviewPanel extends JPanel{
        private double[] coefs;
        int width;
        int height;
        public CepstraPreviewPanel(double [] coefficients, int width, int height) {
            coefs = coefficients;
            this.width = width;
            this.height = height;
            this.setPreferredSize(new Dimension(width, height));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            RealVector rv = new ArrayRealVector(coefs);
            int max = (int)rv.getMaxValue();
            int interval = (width - 5) / coefs.length;
            for (int i = 0; i < coefs.length; i ++) {
                g.drawRect(i * interval, (int) (coefs[i] * (height - 10) / max), 2, 2);
            }
            g.drawLine(3, 0, 0, height);
            g.drawLine(3, height, width, height);
        }
    }
}
