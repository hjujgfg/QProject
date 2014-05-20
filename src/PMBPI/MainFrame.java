package PMBPI;

import PMBPI.Face.Eigenface;
import PMBPI.Face.ImageProcessor;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
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
    static final int CAM_DIM_WIDTH = 176;
    static final int CAM_DIM_HEIGHT = 144;
    static final int PREFERRED_SIZE_W = 92;
    static final int PREFERRED_SIZE_H = 112;

    Webcam webcam;
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
            }
        });
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
                    BufferedImage gray = processor.convertToGrayScale(image);
                    Eigenface.writeToPGM(processor.extractInnerImage(gray, 92, 112), 92, 112, "teteteteteest.pgm");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
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
        }
    }
    public static void main(String[] args) {
        new MainFrame();
    }
}
