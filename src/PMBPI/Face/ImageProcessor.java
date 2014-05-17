package PMBPI.Face;

import PMBPI.Support.DataHolder;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.io.*;

/**
 * Created by egor on 5/17/14.
 */
public class ImageProcessor {
    public ImageProcessor () {

    }
    public BufferedImage convertToGrayScale (BufferedImage image) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp converter = new ColorConvertOp(cs, null);
        BufferedImage grayScaleImage = converter.filter(image, null);
        return grayScaleImage;
    }
    public void writePGM(BufferedImage coloredImage, File destination) {
        try {
            RescaleOp rescaleOp = new RescaleOp(1.35f, 15, null);
            rescaleOp.filter(coloredImage, coloredImage);
            BufferedImage image = convertToGrayScale(coloredImage);
            OutputStream osw = new BufferedOutputStream(new FileOutputStream(destination));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(osw));
            DataOutputStream dos = new DataOutputStream(osw);//
            dos.writeBytes("P5\n"+image.getWidth()+" "+ image.getHeight()+"\n255\n");

            Raster raster = image.getData();
            for (int y = 0; y < raster.getHeight(); y ++) {
                for (int x = 0; x < raster.getWidth(); x ++) {
                    int gray = raster.getSample(x, y, 0);
                    dos.writeByte(gray);
                }
            }
            /*for (int y = 0; y < image.getHeight(); y++)
            {
                for (int x = 0; x < image.getWidth(); x++)
                {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = (rgb & 0xFF);
                    int gray = (r + g + b) / 3;
                    dos.writeByte(gray);
                }
            }*/
            dos.flush();
            dos.close();
            bw.close();
            osw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isImageParamsCorrect(BufferedImage image, int width, int height) {
        if (image.getHeight() == height && image.getWidth() == width) return true;
        return false;
    }
    public boolean isPGMImageParamsCorrect(String img, int width, int height) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(img));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            DataInputStream dis = new DataInputStream(is);
            int x = 0, y = 0;

            int counter = 3;
            boolean bb = false;

            String xx = "";
            String yy = "";
            while (counter != 0) {
                char c = (char) dis.readUnsignedByte();
                if (counter == 2) {
                    if (!bb) {
                        if (c != ' ')
                            xx += c;
                        else
                            bb = !bb;
                    } else {
                        if (c != '\n')
                            yy += c;
                    }
                }
                if (c == '\n')
                    counter--;
            }
            x = Integer.parseInt(xx);
            y = Integer.parseInt(yy);
            if (x == width && y == height) return true;
            else return false;
        } catch (Exception e ) {
            return false;
        }
    }
    private int[] readBufferedImage(BufferedImage image) {
        int [] imageArray = new int[image.getWidth() * image.getHeight()];
        RescaleOp rescaleOp = new RescaleOp(1.35f, 15, null);
        rescaleOp.filter(image, image);
        BufferedImage grayScale = convertToGrayScale(image);
        Raster raster = image.getData();
        int i = 0;
        for (int y = 0; y < raster.getHeight(); y ++) {
            for (int x = 0; x < raster.getWidth(); x ++) {
                int gray = raster.getSample(x, y, 0);
                imageArray[i] = gray;
                i++;
            }
        }
        return imageArray;
    }
    public int[] readFaceFromFile(String path, int width, int height) {
        int[] result = null;
        if (path.toLowerCase().endsWith("pgm")) {
            try {
                if (isPGMImageParamsCorrect(path, width, height))
                    result = Eigenface.readFace(path);
                else return null;
            }catch (Exception e) {
                return null;
            }
        }
        if (path.toLowerCase().endsWith("png")) {
            BufferedImage img = readPNG(path);
            if (img == null) return null;
            return robustReadBufferedImage(img, width, height);
        }
        return null;
    }
    public int[] robustReadBufferedImage(BufferedImage image, int width, int height) {
        if (!isImageParamsCorrect(image, width, height)) return null;
        return readBufferedImage(image);
    }
    private BufferedImage readPNG(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return img;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
