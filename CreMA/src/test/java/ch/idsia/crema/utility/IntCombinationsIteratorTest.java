package ch.idsia.crema.utility;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by giovanni on 29.05.17.
 */
public class IntCombinationsIteratorTest {
    @Test
    public void testNext() {
        int[] dimensions = new int[]{2,3,3};

        IntCombinationsIterator iter = new IntCombinationsIterator(dimensions);
        dimensions[0] = 100;

        assertArrayEquals(new int[]{0,0,0}, iter.next());

        for (int i = 0; i < 6; i++) {
            iter.next();
        }
        assertArrayEquals(new int[]{1,0,1}, iter.next());

        int[] tempArray = null;
        while(iter.hasNext()){
            tempArray = iter.next();
        }
        assertArrayEquals(new int[]{1,2,2}, tempArray);
    }
}
