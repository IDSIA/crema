package ch.idsia.crema.alessandro;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

public class AdaptiveTestsVertexPropagation {

	int nSkills = 4; // Number of skill variables
	int nLevels = 4; // Number of state for the skill variables

	public static void main(String[] args) {
		
		// Objective function (variables y0, y1, t)
		double[] n = new double[] { 2., 4., 0.};
		LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(n, 0);

		//inequalities
		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[2];
		//t > 0
		double[][] Gmh = new double[][]{{0.0, 0.0,-1.0}};//t>0
		inequalities[0] = new LinearMultivariateRealFunction(Gmh[0], 0);
		
		//perspective function of (x-c0)^2 + (y-c1)^2 - R^2 < 0
		//this is t*((y0/t - c0)^2 + (y1/t - c1)^2 -R^2)
		//we do not multiply by t, because it would make the function no more convex
		final double c0 = 0.65;
		final double c1 = 0.65;
		final double R = 0.25;
		inequalities[1] = new ConvexMultivariateRealFunction() {
			
			@Override
			public double value(double[] X) {
				double y0 = X[0];
				double y1 = X[1];
				double t =  X[2];
				return t * (Math.pow(y0 / t - c0, 2) + Math.pow(y1 / t - c1, 2) - Math.pow(R, 2));
			}
			
			@Override
			public double[] gradient(double[] X) {
				double y0 = X[0];
				double y1 = X[1];
				double t =  X[2];
				double[] ret = new double[3];
				ret[0] = 2 * (y0/t - c0);
				ret[1] = 2 * (y1/t - c1);
				ret[2] = Math.pow(c0, 2) + Math.pow(c1, 2) - Math.pow(R, 2) - (Math.pow(y0, 2) + Math.pow(y1, 2))/Math.pow(t, 2);
				return ret;
			}
			
			@Override
			public double[][] hessian(double[] X) {
				double y0 = X[0];
				double y1 = X[1];
				double t  = X[2];
				double[][] ret = {
					{                2/t,                   0, -2*y0/Math.pow(t,2)}, 
					{                  0,                 2/t, -2*y1/Math.pow(t,2)}, 
					{-2*y0/Math.pow(t,2), -2*y1/Math.pow(t,2),  2*(Math.pow(y0,2) + Math.pow(y1,2))/Math.pow(t,3)}};
				return ret;
			}
			
			@Override
			public int getDim() {
				return 3;
			}
		};
		
		//equalities (e.y+f.t=1), f is 0
		double[][] Amb = new double[][]{{ 2.,  3.,  0.}};
		double[] bm= new double[]{1};
		
		//optimization problem
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(objectiveFunction);
		or.setA(Amb);
		or.setB(bm);
		or.setFi(inequalities);
		or.setTolerance(1.E-6);
		or.setToleranceFeas(1.E-6);
		or.setNotFeasibleInitialPoint(new double[] { 0.6, -0.2/3., 0.1 });
		or.setCheckKKTSolutionAccuracy(true);
		
		//optimization
		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(or);
		try {
			int returnCode = opt.optimize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//This will give the solution:
				double[] sol = opt.getOptimizationResponse().getSolution();
			System.out.println(sol[0]);
			System.out.println(sol[1]);
			System.out.println(sol[2]);
			
		
		
//		// Objective function (variables (x,y), dim = 2)
//		double[] a01 = new double[]{2,1};
//		double b01 = 0;
//		double[] a02 = new double[]{3,1};
//		double b02 = 0;
//		ConvexMultivariateRealFunction objectiveFunction = new LogTransformedPosynomial(new double[][]{a01, a02}, new double[]{b01, b02});
//		//constraints
//		double[] a11 = new double[]{1,0};
//		double b11 = Math.log(1);
//		double[] a21 = new double[]{0,1};
//		double b21 = Math.log(1);
//		double[] a31 = new double[]{-1,-1.};
//		double b31 = Math.log(0.7);
//		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[3];
//		inequalities[0] = new LogTransformedPosynomial(new double[][]{a11}, new double[]{b11});
//		inequalities[1] = new LogTransformedPosynomial(new double[][]{a21}, new double[]{b21});
//		inequalities[2] = new LogTransformedPosynomial(new double[][]{a31}, new double[]{b31});
//		//optimization problem
//		OptimizationRequest or = new OptimizationRequest();
//		or.setF0(objectiveFunction);
//		or.setFi(inequalities);
//		or.setInitialPoint(new double[]{Math.log(0.9), Math.log(0.9)});
//		//or.setInteriorPointMethod(JOptimizer.BARRIER_METHOD);//if you prefer the barrier-method
//		//optimization
//		JOptimizer opt = new JOptimizer();
//		opt.setOptimizationRequest(or);
		
//		// Objective function
//		double[][] P = new double[][] {{ 1., 0.4 }, { 0.4, 1. }};
//		PDQuadraticMultivariateRealFunction objectiveFunction = new PDQuadraticMultivariateRealFunction(P, null, 0);
//
//		//equalities
//		double[][] A = new double[][]{{1,1}};
//		double[] b = new double[]{1};
//
//		//inequalities
//		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[2];
//		inequalities[0] = new LinearMultivariateRealFunction(new double[]{-1, 0}, 0);
//		inequalities[1] = new LinearMultivariateRealFunction(new double[]{0, -1}, 0);
//		
//		//optimization problem
//		OptimizationRequest or = new OptimizationRequest();
//		or.setF0(objectiveFunction);
//		or.setInitialPoint(new double[] { 0.1, 0.9});
//		//or.setFi(inequalities); //if you want x>0 and y>0
//		or.setA(A);
//		or.setB(b);
//		or.setToleranceFeas(1.E-12);
//		or.setTolerance(1.E-12);
//		
//		//optimization
//		JOptimizer opt2 = new JOptimizer();
//		opt2.setOptimizationRequest(or);
//		try {
//			int returnCode = opt2.optimize();
//			double[] sol = opt2.getOptimizationResponse().getSolution();
//			System.out.println(sol[0]);
//			System.out.println(sol[1]);
//	
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		//int returnCode = opt.optimize();
		//skillFactor[i] = new IntervalFactor(domSkill[i],domSkill[i-1].union(domSkill[i-2])); 
	}
		// Right answer for skill / level
		// First 4 numbers = right answer of four levels for skill 1
//		double[][] right = new double[][] {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
//		double[][] wrong = new double[][] {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
//		AdaptiveTestsVertexPropagation myTest = new AdaptiveTestsVertexPropagation();
//		AbellanEntropy aaa = new AbellanEntropy();
//		double[][] results = new double[4][2];
//		for(int s=0;s<4;s++){
//			results = myTest.germanTest(s,right,wrong);
//			System.out.println("Skill "+s);
//			System.out.println(Arrays.toString(results[0]));
//			System.out.println(Arrays.toString(results[1]));				
//			System.out.println("H="+Arrays.toString(aaa.getMaxEntro(results[0],results[1])));}
	}
	

	//@Test
	// s is the skill under consideration
	//public double[][] germanTest(int queriedSkill, double[][] rightQ, double[][] wrongQ) {
		// Network structure
		// S0 -> S1 -> S2 -> S3
		//  |     |     |     |
		//  v     v     v     v
		// Q0     Q1    Q2    Q3

		// Model (the whole network)
		//SparseModel<GenericFactor> model = new SparseModel<>();

//		String current;
//		try {
//			current = new java.io.File( "." ).getCanonicalPath();
//	        System.out.println("Current dir:"+current);
//	        String currentDir = System.getProperty("user.dir");
//	        System.out.println("Current dir using System:" +currentDir);
//
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		// Read probabilities from external file
//		double[][] cnPars = readMyFile("src/test/resources/adaptive/cnParameters.txt");
//
//		// -------------- //
//		// SET THE SKILLS //
//		// -------------- //
//
//		// Prepare the domains of the skills (nLevels states each)
//		// Local models over the skills initialized
//		IntervalFactor[] sFact = new IntervalFactor[nSkills]; // Array of factors
//		Strides domSkill[] = new Strides[nSkills];
//		int[] skill = new int[nSkills];
//		for(int s=0;s<nSkills;s++){
//			skill[s] = model.addVariable(nLevels);
//			domSkill[s] = Strides.as(skill[s],nLevels);	
//			if(s==0){
//				sFact[s] = new IntervalFactor(domSkill[s],Strides.EMPTY);
//				sFact[s].setLower(cnPars[0]);
//				sFact[s].setUpper(cnPars[1]);
//			}
//			else{
//				sFact[s] = new IntervalFactor(domSkill[s],domSkill[s-1]);
//				sFact[s].setLower(cnPars[2],0); //P(Si|Si-1=0)
//				sFact[s].setUpper(cnPars[3],0);
//				sFact[s].setLower(cnPars[4],1); //P(Si|Si-1=0)
//				sFact[s].setUpper(cnPars[5],1);
//				sFact[s].setLower(cnPars[6],2); //P(Si|Si-1=0)
//				sFact[s].setUpper(cnPars[7],2);
//				sFact[s].setLower(cnPars[8],3); //P(Si|Si-1=0)
//				sFact[s].setUpper(cnPars[9],3);
//			}
//			model.setFactor(skill[s],sFact[s]);
//		}

		// ----------------------------- //
		// PARSE THE QUESTION PARAMETERS //
		// ----------------------------- //

//		double[][][] qType = new double[nLevels][nLevels][2];
//		qType[0][0] = Arrays.copyOfRange(cnPars[10],0,2);
//		qType[0][1] = Arrays.copyOfRange(cnPars[11],0,2);
//		qType[0][2] = Arrays.copyOfRange(cnPars[12],0,2);
//		qType[0][3] = Arrays.copyOfRange(cnPars[13],0,2);
//		qType[1][0] = Arrays.copyOfRange(cnPars[14],0,2);
//		qType[1][1] = Arrays.copyOfRange(cnPars[15],0,2);
//		qType[1][2] = Arrays.copyOfRange(cnPars[16],0,2);
//		qType[1][3] = Arrays.copyOfRange(cnPars[17],0,2);
//		qType[2][0] = Arrays.copyOfRange(cnPars[18],0,2);
//		qType[2][1] = Arrays.copyOfRange(cnPars[19],0,2);
//		qType[2][2] = Arrays.copyOfRange(cnPars[20],0,2);
//		qType[2][3] = Arrays.copyOfRange(cnPars[21],0,2);
//		qType[3][0] = Arrays.copyOfRange(cnPars[22],0,2);
//		qType[3][1] = Arrays.copyOfRange(cnPars[23],0,2);
//		qType[3][2] = Arrays.copyOfRange(cnPars[24],0,2);
//		qType[3][3] = Arrays.copyOfRange(cnPars[25],0,2);

		// ----------------- //
		// SET THE QUESTIONS //
		// ----------------- //

		// A question for each skill (embedding all the answers)
//		int nQuestions = nSkills;
//
//		double[] lP = new double[2];
//		double[] uP = new double[2];

		// Local models over the questions initialized
//		IntervalFactor[] qFact = new IntervalFactor[nQuestions]; // Array of factors	
		// Prepare the domains of the questions (two states each)
//		Strides domQuestion[] = new Strides[nQuestions];		
//		int[] question = new int[nQuestions];
//		for(int s=0;s<nQuestions;s++){
//			question[s] = model.addVariable(2);
//			domQuestion[s] = Strides.as(question[s],2);
//			qFact[s] = new IntervalFactor(domQuestion[s],domSkill[s]);
//			for(int l=0;l<nLevels;l++){
//				lP[0] = 1.0;
//				uP[0] = 1.0;
//				if((Arrays.stream(rightQ[s]).sum()==0)&&(Arrays.stream(wrongQ[s]).sum()==0)){
//					lP[0] = 0.5;
//					uP[0] = 0.5;}
//				else{
//					for(int l2=0;l2<4;l2++){
//						lP[0] *= Math.pow(qType[l2][l][0],rightQ[s][l2]);
//						uP[0] *= Math.pow(qType[l2][l][1],rightQ[s][l2]);
//						lP[0] *= Math.pow(1.0-qType[l2][l][1],wrongQ[s][l2]);
//						uP[0] *= Math.pow(1.0-qType[l2][l][0],wrongQ[s][l2]);}				
//					lP[1] = 1.0-uP[0];
//					uP[1] = 1.0-lP[0];
//					qFact[s].setLower(lP.clone(),l);
//					qFact[s].setUpper(uP.clone(),l);}}	
//			model.setFactor(question[s],qFact[s]);}

		// Dummy variable implementing the observation of the questions
		// this is a common child of the three questions
//		int dummy = model.addVariable(2);
//		BayesianFactor fDummy = new BayesianFactor(model.getDomain(question[0],question[1],question[2],question[3],dummy), false);
//		fDummy.setValue(1.0, 0,0,0,0, 1);	
//		fDummy.setValue(1.0, 0,0,0,1, 0);	
//		fDummy.setValue(1.0, 0,0,1,0, 0);
//		fDummy.setValue(1.0, 0,0,1,1, 0);	
//		fDummy.setValue(1.0, 0,1,0,0, 0);	
//		fDummy.setValue(1.0, 0,1,0,1, 0);	
//		fDummy.setValue(1.0, 0,1,1,0, 0);	
//		fDummy.setValue(1.0, 0,1,1,1, 0);	
//		fDummy.setValue(1.0, 1,0,0,0, 0);	
//		fDummy.setValue(1.0, 1,0,0,1, 0);	
//		fDummy.setValue(1.0, 1,0,1,0, 0);
//		fDummy.setValue(1.0, 1,0,1,1, 0);	
//		fDummy.setValue(1.0, 1,1,0,0, 0);	
//		fDummy.setValue(1.0, 1,1,0,1, 0);	
//		fDummy.setValue(1.0, 1,1,1,0, 0);	
//		fDummy.setValue(1.0, 1,1,1,1, 0);	
//		model.setFactor(dummy,fDummy);

		// Compute the inferences
//		Inference approx = new Inference();
//		approx.initialize(new HashMap<String, Object>(){{
//			put(ISearch.MAX_TIME, "10");
//			put(GreedyWithRandomRestart.MAX_RESTARTS, "5");
//			put(GreedyWithRandomRestart.MAX_PLATEAU, "2");
//		}});
//		IntervalFactor resultsALP = null;
//		try {
//			resultsALP = approx.query(model,skill[queriedSkill],dummy);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		// Return the results of ApproxLP
//		double[][] output = new double[2][nLevels];
//		output[0] = resultsALP.getLower();
//		output[1] = resultsALP.getUpper();
//		return output;
//	}}

//}
