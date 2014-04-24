package PMBPI.Face;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.IOException;
import java.util.Hashtable;
import java.io.File;
/**
 * Created by Егор on 24.04.2014.
 */
public class PGMReaderAlt {
    //File f;
    ImageInputStream in;
    /** All images have the same width.*/
    private int width = -1;
    /** All images have the same height.*/
    private int height = -1;
    /** All images have the same depth. Must be in the range [1,65535].*/
    private int maxGray = -1;
    private long dataOffset = -1;
    public PGMReaderAlt(File f) {
        try {
            in = new FileImageInputStream(f);
            //maxGray = 255;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage read()
            throws IOException {
        readHeader();



        //ImageInputStream in = (ImageInputStream) getInput();
        in.seek(dataOffset);// + width * height * (maxGray > 255 ? 2 : 1));

        ComponentColorModel ccm = new ComponentColorModel(//
                new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY)),
                new int[]{maxGray > 255 ? 16 : 8},//
                false, false, Transparency.OPAQUE,//
                (maxGray > 255) ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_BYTE);
        SampleModel sm = ccm.createCompatibleSampleModel(width, height);

        BufferedImage img;
        if (maxGray > 255) {
            DataBufferShort db = new DataBufferShort(width * height);
            in.readFully(db.getData(), 0, width * height);
            img = new BufferedImage(ccm, Raster.createWritableRaster(sm, db, new Point(0, 0)), false, new Hashtable());
        } else {
            DataBufferByte db = new DataBufferByte(width * height);
            in.readFully(db.getData(), 0, width * height);
            img = new BufferedImage(ccm, Raster.createWritableRaster(sm, db, new Point(0, 0)), false, new Hashtable());
        }

        return img;
    }
    private void readHeader() throws IOException {
        if (dataOffset == -1) {

            //ImageInputStream in = (ImageInputStream) getInput();
            //ImageInputStream in = new FileImageInputStream(f);
            in.seek(0);

            // Check if file starts with "P5"
            if (in.readShort() != 0x5035) {
                in.reset();
                throw new IOException("Illegal magic number");
            }
            // Skip whitespace (blank, TAB, CR or LF)
            int b = in.readUnsignedByte();
            if (b != 0x20 && b != 0x09 && b != 0x0d && b != 0x0a) {
                throw new IOException("Whitespace missing after magic number");
            }
            // Read width
            width = readHeaderValue(in, "image width");
            if (width < 1) {
                throw new IOException("Illegal image width " + width);
            }
            height = readHeaderValue(in, "image height");
            if (height < 1) {
                throw new IOException("Illegal image width " + height);
            }
            maxGray = readHeaderValue(in, "maximum gray value");
            if (maxGray < 2 || maxGray > 65536) {
                throw new IOException("Illegal maximum gray value " + maxGray);
            }
            dataOffset = in.getStreamPosition();
        }
    }

    private int readHeaderValue(ImageInputStream in, String name) throws IOException {
        // Skip whitespace (blank, TAB, CR or LF) and comments
        int b;
        do {
            b = in.readUnsignedByte();

            if (b == '#') { // comments
                do {
                    b = in.readUnsignedByte();
                } while (b != 0x0d && b != 0x0a);
            }
        } while (b == 0x20 || b == 0x09 || b == 0x0d || b == 0x0a);

        // read value
        if (b < 0x30 || b > 0x39) {
            throw new IOException(name + " missing");
        }
        int value = 0;
        do {
            if (value >= 100000) {
                throw new IOException(name + " is too large");
            }
            value = value * 10 + b - 0x30;
        } while ((b = in.readUnsignedByte()) >= 0x30 && b <= 0x39);
        if (b != 0x20 && b != 0x09 && b != 0x0d && b != 0x0a) {
            throw new IOException("Whitespace after " + name + " missing");
        }
        return value;
    }
}
