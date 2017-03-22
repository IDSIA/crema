package ch.idsia.crema.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.idsia.crema.utility.IndexIterator;

public class StridesTest {

	
	
	@SuppressWarnings("deprecation")
	public void testFilteredIterator() {
		Strides domain = new Strides(new int[] { 0, 1, 2, 3 }, new int[] { 2, 3, 4, 2 });
		
		IndexIterator iterator1 = domain.getFiteredIndexIterator(1, 2);
		//int offset = domain.getPartialOffset(vars, states)
		//IndexIterator iterator2 = domain.getIterator(filtered.getVariable()).offset(offset);

	}
	
	
	@Test
	public void testEmpty() {
		Strides e1 = Strides.as();
		assertEquals(0, e1.getSize());
		
		e1 = DomainBuilder.var().size().strides();
		assertEquals(0, e1.getSize());

		e1 = Strides.empty();
		assertEquals(0, e1.getSize());
	}
	
	@Test
	public void testSplitOuterIteration() {
		Strides domain1 = new Strides(new int[] { 0, 1, 2, 3 }, new int[] { 2, 3, 4, 2 });
		Strides domain2 = new Strides(new int[] { 0, 2 }, new int[] { 2, 4 });
		Strides domain3 = new Strides(new int[] { 1, 3 }, new int[] { 3, 2 });
		
		IndexIterator sub2 = domain1.getIterator(domain1, domain3.getVariables()); // iterate with locked domain3
		IndexIterator sub3 = domain1.getIterator(domain1, domain2.getVariables());
		IndexIterator iter = domain1.getIterator(domain1);
		int index = 0;
		while(iter.hasNext()) {
			int d1 = iter.next();
			int d2 = sub2.next();
			int d3 = sub3.next();
			assertEquals(d1, d2 + d3);
			assertEquals(index, d1);
			++index;
		}
	}
	
	@Test
	public void testSplitInnerIteration() {
		Strides domain1 = new Strides(new int[] { 0, 1, 2, 3 }, new int[] { 2, 3, 4, 2 });
		Strides domain2 = new Strides(new int[] { 0, 2 }, new int[] { 2, 4 });
		Strides domain3 = new Strides(new int[] { 1, 3 }, new int[] { 3, 2 });
		
		int count = 0;
		IndexIterator sub2 = domain1.getIterator(domain2); // iterate with locked domain3
		while(sub2.hasNext()) {
			int[] vals = sub2.getPositions();
			int off2 = domain1.getPartialOffset(domain2.getVariables(), vals);
			int d2 = sub2.next();
			
			IndexIterator sub3 = domain1.getIterator(domain3);
			while(sub3.hasNext()) {
				vals = sub3.getPositions();
				int off3 = domain1.getPartialOffset(domain3.getVariables(), vals);
				int d3 = sub3.next();
				
				assertEquals(off3 + off2, d2 + d3);
				++count;
			}
		}
		
		assertEquals(domain1.getCombinations(), count);
	}
	
	/** test ne getIterator method agains older method 
	 *
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetDomain() {
		Strides s1 = new Strides(new int[] { 1, 2 }, new int[] { 2, 3 });
		
		// reduce iterated domain to var 2 only
		Strides s2 = new Strides(new int[] { 2 }, new int[] { 3 });
		IndexIterator iter = s1.getIterator(s2);
		assertArrayEquals(new int[] { 0, 2, 4 }, iter.toArray());
		
		// iterate over additional variable 
		Strides s3 = new Strides(new int[] { 0, 1, 2 }, new int[] { 2, 2, 3 });
		iter = s1.getIterator(s3);
		assertArrayEquals(new int[] { 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5 }, iter.toArray());

		// iterate over additional variable (last) 
		Strides s4 = new Strides(new int[] { 1, 2, 3 }, new int[] { 2, 3, 2 });
		iter = s1.getIterator(s4);
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5 }, iter.toArray());
		iter = s1.getSupersetIndexIterator(s4);
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5 }, iter.toArray());
		
		// iterate over additional variable and remove var 2  
		Strides s5 = new Strides(new int[] { 0, 1 }, new int[] { 2, 2 });
		iter = s1.getIterator(s5);
		assertArrayEquals(new int[] { 0, 0, 1, 1 }, iter.toArray());
		
		// iterate over additional variable and remove var 2  
		Strides s6 = new Strides(new int[] { 0, 2 }, new int[] { 2, 3 });
		iter = s1.getIterator(s6);
		assertArrayEquals(new int[] { 0, 0, 2, 2, 4, 4 }, iter.toArray());
				
		int o = s1.getPartialOffset(new int[] { 2 }, new int[] { 1 });
		//System.out.println(o);
	}
	
	@Test
	public void testPartialOffset() {
		Strides s4 = new Strides(new int[] { 1, 2, 3 }, new int[] { 2, 3, 2 });
		
		int o1 = s4.getOffset(new int[] { 0, 2, 1 });
		int p1 = s4.getPartialOffset(new int[] { 2, 3 }, new int[] { 2, 1 });
		assertEquals(o1, p1);
		
		// small domain
		o1 = s4.getOffset(new int[] { 0, 1, 0 });
		p1 = s4.getPartialOffset(new int[] { 2 }, new int[] { 1 });
		assertEquals(o1, p1);

		// partial over different domain (unknown vars are ignored!!!)
		o1 = s4.getOffset(new int[] { 0, 1, 0 });
		p1 = s4.getPartialOffset(new int[] { 0, 2, 5 }, new int[] { 4, 1, 6 });
		assertEquals(o1, p1);

		// over empty domain
		p1 = s4.getPartialOffset(new int[] { }, new int[] { });
		assertEquals(0, p1);
		
		// partial of empty domain
		s4 = new Strides(new int[0], new int[0]);
		p1 = s4.getPartialOffset(new int[] { }, new int[] { });
		assertEquals(0, p1);
		p1 = s4.getPartialOffset(new int[] { 1, 2 }, new int[] { 5, 2 });
		assertEquals(0, p1);
	}
	
	
	/**
	 * The the UNION of domains
	 */
	@Test
	public void testUnion() {
		// union test
		Strides s1 = new Strides(new int[] { 0, 1, 2 }, new int[] { 2, 2, 3 });
		Strides s2 = new Strides(new int[] { 0, 2, 4 }, new int[] { 2, 3, 3 });
		Strides s3 = s1.union(s2);

		assertEquals(4, s3.getSize());
		assertArrayEquals(new int[] { 0, 1, 2, 4 }, s3.getVariables());
		assertArrayEquals(new int[] { 2, 2, 3, 3 }, s3.getSizes());
		assertEquals(36, s3.getCombinations());

		s1 = new Strides(new int[] { 0, 1 }, new int[] { 2, 2 });
		s2 = new Strides(new int[] { 0, 2, 4 }, new int[] { 2, 3, 3 });
		s3 = s1.union(s2);

		assertEquals(4, s3.getSize());
		assertArrayEquals(new int[] { 0, 1, 2, 4 }, s3.getVariables());
		assertArrayEquals(new int[] { 2, 2, 3, 3 }, s3.getSizes());
		assertEquals(36, s3.getCombinations());
	}

	/**
	 * The the INTERSECTION of domains
	 */
	@Test
	public void testIntersect() {
		// union test
		Strides s1 = new Strides(new int[] { 0, 1, 2 }, new int[] { 2, 2, 3 });
		Strides s2 = new Strides(new int[] { 0, 2, 4 }, new int[] { 2, 3, 3 });
		Strides s3 = s1.intersection(s2);

		assertEquals(2, s3.getSize());
		assertArrayEquals(new int[] { 0, 2,}, s3.getVariables());
		assertArrayEquals(new int[] { 2, 3 }, s3.getSizes());
		assertEquals(6, s3.getCombinations());

		// empty intersection
		s1 = new Strides(new int[] { 0, 1 }, new int[] { 2, 2 });
		s2 = new Strides(new int[] { 2, 4 }, new int[] { 3, 3 });
		s3 = s1.intersection(s2);

		assertEquals(0, s3.getSize());
		assertArrayEquals(new int[] { }, s3.getVariables());
		assertArrayEquals(new int[] { }, s3.getSizes());
		assertEquals(1, s3.getCombinations());
		
		// complete overlap
		s3 = s2.intersection(s2);
		assertEquals(s2.getSize(), s3.getSize());
		assertArrayEquals(s2.getVariables(), s3.getVariables());
		assertArrayEquals(s2.getSizes(), s3.getSizes());
		assertEquals(s2.getCombinations(), s3.getCombinations());
	}
	
	
	/**
	 * Test variable removal from stride
	 */
	@Test
	public void testStridesStridesInt() {
		Strides s1 = new Strides(new int[] { 0, 1, 2 }, new int[] { 2, 2, 3 });
		Strides s2 = s1.removeAt(1);
		assertEquals(2, s2.getSize());

		assertArrayEquals(new int[] { 0, 2 }, s2.getVariables());
		assertArrayEquals(new int[] { 2, 3 }, s2.getSizes());
		assertArrayEquals(new int[] { 1, 2, 6 }, s2.getStrides());
		assertEquals(6, s2.getCombinations());
	}

	
	@SuppressWarnings("deprecation")
	@Test
	public void testRemoveAt() {
		Strides s1 = new Strides(new int[] { 0, 2, 3, 9 }, new int[] { 2, 2, 3, 7 });
		
		Strides n1 = new Strides(s1, 1);
		Strides n2 = s1.removeAt(1);
		
		assertArrayEquals(n1.getVariables(), n2.getVariables());
		assertArrayEquals(n1.getSizes(), n2.getSizes());
		
		n1 = new Strides(s1, 1);
		n1 = new Strides(n1, 2);
		n2 = s1.removeAt(1, 3);
		
		assertArrayEquals(n1.getVariables(), n2.getVariables());
		assertArrayEquals(n1.getSizes(), n2.getSizes());
		
		n2 = s1.removeAt(0, 1, 2, 3);
		assertArrayEquals(new int[0], n2.getVariables());
		assertArrayEquals(new int[0], n2.getSizes());
		assertEquals(1, n2.getCombinations());
	}
	
	@Test
	public void testStatesOf() {

		Strides s1 = new Strides(new int[] { 0, 2, 3 }, new int[] { 2, 2, 3 });
		assertArrayEquals(new int[] { 0, 0, 0 }, s1.statesOf(0));
		assertArrayEquals(new int[] { 1, 1, 2 }, s1.statesOf(s1.getCombinations() - 1));
		assertArrayEquals(new int[] { 1, 0, 0 }, s1.statesOf(1));
		assertArrayEquals(new int[] { 0, 1, 0 }, s1.statesOf(2));
		assertArrayEquals(new int[] { 1, 0, 1 }, s1.statesOf(5));
		assertArrayEquals(new int[] { 0, 0, 2 }, s1.statesOf(8));
		assertArrayEquals(new int[] { 1, 0, 2 }, s1.statesOf(9));
		assertArrayEquals(new int[] { 0, 1, 2 }, s1.statesOf(10));

		s1 = new Strides(new int[] { 0, 2, 3 }, new int[] { 2, 3, 2 });
		assertArrayEquals(new int[] { 0, 0, 0 }, s1.statesOf(0));
		assertArrayEquals(new int[] { 1, 0, 0 }, s1.statesOf(1));
		assertArrayEquals(new int[] { 0, 1, 0 }, s1.statesOf(2));
		assertArrayEquals(new int[] { 1, 1, 0 }, s1.statesOf(3));
		assertArrayEquals(new int[] { 0, 2, 0 }, s1.statesOf(4));
		assertArrayEquals(new int[] { 1, 2, 0 }, s1.statesOf(5));
		assertArrayEquals(new int[] { 0, 0, 1 }, s1.statesOf(6));
		assertArrayEquals(new int[] { 1, 0, 1 }, s1.statesOf(7));
		assertArrayEquals(new int[] { 0, 1, 1 }, s1.statesOf(8));
		assertArrayEquals(new int[] { 1, 1, 1 }, s1.statesOf(9));
		assertArrayEquals(new int[] { 0, 2, 1 }, s1.statesOf(10));
		assertArrayEquals(new int[] { 1, 2, 1 }, s1.statesOf(11));
	}

	@Test
	public void testGetOffset() {
		Strides s1 = new Strides(new int[] { 0, 1, 2 }, new int[] { 2, 2, 3 });
		assertEquals(0, s1.getOffset(new int[] { 0, 0, 0 }));
		assertEquals(s1.getCombinations() - 1, s1.getOffset(new int[] { 1, 1, 2 }));
		assertEquals(1, s1.getOffset(new int[] { 1, 0, 0 }));
		assertEquals(2, s1.getOffset(new int[] { 0, 1, 0 }));
		assertEquals(4, s1.getOffset(new int[] { 0, 0, 1 }));
		assertEquals(8, s1.getOffset(new int[] { 0, 0, 2 }));
		assertEquals(9, s1.getOffset(new int[] { 1, 0, 2 }));
		assertEquals(10, s1.getOffset(new int[] { 0, 1, 2 }));
	}

	@Test
	public void testGetCombinations() {
		Strides s2 = new Strides(new int[] { 0, 2, 4 }, new int[] { 2, 3, 3 });
		assertEquals(18, s2.getCombinations());
		assertEquals(s2.getCombinations(), s2.getStrideAt(s2.getSize()));
	}

	@Test
	public void testGetReorderedIterator() {
		//fail("Not yet implemented");
	}

}
