package ch.idsia.crema.model;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.utility.RandomUtil;

import org.junit.jupiter.api.Test;

import com.google.common.primitives.Doubles;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.rng.sampling.distribution.DirichletSampler;
import org.apache.commons.rng.simple.RandomSource;


public class BayesianFactorTest {

	@Test
	public void testMarginalize() {
		int[] vars = new int[]{1, 2};
		int[] size = new int[]{3, 3};

		double[] vals = new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.10, 0.10};

		BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.get();

		assertArrayEquals(new double[]{0.45, 0.2, 0.35}, ibf.marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.4, 0.2, 0.4}, ibf.marginalize(2).getData(), 1e-7);

		assertArrayEquals(new double[]{1}, ibf.marginalize(2).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{1}, ibf.marginalize(1).marginalize(2).getData(), 1e-7);

		assertArrayEquals(new int[]{}, ibf.marginalize(1).marginalize(2).getDomain().getVariables());

		assertArrayEquals(new int[]{1}, ibf.marginalize(2).getDomain().getVariables());
		assertArrayEquals(new int[]{2}, ibf.marginalize(1).getDomain().getVariables());

		vars = new int[]{1, 2, 3};
		size = new int[]{2, 2, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.20};

		ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.get();

		assertArrayEquals(new double[]{0.2, 0.35, 0.1, 0.35}, ibf.marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.4, 0.15, 0.2, 0.25}, ibf.marginalize(2).getData(), 1e-7);
		assertArrayEquals(new double[]{0.2, 0.10, 0.4, 0.30}, ibf.marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.55, 0.45}, ibf.marginalize(1).marginalize(2).getData(), 1e-7);
		assertArrayEquals(new double[]{0.3, 0.7}, ibf.marginalize(1).marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.55, 0.45}, ibf.marginalize(2).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.6, 0.4}, ibf.marginalize(2).marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.3, 0.7}, ibf.marginalize(3).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.6, 0.4}, ibf.marginalize(3).marginalize(2).getData(), 1e-7);
	}




	@Test
	public void testLogMarginalize() {
		int[] vars = new int[]{1, 2};
		int[] size = new int[]{3, 3};


		double[] vals = new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.10, 0.10};

		BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();

		assertArrayEquals(new double[]{0.45, 0.2, 0.35}, ibf.marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.4, 0.2, 0.4}, ibf.marginalize(2).getData(), 1e-7);

		assertArrayEquals(new double[]{1}, ibf.marginalize(2).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{1}, ibf.marginalize(1).marginalize(2).getData(), 1e-7);

		assertArrayEquals(new int[]{}, ibf.marginalize(1).marginalize(2).getDomain().getVariables());

		assertArrayEquals(new int[]{1}, ibf.marginalize(2).getDomain().getVariables());
		assertArrayEquals(new int[]{2}, ibf.marginalize(1).getDomain().getVariables());

		vars = new int[]{1, 2, 3};
		size = new int[]{2, 2, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.20};

		ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();

		assertArrayEquals(new double[]{0.2, 0.35, 0.1, 0.35}, ibf.marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.4, 0.15, 0.2, 0.25}, ibf.marginalize(2).getData(), 1e-7);
		assertArrayEquals(new double[]{0.2, 0.10, 0.4, 0.30}, ibf.marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.55, 0.45}, ibf.marginalize(1).marginalize(2).getData(), 1e-7);
		assertArrayEquals(new double[]{0.3, 0.7}, ibf.marginalize(1).marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.55, 0.45}, ibf.marginalize(2).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.6, 0.4}, ibf.marginalize(2).marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.3, 0.7}, ibf.marginalize(3).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.6, 0.4}, ibf.marginalize(3).marginalize(2).getData(), 1e-7);
	}

	@Test
	public void testCombineCommutativeProperty() {
		int[] vars = new int[]{1, 2, 3};
		int[] size = new int[]{2, 3, 2};
		double[] vals = new double[]{0.15, 0.85, 0.25, 0.75, 0.95, 0.05, 0.5, 0.5, 0.4, 0.6};

		BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.get();

		vars = new int[]{2, 3};
		size = new int[]{3, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.20, 0.25, 0.1};

		BayesianFactor ibf2 = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.get();

		// P PA = new P(A | B, C);
		// P PA = PA_B * PB - B;

		BayesianFactor r = ibf2.combine(ibf);

		double[] data = r.getData();

		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		r = ibf.combine(ibf2);
		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		assertArrayEquals(data, r.getData(), 1e-9);
	}

	@Test
	public void testLogCombineCommutativeProperty() {
		int[] vars = new int[]{1, 2, 3};
		int[] size = new int[]{2, 3, 2};
		double[] vals = new double[]{0.15, 0.85, 0.25, 0.75, 0.95, 0.05, 0.5, 0.5, 0.4, 0.6};

		BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();

		vars = new int[]{2, 3};
		size = new int[]{3, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.20, 0.25, 0.1};

		BayesianFactor ibf2 = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();

		// P PA = new P(A | B, C);
		// P PA = PA_B * PB - B;

		BayesianFactor r = ibf2.combine(ibf);

		double[] data = r.getData();

		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		r = ibf.combine(ibf2);
		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		assertArrayEquals(data, r.getData(), 1e-9);
	}

	@Test
	public void testCombineDivision() {
		int[] vars = new int[]{1, 2, 3};
		int[] size = new int[]{2, 3, 2};
		double[] vals = new double[]{0.15, 0.85, 0.25, 0.75, 0.95, 0.05, 0.5, 0.5, 0.4, 0.6};

		BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.get();

		vars = new int[]{2, 3};
		size = new int[]{3, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.20, 0.25, 0.1};

		BayesianFactor ibf2 = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.get();

		BayesianFactor r = ibf2.combine(ibf);

		double[] data = r.getData();

		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		r = ibf2.combine(ibf);
		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		assertArrayEquals(data, r.getData(), 1e-9);

		// a * b / b = a
		r = r.divide(ibf2);
		assertEquals(ibf, r);
	}

	@Test
	public void testLogCombineDivision() {
		int[] vars = new int[]{1, 2, 3};
		int[] size = new int[]{2, 3, 2};
		double[] vals = new double[]{0.15, 0.85, 0.25, 0.75, 0.95, 0.05, 0.5, 0.5, 0.4, 0.6};

		BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();

		vars = new int[]{2, 3};
		size = new int[]{3, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.20, 0.25, 0.1};

		BayesianFactor ibf2 = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();

		BayesianFactor r = ibf2.combine(ibf);

		double[] data = r.getData();

		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		r = ibf2.combine(ibf);
		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		assertArrayEquals(data, r.getData(), 1e-9);

		// a * b / b = a
		r = r.divide(ibf2);
		assertEquals(ibf, r);
	}

//	@Test
	public void testtime() {
		int a = 0;
		int b = 1;
		int c = 2;

		Strides d1 = new Strides(new int[]{a,c,b }, new int[]{2,2,3});
		Strides d2 = new Strides(new int[]{b,c}, new int[]{3,2});
		int tests = 50000;
		int reps = 100;
		double div = 1000.0;
		long tot = 0;
		double x = 0;

		for (int rep = -50; rep < reps; ++ rep){
			var rs = RandomSource.JDK.create(0xb00b);
			DirichletSampler sampler2 = DirichletSampler.symmetric(rs, 2, 1);
			DirichletSampler sampler_prior = DirichletSampler.symmetric(rs, 6, 1);
			
	
			double[][] arrpa = sampler2.samples(tests*6).toArray(len->new double[len][]);
			double[][] arrpb = sampler_prior.samples(tests).toArray(len->new double[len][]);
	
			
			for (int i = 0; i < tests; ++i) {
	
				double[] parr = Doubles.concat(arrpa[i*6], arrpa[i*6+1], arrpa[i*6+2], arrpa[i*6+3], arrpa[i*6+4], arrpa[i*6+5]);
				BayesianFactor pa = BayesianFactorFactory.factory().domain(d1).data(parr).log();
				BayesianFactor pb = BayesianFactorFactory.factory().domain(d2).data(arrpb[i]).log();
	
				final long nt = System.nanoTime();
				var fact = pa.combine(pb)
				// .marginalize(c)
				//	.marginalize(b)
					;
				long diff = (System.nanoTime() - nt);
				double[] data = fact.getData();
				if (rep >=0) {
					tot += diff;
					x += Arrays.stream(data).sum();
				}
			}

		}

		System.out.println("log:" + (tot/div/reps) + "\t" + (x/reps));
			
		double x2 = 0;
		tot=0;

		for (int rep = -10; rep < reps; ++ rep){
			var rs = RandomSource.JDK.create(0xb00b);
			DirichletSampler sampler2 = DirichletSampler.symmetric(rs, 2, 1);
			DirichletSampler sampler_prior = DirichletSampler.symmetric(rs, 6, 1);
			
			
			double[][] arrpa = sampler2.samples(tests*6).toArray(len->new double[len][]);
			double[][] arrpb = sampler_prior.samples(tests).toArray(len->new double[len][]);

			for (int i = 0; i < tests; ++i) {
				
				double[] parr = Doubles.concat(arrpa[i*6], arrpa[i*6+1], arrpa[i*6+2], arrpa[i*6+3], arrpa[i*6+4], arrpa[i*6+5]);
				// BayesianFactor pa = BayesianFactorFactory.factory().domain(d1).data(parr).get();//.log();
				// BayesianFactor pb = BayesianFactorFactory.factory().domain(d2).data(arrpb[i]).get();//.log();
				
				BayesianFactor pa = BayesianFactorFactory.factory().domain(d1).data(parr).get();
				BayesianFactor pb = BayesianFactorFactory.factory().domain(d2).data(arrpb[i]).get();
				
				long nt = System.nanoTime();
				var fact = pa.combine(pb)
				// .marginalize(c)
				//	.marginalize(b)
				;
				long diff = (System.nanoTime() - nt) ;
				double[] data = fact.getData();
				if (rep >=0) {
					tot += diff;
					x2 += Arrays.stream(data).sum();
				}
			}
		}

		System.out.println("get:" + (tot/div/reps) + "\t" + (x/reps));
		assertEquals(x,x2, 0.000000001);

		
	}



//	@Test
	public void testLogSpeed() {
		
		int[] vars = new int[]{0, 1, 2};
		int[] size = new int[]{2, 3, 3};

		int tests = 100000;
		long total = 0;
		int reps = 20;
		var rs = RandomSource.JDK.create(0xb00b);
		for (int rep = -10; rep < reps; ++ rep){
			DirichletSampler sampler = DirichletSampler.symmetric(rs, 18, 1);
			
			long time = System.nanoTime();
			double[][] arrs = sampler.samples(tests).toArray(len->new double[len][]);
			for (int i = 0; i < tests; ++i) {

				double[] vals = arrs[i];//new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.10, 0.10};
				
				BayesianFactor ibf = BayesianFactorFactory.factory().domain(new Strides(vars, size))
				.data(vals)
				.log();
				
				double[] dta = ibf.marginalize(1).getData();
				for (int j = 0; j > 3; ++j) {	
					assertEquals(dta[j],    vals[j*6+0] + vals[j*6+2] + vals[j*6+4], 0.00000001);
					assertEquals(dta[j+1],  vals[j*6+1] + vals[j*6+3] + vals[j*6+5], 0.00000001);
				}
			}
			long delta = System.nanoTime() - time;
			
			if (rep >= 0) {
				total += delta;
				System.out.println(delta);
			}
		}
		System.out.println(total/1000000.0/reps);
	}
}
