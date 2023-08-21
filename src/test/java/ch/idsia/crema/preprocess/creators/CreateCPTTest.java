package ch.idsia.crema.preprocess.creators;

import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateCPTTest {

    private final CreateCPT creator = new CreateCPT();

    @Test
    public void testCreate() {

        //BMI example
        double[] cutsH = new double[]{1.55, 1.60, 1.65, 1.70, 1.75, 1.80, 1.85, 1.90, 1.95, 2.00};
        double[] cutsW = new double[]{55.0, 60.0, 65.0, 70.0, 75.0, 80.0, 85.0, 90.0, 95.0, 100.0, 105.0, 110.0, 115.0};
        double[] cutsBMI = new double[]{0.0, 15.0, 16, 18.5, 25.0, 30.0, 35.0, 40.0, 100.0};

        double[][] parents = new double[][]{cutsW, cutsH};
        Op bmi = (w, h) -> w / h / h;

        IntervalFactor cpt = creator.create(2, new int[]{0, 1}, cutsBMI, parents, bmi);
        //System.out.println(cpt);

        // w1 and h1
        assertArrayEquals(cpt.getLower(0, 0), new double[10]);
        assertArrayEquals(cpt.getUpper(0, 0), creator.createUpper(new int[]{4}, 10));

        // w2 and h1
        assertArrayEquals(cpt.getLower(1, 0), new double[10]);
        assertArrayEquals(cpt.getUpper(1, 0), creator.createUpper(new int[]{4, 5}, 10));

        // w5 and h6
        assertArrayEquals(cpt.getLower(4, 5), new double[10]);
        assertArrayEquals(cpt.getUpper(4, 5), creator.createUpper(new int[]{4}, 10));

        // w12 and h9
        assertArrayEquals(cpt.getLower(11, 8), new double[10]);
        assertArrayEquals(cpt.getUpper(11, 8), creator.createUpper(new int[]{5, 6}, 10));
    }

    @Test
    public void testBorders() {
        double[] parentIntervalLower = new double[]{55.0, 1.55};
        double[] parentIntervalUpper = new double[]{60.0, 1.60};
        //example of the BMI
        Op bmi = (w, h) -> w / h / h;

        //bmi low = 21.48
        //bmi high = 24.97
        double tolerance = 0.001;
        double[] interval = creator.borders(parentIntervalLower, parentIntervalUpper, bmi);

        assertArrayEquals(new double[]{21.484, 24.973}, interval, tolerance);
    }

    @Test
    public void testWhichPosition() {
        double[] intervals = new double[]{0.0, 15.0, 16, 18.5, 25.0, 30.0, 35.0, 40.0, 100.0};
        assertEquals(1, creator.whichPosition(intervals, -10));
        assertEquals(1, creator.whichPosition(intervals, 11));
        assertEquals(1, creator.whichPosition(intervals, 15));
        assertEquals(3, creator.whichPosition(intervals, 17));
        assertEquals(5, creator.whichPosition(intervals, 29));
        assertEquals(8, creator.whichPosition(intervals, 41));
        assertEquals(8, creator.whichPosition(intervals, 200));
    }

    @Test
    public void testCreateUpper() {
        int[] interval = new int[]{3, 4};
        double[] result = creator.createUpper(interval, 4);
        double[] expected = new double[]{.0, .0, 1.0, 1.0};

        assertArrayEquals(result, expected);
    }
}
