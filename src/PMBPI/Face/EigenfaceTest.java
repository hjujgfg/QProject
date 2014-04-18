package PMBPI.Face;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by egor on 4/15/14.
 */
public class EigenfaceTest extends TestCase {
    @Test
    public void testReadFaces() throws Exception {

    }

    @Test
    public void testMatrixMult() throws Exception {
        int [][] a = {{1, 2, 3}, {4, 5, 6}};
        int [][] b = {{2, 3}, {4, 5}, {6, 7}};
        int [][] exp = {{28, 34}, {64, 79}};
        int [][] res = Eigenface.matrixMult(a, b);
        for (int i = 0; i < exp.length; i ++) {
            for (int j = 0; j < exp[0].length; j ++) {
                assertEquals("werg", exp[i][j], res[i][j]);
            }
        }
    }

    @Test
    public void testTranspose() throws Exception {
        int [][] in = {{1, 2, 3}, {4, 5, 6}};
        int [][] res = {{1, 4}, {2, 5}, {3, 6}};
        int [][] out = Eigenface.transpose(in);
        for (int i = 0; i < res.length; i ++) {
            for (int j = 0; j < res[0].length; j ++) {
                assertEquals("test "+ i + " " + j, res[i][j], out[i][j]);
            }
        }

    }
}
