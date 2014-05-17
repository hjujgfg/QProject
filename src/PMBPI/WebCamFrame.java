package PMBPI;

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

/**
 * Created by Егор on 24.04.2014.
 */
public class WebCamFrame extends JFrame{
    private JPanel rootPanel;
    private JButton takeShotBtn;
    private JPanel camPanel;
    private Webcam webcam;
    public static void main(String[] params) {
        WebCamFrame fc = new WebCamFrame();
    }
    public WebCamFrame() {
        super("PMBPI Project");
        BasicConfigurator.configure();
        setContentPane(rootPanel);
        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                webcam.close();
            }
        });
        webcam = Webcam.getDefault();
        camPanel.setLayout(new GridLayout(1,1));
        final WebcamPanel panel = new WebcamPanel(webcam);
        camPanel.add(panel);
        panel.start();

        takeShotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get image
                BufferedImage image = webcam.getImage();

                // save image to PNG file
                try {
                    ImageIO.write(image, "PNG", new File("test.png"));
                    ImageIcon icon = new ImageIcon("test.png");
                    ImageProcessor processor = new ImageProcessor();
                    processor.writePGM(image, new File("test.pgm"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        setVisible(true);
    }
}
