package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.IO;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.bayesian.BayesianNoisyOrFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:46
 */
public class BayesUAIWriterTest {

	@Test
	public void readBayesUaiAndWrite() throws IOException {
		String filename = "./models/bayes";

		BayesianNetwork bn = new BayesUAIParser(filename + ".uai").parse();

		IO.write(bn, filename + "_2.uai");
	}

	@Test
	public void readBayesUaiAndSerialize() throws IOException {
		String filename = "./models/bayes.uai";

		BayesianNetwork bn = new BayesUAIParser(filename).parse();

		List<String> lines = new BayesUAIWriter(bn, filename).serialize();
		BayesianNetwork bn2 = new BayesUAIParser(lines).parse();

		Assertions.assertEquals(bn.getVariables().length, bn2.getVariables().length);
		Assertions.assertEquals(bn.getNetwork().vertexSet().size(), bn2.getNetwork().vertexSet().size());
		Assertions.assertEquals(bn.getNetwork().edgeSet().size(), bn2.getNetwork().edgeSet().size());
	}

	@Test
	public void writeAndReadLogics() {
		BayesianNetwork bn = new BayesianNetwork();
		final int x1 = bn.addVariable(2);
		final int x2 = bn.addVariable(2);
		final int x3 = bn.addVariable(2);

		final BayesianFactor[] f = new BayesianFactor[3];
		f[0] = BayesianFactorFactory.factory().domain(bn.getDomain(x1)).data(new double[]{.5, .5}).get();
		f[1] = BayesianFactorFactory.factory().domain(bn.getDomain(x2)).data(new double[]{.5, .5}).get();
		f[2] = BayesianFactorFactory.factory().domain(bn.getDomain(x1, x2, x3))
				.noisyOr(new int[]{x1, x2}, new int[]{0, 1}, new double[]{.6, .3});

		bn.setFactors(f);

		List<String> lines = new BayesUAIWriter(bn, "./models/bayes.uai").serialize();

		String str = String.join("\n", lines.subList(14, lines.size()));

		Assertions.assertEquals("NOISY-OR\n2 0.6 0.3\n2 0 1\n2 0 1\n", str);

		BayesianNetwork bn2 = new BayesUAIParser(lines).parse();

		Assertions.assertEquals(bn.getVariables().length, bn2.getVariables().length);
		Assertions.assertEquals(bn.getFactor(2).getClass(), bn2.getFactor(2).getClass());

		final BayesianNoisyOrFactor fa = (BayesianNoisyOrFactor) bn.getFactor(2);
		final BayesianNoisyOrFactor fb = (BayesianNoisyOrFactor) bn2.getFactor(2);

		Assertions.assertArrayEquals(fa.getStrengths(), fb.getStrengths());
		Assertions.assertArrayEquals(fa.getParents(), fb.getParents());
	}

}