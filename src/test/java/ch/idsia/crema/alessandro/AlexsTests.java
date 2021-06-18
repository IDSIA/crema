package ch.idsia.crema.alessandro;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.algebra.SeparateConvexDefaultAlgebra;
import ch.idsia.crema.factor.algebra.SeparateDefaultAlgebra;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.inference.approxlp.ApproxLP1;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.search.ISearch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Disabled
public class AlexsTests {

	public static void main(String[] args) {
		//new AlexsTests().twoNodes();
		//new AlexsTests().threeNodes();
		//new AlexsTests().firstTest();
		//new AlexsTests().chainBack();
		new AlexsTests().chainBackTest();

		System.out.println("DONE");
	}

	double[][] vertici;
	double[][] vertici_conv;
	double[][] vertici2;
	double[][] vertici_conv2;
/*
	JFrame frame = new JFrame();
	JPanel canvas = new JPanel() {

		@Override
		protected void paintComponent(java.awt.Graphics g) {
			super.paintComponent(g);
			frame.setSize(650, 650);
			int fontSize = 12;
			g.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
			int wi = 600;// (int)
			// r.getWidth();
			int he = 600;// (int) r.getHeight();
			int thick = 8;
			g.clearRect(0, 0, wi, he);
			g.setColor(Color.YELLOW);
			int xpoints[] = { 0, wi, 0, 0 };
			int ypoints[] = { 0, 0, he, 0 };
			g.fillPolygon(xpoints, ypoints, 4);
			g.setColor(Color.BLUE);
			for (double[] vert : vertici) {
				int x = (int) (wi * vert[0]);
				int y = (int) (he * vert[1]);
				g.fillOval(x, y, thick, thick);
			}
			g.setColor(Color.RED);
			for (double[] vert : vertici_conv) {
				int x = (int) (wi * vert[0]);
				int y = (int) (he * vert[1]);
				g.fillOval(x, y, thick, thick);
			}
		};
	};
*/

	@Test
	public void twoNodesTernary() {
		DAGModel<GenericFactor> model = new DAGModel<>();
		int varA = model.addVariable(3); // Variables
		int varB = model.addVariable(3); // Variables
		Strides dA = Strides.as(varA, 3);
		Strides dB = Strides.as(varB, 3);
		IntervalFactor ffA, ffB;
		ffA = IntervalFactorFactory.factory()
				.domain(dA)
				.lower(new double[]{0.2, 0.2, 0.2})
				.upper(new double[]{0.4, 0.4, 0.4})
				.get();
		ffB = IntervalFactorFactory.factory()
				.domain(dB, dA)
				.lower(new double[]{0.2, 0.2, 0.2}, 0)
				.upper(new double[]{0.4, 0.4, 0.4}, 0)
				.lower(new double[]{0.2, 0.2, 0.2}, 1)
				.upper(new double[]{0.4, 0.4, 0.4}, 1)
				.lower(new double[]{0.2, 0.2, 0.2}, 2)
				.upper(new double[]{0.4, 0.4, 0.4}, 2)
				.get();

		model.setFactor(varA, ffA);
		model.setFactor(varB, ffB);
		VertexFactor fA, fB;
		fA = VertexFactorFactory.factory()
				.domain(dA)
				.addVertex(new double[]{.2, .4, .4})
				.addVertex(new double[]{.4, .2, .4})
				.addVertex(new double[]{.4, .4, .2})
				.get();
		fB = VertexFactorFactory.factory()
				.domain(dB, dA)
				.addVertex(new double[]{.4, .4, .2}, 0)
				.addVertex(new double[]{.4, .2, .4}, 0)
				.addVertex(new double[]{.2, .4, .4}, 0)
				.addVertex(new double[]{.4, .4, .2}, 1)
				.addVertex(new double[]{.4, .2, .4}, 1)
				.addVertex(new double[]{.2, .4, .4}, 1)
				.addVertex(new double[]{.4, .4, .2}, 2)
				.addVertex(new double[]{.4, .2, .4}, 2)
				.addVertex(new double[]{.2, .4, .4}, 2)
				.get();

		int dummy = model.addVariable(2);
		BayesianFactor fDummy = BayesianFactorFactory.factory()
				.domain(model.getDomain(1, dummy))
				.value(1.0, 1, 0)
				.value(1.0, 0, 1)
				.get();

		model.setFactor(dummy, fDummy);


		// Algebra to perform the computations
		SeparateDefaultAlgebra alge = new SeparateDefaultAlgebra();
		SeparateConvexDefaultAlgebra convex_alge = new SeparateConvexDefaultAlgebra();
		// Forward
		//		VertexFactor tmp = alge.combine(fA, fB);
		//		fB = alge.marginalize(tmp,0);
		//		fB = convex_alge.convex(fB);
		//		System.out.println("--- VE ---");
		//		for (double[] vertex : vertici_conv2)
		//			System.out.println(Arrays.toString(vertex));
		// Backward
		int s = 0;
		fB = fB.reseparate(Strides.EMPTY);
		fB = fB.filter(varB, s);//
		VertexFactor bottom = convex_alge.fullConvex(fB);
		VertexFactor parent = fA.reseparate(Strides.EMPTY);
		VertexFactor tmp = alge.combine(bottom, parent);
		tmp = convex_alge.fullConvex(tmp);
		fA = tmp.normalize();
		fA = convex_alge.convex(fA);
		System.out.println("--- VE ---");
		for (double[] vertex : fA.getVertices())
			System.out.println(Arrays.toString(vertex));
		System.out.println("========");


		ApproxLP1<GenericFactor> approx = new ApproxLP1<>();
		approx.setEvidenceNode(dummy);
		IntervalFactor resultsALP = approx.query(model, 0);
		// Results of ApproxLP
		System.out.println(Arrays.toString(resultsALP.getLower()));
		System.out.println(Arrays.toString(resultsALP.getUpper()));
		//
	}

	public void threeNodes() {
		DAGModel<GenericFactor> model = new DAGModel<>();
		int varA = model.addVariable(2); // Variables
		int varB = model.addVariable(2); // Variables
		int varC = model.addVariable(2); // Variables
		Strides dA = Strides.as(varA, 2);
		Strides dB = Strides.as(varB, 2);
		Strides dC = Strides.as(varC, 2);
		IntervalFactor ffA, ffB, ffC;
		ffA = IntervalFactorFactory.factory()
				.domain(dA)
				.lower(new double[]{0.4, 0.4})
				.upper(new double[]{0.6, 0.6})
				.get();

		ffB = IntervalFactorFactory.factory()
				.domain(dB, dA)
				.lower(new double[]{0.2, 0.7}, 0)
				.upper(new double[]{0.3, 0.8}, 0)
				.lower(new double[]{0.8, 0.1}, 1)
				.upper(new double[]{0.9, 0.2}, 1)
				.get();
		ffC = IntervalFactorFactory.factory()
				.domain(dC, dB)
				.lower(new double[]{0.2, 0.7}, 0)
				.upper(new double[]{0.3, 0.8}, 0)
				.lower(new double[]{0.8, 0.1}, 1)
				.upper(new double[]{0.9, 0.2}, 1)
				.get();
		model.setFactor(varA, ffA);
		model.setFactor(varB, ffB);
		model.setFactor(varC, ffC);
		VertexFactor fA, fB, fC;
		fA = VertexFactorFactory.factory()
				.domain(dA)
				.addVertex(new double[]{.4, .6})
				.addVertex(new double[]{.6, .4})
				.get();
		fB = VertexFactorFactory.factory()
				.domain(dB, dA)
				.addVertex(new double[]{0.2, 0.8}, 0)
				.addVertex(new double[]{0.3, 0.7}, 0)
				.addVertex(new double[]{0.8, 0.2}, 1)
				.addVertex(new double[]{0.9, 0.1}, 1)
				.get();
		fC = VertexFactorFactory.factory()
				.domain(dC, dB)
				.addVertex(new double[]{0.2, 0.8}, 0)
				.addVertex(new double[]{0.3, 0.7}, 0)
				.addVertex(new double[]{0.8, 0.2}, 1)
				.addVertex(new double[]{0.9, 0.1}, 1)
				.get();
		int dummy = model.addVariable(2);
		BayesianFactor fDummy = BayesianFactorFactory.factory()
				.domain(model.getDomain(2, dummy))
				.value(1.0, 1, 1)
				.value(1.0, 0, 0)
				.get();
		model.setFactor(dummy, fDummy);


		// Algebra to perform the computations
		SeparateDefaultAlgebra alge = new SeparateDefaultAlgebra();
		SeparateConvexDefaultAlgebra convex_alge = new SeparateConvexDefaultAlgebra();
		// Forward
		//		VertexFactor tmp = alge.combine(fA, fB);
		//		fB = alge.marginalize(tmp,0);
		//		fB = convex_alge.convex(fB);
		//		System.out.println("--- VE ---");
		//		for (double[] vertex : vertici_conv2)
		//			System.out.println(Arrays.toString(vertex));
		//Backward
		int s = 1;
		fC = fC.reseparate(Strides.EMPTY);
		fC = fC.filter(varC, s);//
		fC = convex_alge.fullConvex(fC);
		fB = fB.reseparate(Strides.EMPTY);
		fB = alge.combine(fB, fC);
		fB = alge.marginalize(fB, 1);
		fB = fB.reseparate(Strides.EMPTY);
		fA = alge.combine(fA, fB);
		//VertexFactor bottom = convex_alge.fullConvex(fC);
		//VertexFactor parent = fB.reseparate(Strides.EMPTY);
		//VertexFactor tmp = alge.combine(bottom, parent);
		//tmp = convex_alge.fullConvex(tmp);
		fA = fA.normalize();
		fA = convex_alge.convex(fA);
		System.out.println("--- VE ---");
		for (double[] vertex : fA.getVertices())
			System.out.println(Arrays.toString(vertex));
		System.out.println("========");

		ApproxLP1<GenericFactor> approx = new ApproxLP1<>();
		approx.setEvidenceNode(3);
		IntervalFactor resultsALP = approx.query(model, 0);
		// Results of ApproxLP
		System.out.println(Arrays.toString(resultsALP.getLower()));
		System.out.println(Arrays.toString(resultsALP.getUpper()));
		//
	}

	public void twoNodes() {
		DAGModel<GenericFactor> model = new DAGModel<>();
		int varA = model.addVariable(2); // Variables
		int varB = model.addVariable(2); // Variables
		Strides dA = Strides.as(varA, 2);
		Strides dB = Strides.as(varB, 2);
		IntervalFactor ffA, ffB;
		ffA = IntervalFactorFactory.factory()
				.domain(dA)
				.lower(new double[]{0.4, 0.4})
				.upper(new double[]{0.6, 0.6})
				.get();
		ffB = IntervalFactorFactory.factory()
				.domain(dB, dA)
				.lower(new double[]{0.2, 0.7}, 0)
				.upper(new double[]{0.3, 0.8}, 0)
				.lower(new double[]{0.8, 0.1}, 1)
				.upper(new double[]{0.9, 0.2}, 1)
				.get();
		model.setFactor(varA, ffA);
		model.setFactor(varB, ffB);
		VertexFactor fA, fB;
		fA = VertexFactorFactory.factory()
				.domain(dA)
				.addVertex(new double[]{.4, .6})
				.addVertex(new double[]{.6, .4})
				.get();
		fB = VertexFactorFactory.factory()
				.domain(dB, dA)
				.addVertex(new double[]{0.2, 0.8}, 0)
				.addVertex(new double[]{0.3, 0.7}, 0)
				.addVertex(new double[]{0.8, 0.2}, 1)
				.addVertex(new double[]{0.9, 0.1}, 1)
				.get();
		int dummy = model.addVariable(2);
		BayesianFactor fDummy = BayesianFactorFactory.factory()
				.domain(model.getDomain(1, dummy))
				.value(1.0, 1, 0)
				.value(1.0, 0, 1)
				.get();
		model.setFactor(dummy, fDummy);


		// Algebra to perform the computations
		SeparateDefaultAlgebra alge = new SeparateDefaultAlgebra();
		SeparateConvexDefaultAlgebra convex_alge = new SeparateConvexDefaultAlgebra();
		// Forward
		//		VertexFactor tmp = alge.combine(fA, fB);
		//		fB = alge.marginalize(tmp,0);
		//		fB = convex_alge.convex(fB);
		//		System.out.println("--- VE ---");
		//		for (double[] vertex : vertici_conv2)
		//			System.out.println(Arrays.toString(vertex));
		//Backward
		int s = 0;
		fB = fB.reseparate(Strides.EMPTY);
		fB = fB.filter(varB, s);//
		VertexFactor bottom = convex_alge.fullConvex(fB);
		VertexFactor parent = fA.reseparate(Strides.EMPTY);
		VertexFactor tmp = alge.combine(bottom, parent);
		tmp = convex_alge.fullConvex(tmp);
		fA = tmp.normalize();
		fA = convex_alge.convex(fA);
		System.out.println("--- VE ---");
		for (double[] vertex : fA.getVertices())
			System.out.println(Arrays.toString(vertex));
		System.out.println("========");


		ApproxLP1<GenericFactor> approx = new ApproxLP1<>();
		approx.setEvidenceNode(2);
		IntervalFactor resultsALP = approx.query(model, 0);
		// Results of ApproxLP
		System.out.println(Arrays.toString(resultsALP.getLower()));
		System.out.println(Arrays.toString(resultsALP.getUpper()));
		//
	}

	public void firstTest() {
		int nVars = 200; // # of variables
		int nDim = 3; // dimensionality
		double imprecision = 0.1; // imprecision level
		double tol = 1000.0; // tolerance on the simplex
		System.out.println("=====================");
		System.out.println("Number of vars=" + nVars);

		double[][] myVertices;
		double[] lowerP = new double[nDim];
		double[] upperP = new double[nDim];
		long startTime = System.nanoTime(); // Elapsed time

		CNGenerator a = new CNGenerator(); // Credal set generator

		// Model initialization
		DAGModel<IntervalFactor> model = new DAGModel<>();
		Strides[] d = new Strides[nVars]; // Array of domains

		for (int i = 0; i < nVars; i++) {
			int var = model.addVariable(nDim); // Variables
			d[i] = Strides.as(var, nDim);
		} // Domains

		// Local factors (V-rep)
		VertexFactor[] f = new VertexFactor[nVars];
		IntervalFactor[] ff =  new IntervalFactor[nVars]; // Array of factors

		VertexFactorFactory vff;
		IntervalFactorFactory iff;

		// FIRST LOCAL MODEL
		vff = VertexFactorFactory.factory().domain(d[0]);
		myVertices = a.linvac(nDim, imprecision);
		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : myVertices) {
			vff.addVertex(vertex);
			for (int i = 0; i < nDim; i++) {
				if (vertex[i] < lowerP[i]) {
					lowerP[i] = vertex[i];
				}
				if (vertex[i] > upperP[i]) {
					upperP[i] = vertex[i];
				}
			}
		}
		f[0] = vff.get();
		ff[0] = IntervalFactorFactory.factory().domain(d[0])
				.lower(lowerP)
				.upper(upperP)
				.get();
		model.setFactor(0, ff[0]);
		// for(double[] vv: myVertices)
		// System.out.println(Arrays.toString(vv));
		// System.out.println(Arrays.toString(lowerP));
		// System.out.println(Arrays.toString(upperP));

		// OTHER LOCAL MODELS
		for (int i = 1; i < nVars; i++) {
			vff = VertexFactorFactory.factory().domain(d[i], d[i - 1]);
			iff = IntervalFactorFactory.factory().domain(d[i], d[i - 1]);
			for (int j = 0; j < nDim; j++) { // Loop over the parents
				Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
				Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
				myVertices = a.linvac(nDim, imprecision);
				for (double[] vertex : myVertices) {
					vff.addVertex(vertex, j);
					for (int l = 0; l < nDim; l++) {
						if (vertex[l] < lowerP[l])
							lowerP[l] = vertex[l];
						if (vertex[l] > upperP[l])
							upperP[l] = vertex[l];
					}
				}
				iff.lower(lowerP, j);
				iff.upper(upperP, j);
				// System.out.println("------"+j+"-----");
				// for(double[] vv: myVertices)
				// System.out.println(Arrays.toString(vv));
				// System.out.println(Arrays.toString(lowerP));
				// System.out.println(Arrays.toString(upperP));
			}
			f[i] = vff.get();
			ff[i] = iff.get();
			model.setFactor(i, ff[i]);
		}

		// Elapsed time
		long difference = System.nanoTime() - startTime;
		System.out.println("Random chain created in " + String.format("%d min, %d sec",
				TimeUnit.NANOSECONDS.toHours(difference), TimeUnit.NANOSECONDS.toSeconds(difference)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
		startTime = System.nanoTime(); // Elapsed time

		// Algebra to perform the computations
		SeparateDefaultAlgebra alge = new SeparateDefaultAlgebra();
		SeparateConvexDefaultAlgebra convex_alge = new SeparateConvexDefaultAlgebra();

		// Frame to evaluate convex hull
		//frame.getContentPane().add(canvas);
		//frame.setVisible(true);

		// Top-down Elimination (VE)
		for (int i = 1; i < nVars; i++) {
			System.out.println("-------------------");
			System.out.println("Processing variable " + i);
			System.out.println("Combining ...");
			VertexFactor tmp = alge.combine(f[i], f[i - 1]);
			System.out.println("Marginalizing ...");
			f[i] = alge.marginalize(tmp, i - 1);
			vertici2 = f[i].getVertices();
			// Rounding to prevent numerical issues with the convex hull
			for (double[] vertex : vertici2) {
				vertex[0] = Math.round(vertex[0] * tol) / tol;
				vertex[1] = Math.round(vertex[1] * tol) / tol;
				vertex[2] = 1.0 - vertex[0] - vertex[1];
			}
			System.out.println("Hulling ...");
			f[i] = convex_alge.convex(f[i]);
			vertici_conv2 = f[i].getVertices();

			Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
			Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
			for (double[] vertex : vertici_conv2) {
				for (int l = 0; l < nDim; l++) {
					if (vertex[l] < lowerP[l])
						lowerP[l] = vertex[l];
					if (vertex[l] > upperP[l])
						upperP[l] = vertex[l];
				}
			}
			if (i == nVars - 1) {
				vertici = vertici2;
				vertici_conv = vertici_conv2;
				//frame.invalidate();

				// Results of VE
				System.out.println("--- VE ---");
				System.out.println(Arrays.toString(lowerP));
				System.out.println(Arrays.toString(upperP));
			}
		}

		// Inference with ApproxLP
		System.out.println("--- ApproxLP ---");
		ApproxLP1<IntervalFactor> aprrox = new ApproxLP1<>();
		HashMap<String, Object> init = new HashMap<>();
		init.put(ISearch.MAX_TIME, "10000");
		aprrox.initialize(init);
		IntervalFactor resultsALP = aprrox.query(model, nVars - 1);

		// Results of ApproxLP
		System.out.println(Arrays.toString(resultsALP.getLower()));
		System.out.println(Arrays.toString(resultsALP.getUpper()));

		difference = System.nanoTime() - startTime;
		System.out.println("Elasped time " + String.format("%d min, %d sec", TimeUnit.NANOSECONDS.toHours(difference),
				TimeUnit.NANOSECONDS.toSeconds(difference)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));

	}

	public void chainBack() {

		int s = 0; // observed state
		int nVars = 300; // # of variables
		int nDim = 3; // dimensionality
		double imprecision = 0.1; // imprecision level
		System.out.println("=====================");
		System.out.println("Number of vars=" + nVars);
		double tol = 1000.0; // tolerance on the simplex

		double[][] myVertices;
		double[] lowerP = new double[nDim];
		double[] upperP = new double[nDim];
		long startTime = System.nanoTime(); // Elapsed time

		CNGenerator a = new CNGenerator(); // Credal set generator

		// Model initialization
		DAGModel<GenericFactor> model = new DAGModel<>();
		Strides[] d = new Strides[nVars]; // Array of domains
		//
		for (int i = 0; i < nVars; i++) {
			int var = model.addVariable(nDim); // Variables
			d[i] = Strides.as(var, nDim);
		} // Domains

		// Local factors (V-rep)
		VertexFactor[] f = new VertexFactor[nVars];
		IntervalFactor[] ff =  new IntervalFactor[nVars]; // Array of factors

		VertexFactorFactory vff;
		IntervalFactorFactory iff;

		// FIRST LOCAL MODEL
		vff = VertexFactorFactory.factory().domain(d[0]);
		myVertices = a.linvac(nDim, imprecision);
		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : myVertices) {
			vff.addVertex(vertex);
			for (int i = 0; i < nDim; i++) {
				if (vertex[i] < lowerP[i])
					lowerP[i] = vertex[i];
				if (vertex[i] > upperP[i])
					upperP[i] = vertex[i];
			}
		}
		f[0] = vff.get();
		ff[0] = IntervalFactorFactory.factory()
				.domain(d[0])
				.lower(lowerP)
				.upper(upperP)
				.get();
		model.setFactor(0, ff[0]);
		// for(double[] vv: myVertices)
		// System.out.println(Arrays.toString(vv));
		// System.out.println(Arrays.toString(lowerP));
		// System.out.println(Arrays.toString(upperP));
		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : f[0].getVertices()) {
			for (int l = 0; l < nDim; l++) {
				if (vertex[l] < lowerP[l])
					lowerP[l] = vertex[l];
				if (vertex[l] > upperP[l])
					upperP[l] = vertex[l];
			}
		}
		System.out.println(Arrays.toString(lowerP.clone()));
		System.out.println(Arrays.toString(upperP.clone()));

		// OTHER LOCAL MODELS
		for (int i = 1; i < nVars; i++) {
			vff = VertexFactorFactory.factory().domain(d[i], d[i - 1]);
			iff = IntervalFactorFactory.factory().domain(d[i], d[i - 1]);
			for (int j = 0; j < nDim; j++) { // Loop over the parents
				Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
				Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
				myVertices = a.linvac(nDim, imprecision);
				for (double[] vertex : myVertices) {
					vff.addVertex(vertex, j);
					for (int l = 0; l < nDim; l++) {
						if (vertex[l] < lowerP[l])
							lowerP[l] = vertex[l];
						if (vertex[l] > upperP[l])
							upperP[l] = vertex[l];
					}
				}
				iff.lower(lowerP.clone(), j);
				iff.upper(upperP.clone(), j);
				//System.out.println("------"+j+"-----");
				//for(double[] vv: myVertices)
				//	System.out.println(Arrays.toString(vv));
				//System.out.println(Arrays.toString(lowerP));
				//System.out.println(Arrays.toString(upperP));
			}
			f[i] = vff.get();
			ff[i] = iff.get();
			model.setFactor(i, ff[i]);
		}

		int dummy = model.addVariable(2);

		BayesianFactor fDummy = BayesianFactorFactory.factory()
				.domain(model.getDomain(nVars - 1, dummy))
				//.value(1.0,0,1);
				//.value(1.0,1,0);
				//.value(1.0,2,0);

				.value(1.0, 0 /* nVars -1 = 0 */,
						s == 0 ? 1 : 0 /* dummy = true */)
				// p(dummy=true|nvars-1=0)
				// note however that dummy is bigger than nvars-1 so it will appear after
				.value(1.0, 1 /* nVars -1 = 1 */,
						s == 1 ? 1 : 0 /* dummy = false */)
				.value(1.0, 2 /* nVars -1 = 2 */,
						s == 2 ? 1 : 0 /* dummy = false */)
				.get();
		model.setFactor(dummy, fDummy);

		// Elapsed time
		long difference = System.nanoTime() - startTime;
		System.out.println("Random chain created in " + String.format("%d min, %d sec",
				TimeUnit.NANOSECONDS.toHours(difference), TimeUnit.NANOSECONDS.toSeconds(difference)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
		startTime = System.nanoTime(); // Elapsed time

		// Algebra to perform the computations
		SeparateDefaultAlgebra alge = new SeparateDefaultAlgebra();
		SeparateConvexDefaultAlgebra convex_alge = new SeparateConvexDefaultAlgebra();

		// Frame to evaluate convex hull
		// frame.getContentPane().add(canvas);
		// frame.setVisible(true);

		// Set observation of the last variable in its
		f[nVars - 1] = f[nVars - 1].reseparate(Strides.EMPTY);
		f[nVars - 1] = f[nVars - 1].filter(nVars - 1, s);//

		// Bottom-up Elimination (VE)
		for (int i = nVars - 1; i > 0; i--) {
			System.out.println("-------------------");
			System.out.println("Processing variable " + i);
			System.out.println("Combining ...");

			vertici2 = f[i].getVertices();
			VertexFactor bottom = convex_alge.fullConvex(f[i]);
			vertici_conv2 = bottom.getVertices();
			System.out.println("V before" + vertici2.length + "after" + vertici_conv2.length);

			VertexFactor parent = f[i - 1].reseparate(Strides.EMPTY);
			// VertexFactor parent = f[i-1];
			VertexFactor tmp = alge.combine(bottom, parent);
			System.out.println("Marginalizing ...");

			//tmp = convex_alge.fullConvex(tmp);

			if (i - 1 == 0) {
				// processing query
				tmp = convex_alge.fullConvex(tmp);
				f[0] = tmp.normalize();
			} else
				f[i - 1] = alge.marginalize(tmp, i - 1);
			// vertici2 = f[i-1].getVertices();
			//// Rounding to prevent numerical issues with the convex hull
			//for (double[] vertex : vertici2) {
			// vertex[0] = Math.round(vertex[0] * tol) / tol;
			// vertex[1] = Math.round(vertex[1] * tol) / tol;
			// vertex[2] = 1.0 - vertex[0] - vertex[1];
			//}
		}

		// Rounding to prevent numerical issues with the convex hull
		// System.out.println("Hulling ...");
		// f[i] = convex_alge.convex(f[i]);
		vertici_conv2 = f[0].getVertices();

		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : vertici_conv2) {
			for (int l = 0; l < nDim; l++) {
				if (vertex[l] < lowerP[l])
					lowerP[l] = vertex[l];
				if (vertex[l] > upperP[l])
					upperP[l] = vertex[l];
			}
		}
		// if (i == nVars - 1) {
		//		vertici = vertici2;
		//		vertici_conv = vertici_conv2;
		//		frame.invalidate();

		// Results of VE
		System.out.println("--- VE (Back) ---");
		System.out.println(Arrays.toString(lowerP));
		System.out.println(Arrays.toString(upperP));
		// }

		// Inference with ApproxLP
		System.out.println("--- ApproxLP (Back) ---");

		ApproxLP1<GenericFactor> approx = new ApproxLP1<>();
		HashMap<String, Object> init = new HashMap<>();
		init.put(ISearch.MAX_TIME, "10000");
		approx.initialize(init);
		approx.setEvidenceNode(dummy);
		IntervalFactor resultsALP = approx.query(model, 0);

		// Results of ApproxLP
		System.out.println(Arrays.toString(resultsALP.getLower()));
		System.out.println(Arrays.toString(resultsALP.getUpper()));

		difference = System.nanoTime() - startTime;
		System.out.println("Elasped time " + String.format("%d min, %d sec", TimeUnit.NANOSECONDS.toHours(difference),
				TimeUnit.NANOSECONDS.toSeconds(difference)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
	}

	public void chainBackTest() {

		int s = 2; // observed state
		int nVars = 10; // # of variables
		int nDim = 3; // dimensionality
		double imprecision = 0.4; // imprecision level
		double tol = 1E-10;
		System.out.println("=====================");
		System.out.println("Number of vars=" + nVars);
		double[][] myVertices;
		double[] lowerP = new double[nDim];
		double[] upperP = new double[nDim];
		long startTime = System.nanoTime(); // Elapsed time
		CNGenerator myVarA = new CNGenerator(); // Credal set generator
		// Model initialization
		DAGModel<GenericFactor> model = new DAGModel<>();
		Strides[] d = new Strides[nVars]; // Array of domains
		for (int i = 0; i < nVars; i++) {
			int var = model.addVariable(nDim); // Variables
			d[i] = Strides.as(var, nDim);
		} // Domains

		// Local factors (V-rep)
		VertexFactor[] f = new VertexFactor[nVars];
		IntervalFactor[] ff = new IntervalFactor[nVars]; // Array of factors

		VertexFactorFactory vff;
		IntervalFactorFactory iff;

		// FIRST LOCAL MODEL
		vff = VertexFactorFactory.factory().domain(d[0]);
		iff = IntervalFactorFactory.factory().domain(d[0]);
		myVertices = myVarA.linvac(nDim, imprecision);
		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : myVertices) {
			vff.addVertex(vertex);
			for (int i = 0; i < nDim; i++) {
				if (vertex[i] < lowerP[i])
					lowerP[i] = vertex[i];
				if (vertex[i] > upperP[i])
					upperP[i] = vertex[i];
			}
		}
		iff.lower(lowerP.clone());
		iff.upper(upperP.clone());

		f[0] = vff.get();
		ff[0] = iff.get();
		model.setFactor(0, ff[0]);
		//for (double[] vv : myVertices)
		//System.out.println(Arrays.toString(vv));
		System.out.println(Arrays.toString(lowerP));
		System.out.println(Arrays.toString(upperP));
		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : f[0].getVertices()) {
			for (int l = 0; l < nDim; l++) {
				if (vertex[l] < lowerP[l])
					lowerP[l] = vertex[l];
				if (vertex[l] > upperP[l])
					upperP[l] = vertex[l];
			}
		}
		System.out.println(Arrays.toString(lowerP));
		System.out.println(Arrays.toString(upperP));

		// OTHER LOCAL MODELS
		for (int i = 1; i < nVars; i++) {
			myVertices = myVarA.linvac(nDim, imprecision);

			vff = VertexFactorFactory.factory().domain(d[i], d[i - 1]);
			iff = IntervalFactorFactory.factory().domain(d[i], d[i - 1]);
			for (int j = 0; j < nDim; j++) { // Loop over the parents
				Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
				Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
				// myVertices = a.linvac(nDim, imprecision);
				// myVertices = a.Jasper(nDim, .01);
				for (double[] vertex : myVertices) {
					vff.addVertex(vertex, j);
					for (int l = 0; l < nDim; l++) {
						if (vertex[l] < lowerP[l])
							lowerP[l] = vertex[l];
						if (vertex[l] > upperP[l])
							upperP[l] = vertex[l];
					}
				}
				iff.lower(lowerP.clone(), j);
				iff.upper(upperP.clone(), j);
				System.out.println("------" + j + "-----");
				for (double[] vv : myVertices)
					System.out.println(Arrays.toString(vv));
				System.out.println(Arrays.toString(lowerP));
				System.out.println(Arrays.toString(upperP));
			}
			f[i] = vff.get();
			ff[i] = iff.get();
			model.setFactor(i, ff[i]);
		}
		int dummy = model.addVariable(2);
		BayesianFactor fDummy = BayesianFactorFactory.factory().domain(model.getDomain(nVars - 1, dummy))
				.value(1.0, 0 /* nVars -1 = 0 */,
						s == 0 ? 1 : 0 /* dummy = true */)
				// p(dummy=true|nvars-1=0)
				// note however that dummy is bigger than nvars-1 so it will appear after
				.value(1.0, 1 /* nVars -1 = 1 */,
						s == 1 ? 1 : 0 /* dummy = false */)
				.value(1.0, 2 /* nVars -1 = 2 */,
						s == 2 ? 1 : 0 /* dummy = false */)
				.get();
		model.setFactor(dummy, fDummy);
		// Elapsed time
		long difference = System.nanoTime() - startTime;
		System.out.println("Random chain created in " + String.format("%d min, %d sec",
				TimeUnit.NANOSECONDS.toHours(difference), TimeUnit.NANOSECONDS.toSeconds(difference)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
		startTime = System.nanoTime(); // Elapsed time
		// Algebra to perform the computations
		SeparateDefaultAlgebra alge = new SeparateDefaultAlgebra();
		SeparateConvexDefaultAlgebra convex_alge = new SeparateConvexDefaultAlgebra();
		// Frame to evaluate convex hull
		// frame.getContentPane().add(canvas);
		// frame.setVisible(true);
		// Set observation of the last variable in its
		f[nVars - 1] = f[nVars - 1].reseparate(Strides.EMPTY);
		f[nVars - 1] = f[nVars - 1].filter(nVars - 1, s);
		// Bottom-up Elimination (VE)
		// try {
		//      FileWriter writer = new FileWriter("MyFile.txt", true);
		//    writer.write("CIAO");
		for (int i = nVars - 1; i > 0; i--) {
			System.out.print("[V" + i + "] ");
			vertici2 = f[i].getVertices();
			if (i < 3) {
				for (double[] vvv : vertici2) {
					System.out.println(Arrays.toString(vvv));
				}
				//writer.write(Arrays.toString(vvv)+"\n");
				//writer.write("\n");

			}
			//writer.close();//}

			System.out.print(vertici2.length);
			//f[i] = convex_alge.round(f[i], tol);
			VertexFactor bottom = convex_alge.fullConvex(f[i]);
			vertici_conv2 = bottom.getVertices();
			System.out.print("/" + vertici_conv2.length);
			VertexFactor parent = f[i - 1].reseparate(Strides.EMPTY);
			VertexFactor tmp = alge.combine(bottom, parent);
			System.out.println();
			if (i - 1 == 0) {
				// processing query
				tmp = convex_alge.fullConvex(tmp);
				f[0] = tmp.normalize();
				//f[0] = convex_alge.convex(f[0]);
			} else
				f[i - 1] = alge.marginalize(tmp, i - 1);
		}

		// } catch (IOException e) {
		//   e.printStackTrace();
		//}

		// Rounding to prevent numerical issues with the convex hull
		vertici_conv2 = f[0].getVertices();
		Arrays.fill(lowerP, Double.POSITIVE_INFINITY);
		Arrays.fill(upperP, Double.NEGATIVE_INFINITY);
		for (double[] vertex : vertici_conv2) {
			for (int l = 0; l < nDim; l++) {
				if (vertex[l] < lowerP[l])
					lowerP[l] = vertex[l];
				if (vertex[l] > upperP[l])
					upperP[l] = vertex[l];
			}
		}
		//if (i == nVars - 1) {
		//		vertici = vertici2;
		//		vertici_conv = vertici_conv2;
		//frame.invalidate();
		// Results of VE
		System.out.println("--- VE (Back) ---");
		System.out.println(Arrays.toString(lowerP));
		System.out.println(Arrays.toString(upperP));
		// }
		// Inference with ApproxLP
		System.out.println("--- ApproxLP (Back) ---");

		ApproxLP1<GenericFactor> approx = new ApproxLP1<>();
		HashMap<String, Object> init = new HashMap<>();
		init.put(ISearch.MAX_TIME, "10000");
		approx.initialize(init);
		approx.setEvidenceNode(dummy);
		IntervalFactor resultsALP = approx.query(model, 0);
		// Results of ApproxLP
		System.out.println(Arrays.toString(resultsALP.getLower()));
		System.out.println(Arrays.toString(resultsALP.getUpper()));

		difference = System.nanoTime() - startTime;
		System.out.println("Elapsed time " + String.format("%d min, %d sec", TimeUnit.NANOSECONDS.toHours(difference),
				TimeUnit.NANOSECONDS.toSeconds(difference)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
	}
}