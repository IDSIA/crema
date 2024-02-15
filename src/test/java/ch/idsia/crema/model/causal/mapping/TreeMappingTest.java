/**
 * 
 */
package ch.idsia.crema.model.causal.mapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.causal.SCM;
import ch.idsia.crema.model.causal.SCM.VariableType;
import ch.idsia.crema.model.io.dot.DetailedDotSerializer;
import ch.idsia.crema.model.io.dot.Info;

/**
 * 
 */
class TreeMappingTest {
	SCM one;
	SCM two;

	int v1;
	int v2;
	int e1;
	int e2;

	@BeforeEach
	void setup() {
		SCM scm1 = new SCM();
		v1 = scm1.addEndogenous(2);
		v2 = scm1.addEndogenous(2);
		e1 = scm1.addExogenous(2);
		e2 = scm1.addExogenous(2);
		scm1.addParents(v2, v1, e2);
		scm1.addParent(v1, e1);

		var f1 = BayesianFactorFactory.factory().domain(scm1.getFullDomain(v1))
				.data(new int[] { v1, e1 }, new double[] { 0.1, 0.9, 0.3, 0.7 }).get();
		scm1.setFactor(v1, f1);

		var f2 = BayesianFactorFactory.factory().domain(scm1.getFullDomain(v2))
				.data(new int[] { v2, v1, e2 }, new double[] { 0.4, 0.6, 0.2, 0.8, 0.1, 0.9, 0.7, 0.3 }).get();
		scm1.setFactor(v2, f2);

		var f3 = BayesianFactorFactory.factory().domain(scm1.getFullDomain(e1))
				.data(new int[] { e1 }, new double[] { 0.25, 0.75 }).get();
		scm1.setFactor(e1, f3);

		var f4 = BayesianFactorFactory.factory().domain(scm1.getFullDomain(e2))
				.data(new int[] { e2 }, new double[] { 0.2, 0.8 }).get();
		scm1.setFactor(e2, f4);

		SCM scm2 = new SCM();
		scm2.addEndogenous(v1, 2);
		scm2.addEndogenous(v2, 2);
		scm2.addExogenous(e1, 2);
		scm2.addExogenous(e2, 2);

		scm2.addParent(v2, e2);
		scm2.addParent(v1, e1);

		f1 = BayesianFactorFactory.factory().domain(scm2.getFullDomain(v1))
				.data(new int[] { v1, e1 }, new double[] { 0.45, 0.55, 0.35, 0.65 }).get();
		scm2.setFactor(v1, f1);

		f2 = BayesianFactorFactory.factory().domain(scm2.getFullDomain(v2))
				.data(new int[] { v2, e2 }, new double[] { 0.6, 0.4, 0.15, 0.85 }).get();
		scm2.setFactor(v2, f2);

		f3 = BayesianFactorFactory.factory().domain(scm2.getFullDomain(e1))
				.data(new int[] { e1 }, new double[] { 0.55, 0.45 }).get();
		scm2.setFactor(e1, f3);

		f4 = BayesianFactorFactory.factory().domain(scm2.getFullDomain(e2))
				.data(new int[] { e2 }, new double[] { 0.65, 0.35 }).get();
		scm2.setFactor(e2, f4);

		one = scm1;
		two = scm2;
	}

	/**
	 * Test method for
	 * {@link ch.idsia.crema.model.causal.mapping.TreeMapping#get()}.
	 */
	@Test
	void testGet() {
		TreeMapping tm = new TreeMapping();
		assertNull(tm.get());
		tm.add(one);
		assertNotNull(tm.get());
	}

	static String name(TreeMapping tm, int id) {
		SCM global = tm.get();
		int lid = tm.fromGlobal(id);
		if (global.isExogenous(id)) {
			return "U" + lid;
		}

		int world = tm.worldIdOf(id);
		return "X" + lid + "<sub>" + world + "</sub>";
	}

	/**
	 * Test method for
	 * {@link ch.idsia.crema.model.causal.mapping.TreeMapping#add(ch.idsia.crema.model.causal.SCM)}.
	 */
	@Test
	void testAdd() {

		final TreeMapping tm = new TreeMapping();
		assertNull(tm.get());
		tm.add(one);
		tm.add(two);
		SCM global = tm.get();
		// DetailedDotSerializer.saveModel("test.png", new
		// Info().model(global).nodeName((id)->name(tm, id)));
		assertNotNull(tm.get());
	}

	/**
	 * Test method for
	 * {@link ch.idsia.crema.model.causal.mapping.TreeMapping#toGlobal(int, int)}.
	 */
	@Test
	void testToGlobal() {
		final TreeMapping tm = new TreeMapping();
		int w1 = tm.add(one);
		int w2 = tm.add(two);
		SCM global = tm.get();

		for (int v : one.getVariables()) {
			int g = tm.toGlobal(v, w1);
			assertEquals(tm.fromGlobal(g), v);
		}
		for (int v : two.getVariables()) {
			int g = tm.toGlobal(v, w2);
			assertEquals(tm.fromGlobal(g), v);
		}

	}

	/**
	 * Test method for
	 * {@link ch.idsia.crema.model.causal.mapping.TreeMapping#fromGlobal(int)}.
	 */
	@Test
	void testExogenous() {
		final TreeMapping tm = new TreeMapping();
		int w1 = tm.add(one);
		int w2 = tm.add(two);

		int ge1 = tm.toGlobal(e1, w1);
		int ge2 = tm.toGlobal(e1, w2);
		assertEquals(ge1, ge2);
	}

	/**
	 * Test parenting preservalfor
	 */
	@Test
	void testParenting() {
		final TreeMapping tm = new TreeMapping();
		int w1 = tm.add(one);
		int w2 = tm.add(two);
		SCM global = tm.get();
		for (int v : global.getVariables()) {
			int[] p = global.getParents(v);
			
			SCM w = tm.worldOf(v);
			if (w == null) {
				w = one;
				assert(global.isExogenous(v));
			}
			
			int vs = tm.fromGlobal(v);
			int[] ps = tm.fromGlobal(p);
			int[] pt = w.getParents(vs);
			
			Arrays.sort(ps);
			Arrays.sort(pt);
			assertArrayEquals(ps, pt);
		}
	}


}
