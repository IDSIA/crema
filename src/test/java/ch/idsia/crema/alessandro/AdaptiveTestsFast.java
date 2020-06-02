package ch.idsia.crema.alessandro;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.sepolyve.SePolyVE;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.io.dot.DotSerialize;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.search.ISearch;
import ch.idsia.crema.search.impl.GreedyWithRandomRestart;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdaptiveTestsFast {

	// Global variables
	public static final int nSkills = 4; // Number of skill variables
	public static final int nLevels = 4; // Number of state for the skill variables
	public static final double cutOff = 4E-2; // Cutoff to remove numerical problems
	public static final String myPath = "src/test/resources/adaptive/"; // Path to find input/output files
	public static final int nStudents = 10; // Number of students FIXME

	public static void main(String[] args) {

		// Text file where we store the BN and CN pars
		String credalFileName = "cnParameters.txt";
		String bayesFileName = "cnParametersBayes.txt";

		// Read the credal net and create a file with the Bayesian network
		System.out.println("Converting the credal in a Bayesian net ...");
		new AdaptiveFileTools().writeBNFile(myPath+credalFileName);
		System.out.println("Inference time ...");

		// Demonstrative answers sequence
		//double[][][] right2 = {{{0.0, 7.0, 9.0, 10.0},{0.0, 8.0, 8.0, 7.0},{0.0, 5.0, 5.0, 5.0},{0.0, 4.0, 6.0, 1.0}}};
		//double[][][] wrong2 = {{{0.0, 6.0, 5.0, 5.0},{0.0, 4.0, 7.0, 3.0},{0.0, 1.0, 2.0, 5.0},{0.0, 5.0, 8.0, 9.0}}};
		// Whole set of answers
		double[][][] right2 = {{{0.0, 4.0, 5.0, 5.0},{0.0, 4.0, 1.0, 5.0},{0.0, 4.0, 3.0, 0.0},{0.0, 1.0, 2.0, 1.0}}, {{0.0, 3.0, 6.0, 2.0},{0.0, 4.0, 1.0, 1.0},{0.0, 3.0, 2.0, 0.0},{0.0, 2.0, 4.0, 4.0}}, {{0.0, 5.0, 0.0, 0.0},{0.0, 1.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}}};
		double[][][] wrong2 = {{{0.0, 6.0, 5.0, 5.0},{0.0, 4.0, 7.0, 3.0},{0.0, 1.0, 2.0, 5.0},{0.0, 5.0, 8.0, 9.0}}, {{0.0, 7.0, 4.0, 8.0},{0.0, 4.0, 7.0, 7.0},{0.0, 2.0, 3.0, 5.0},{0.0, 4.0, 6.0, 6.0}}, {{0.0, 5.0, 10.0, 10.0},{0.0, 7.0, 8.0, 8.0},{0.0, 5.0, 5.0, 5.0},{0.0, 6.0, 10.0, 10.0}}};

		// Initialise objects
		AdaptiveTestsFast myTest = new AdaptiveTestsFast();
		//AbellanEntropy Abellan = new AbellanEntropy();

		// Local variables
		double[][] results = new double[4][2];
		long startTime,difference;

		// Start the clock (elapsed time)
		startTime = System.nanoTime();

		// Loop over the students
		for(int student=0;student<nStudents;student++){

			// Loop over the skills
			for(int skill=0;skill<nSkills;skill++){

				// Compute and print the results of the credal
				results = myTest.germanTest(myPath+credalFileName,skill,right2[student],wrong2[student]);
				System.out.print("[ID"+student+"][S"+skill+"][Credal L]\t");
				for (double p:results[0]) System.out.print(String.format(Locale.ROOT, "%2.3f\t", p*100));
				System.out.print("\n");
				System.out.print("[ID"+student+"][S"+skill+"][Credal U]\t");
				for (double p:results[1]) System.out.print(String.format(Locale.ROOT, "%2.3f\t", p*100));
				System.out.print("\n");

				// Compute and print the results of the Bayesian
				results = myTest.germanTest(myPath+bayesFileName,skill,right2[student],wrong2[student]);
				System.out.print("[ID"+student+"][S"+skill+"][Bayes]\t\t");
				for (double p:results[0]) System.out.print(String.format(Locale.ROOT, "%2.3f\t", p*100));		    
				System.out.print("\n");
			}
		}

		// Stops the clock and write the elapsed time
		difference = System.nanoTime() - startTime;
		System.out.println("Elapsed time " + String.format("%d min, %d sec", TimeUnit.NANOSECONDS.toHours(difference),
				TimeUnit.NANOSECONDS.toSeconds(difference)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
	}

	//System.out.println("H="+Arrays.toString(Abellan.getMaxEntro(results[0],results[1])));

	// Multiply a logarithmic CPT by a constant and exponentiate
	public double[][] fromLogsToProbs(double[][] logTab){

		// Initialise the output
		double[][] probs = new double[nLevels][2];

		// Compute the maximum of the logs
		double maximumLog = Double.NEGATIVE_INFINITY;
		for(int i=0;i<nLevels;i++)
			if(logTab[i][1]>maximumLog)
				maximumLog=logTab[i][1];

		// Renormalization
		for(int i=0;i<nLevels;i++){
			for(int j=0;j<2;j++){
				probs[i][j]=Math.exp(logTab[i][j]-maximumLog);
				if(probs[i][j]<cutOff){
					probs[i][j]=cutOff;}}}

		return probs;
	}

	//@Test
	// s is the skill under consideration
	public double[][] germanTest(String fileName, int queriedSkill, double[][] rightQ, double[][] wrongQ) {

		// S0 -> S1 -> S2 -> S3
		//  v     v     v     v
		// Q0     Q1    Q2    Q3

		// Vertex Specification
		SparseModel<GenericFactor> model = new SparseModel<>();

		// Read probabilities from external file
		AdaptiveFileTools myTools = new AdaptiveFileTools();
		double[][] cnPars = myTools.readMyFile(fileName);

		//SparseModel<GenericFactor> modelI = new SparseModel<>();
		
		int[] mySkills = new int[4];
		int[] myQuestions = new int[4];
		Strides[] dS = new Strides[4];
		Strides[] dQ = new Strides[4];
		for(int i=0;i<4;i++){
			mySkills[i] = model.addVariable(4);
			myQuestions[i] = model.addVariable(2);
			dS[i] = Strides.as(mySkills[i],4);
			dQ[i] = Strides.as(myQuestions[i],2);}

		VertexFactor[] fS = new VertexFactor[4];
		VertexFactor[] fQ = new VertexFactor[4];
		fS[0] = new VertexFactor(dS[0],Strides.EMPTY);
		fS[1] = new VertexFactor(dS[1],dS[0]);
		fS[2] = new VertexFactor(dS[2],dS[1]);
		fS[3] = new VertexFactor(dS[3],dS[2]);
		fQ[0] = new VertexFactor(dQ[0],dS[0]);
		fQ[1] = new VertexFactor(dQ[1],dS[1]);
		fQ[2] = new VertexFactor(dQ[2],dS[2]);
		fQ[3] = new VertexFactor(dQ[3],dS[3]);

		CNGenerator aaa = new CNGenerator();

//		fS[0].addVertex(new double[] {.2,.4,.2,.2});
//		fS[0].addVertex(new double[] {.2,.2,.4,.2});
		for(double[] a : aaa.fromInt2VertFullD(new double[][] {cnPars[0],cnPars[1]}))
			fS[0].addVertex(a);

		

		for(int i=1;i<4;i++){
			for(double[] a : aaa.fromInt2VertFullD(new double[][] {cnPars[2],cnPars[3]}))
				fS[i].addVertex(a,0);
			for(double[] a : aaa.fromInt2VertFullD(new double[][] {cnPars[4],cnPars[5]}))
				fS[i].addVertex(a,1);
			for(double[] a : aaa.fromInt2VertFullD(new double[][] {cnPars[6],cnPars[7]}))
				fS[i].addVertex(a,2);
			for(double[] a : aaa.fromInt2VertFullD(new double[][] {cnPars[8],cnPars[9]}))
				fS[i].addVertex(a,3);}

//		for(int i=1;i<4;i++){
//			fS[i].addVertex(new double[] {.2,.4,.2,.2}, 0);
//			fS[i].addVertex(new double[] {.2,.2,.4,.2}, 0);
//			fS[i].addVertex(new double[] {.4,.2,.2,.2}, 1);
//			fS[i].addVertex(new double[] {.2,.4,.2,.2}, 1);
//			fS[i].addVertex(new double[] {.2,.2,.2,.4}, 2);
//			fS[i].addVertex(new double[] {.4,.2,.2,.2}, 2);
//			fS[i].addVertex(new double[] {.2,.4,.2,.2}, 3);
//			fS[i].addVertex(new double[] {.2,.2,.4,.2}, 3);}

		for(int i=0;i<4;i++){
			fQ[i].addVertex(new double[] {.3,.7}, 0);
			fQ[i].addVertex(new double[] {.9,.1}, 0);
			fQ[i].addVertex(new double[] {.4,.6}, 1);
			fQ[i].addVertex(new double[] {.8,.2}, 1);
			fQ[i].addVertex(new double[] {.5,.5}, 2);
			fQ[i].addVertex(new double[] {.9,.1}, 2);
			fQ[i].addVertex(new double[] {.4,.6}, 3);
			fQ[i].addVertex(new double[] {.6,.4}, 3);}

//		for(int i=0;i<4;i++){
//			modelV.setFactor(myQuestions[i], fQ[i]);
//			modelV.setFactor(mySkills[i], fS[i]);}

			for(int i=0;i<4;i++){
				model.setFactor(myQuestions[i], fQ[i]);
				model.setFactor(mySkills[i], fS[i]);}

//		int dummy2 = model.addVariable(2);
//		BayesianFactor fDummy2 = new BayesianFactor(model.getDomain(myQuestions[0],dummy2), false);
//		fDummy2.setValue(1.0, 1, 1);	
//		fDummy2.setValue(1.0, 0, 0);
//		model.setFactor(dummy2, fDummy2);

		// cast factors
		SparseModel<VertexFactor> modelV = model.convert((x,v)->(VertexFactor)x);
		
		//TIntIntMap evidence = new TIntIntHashMap();
		//evidence.put(dummy2, 0);
		
		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(myQuestions[0],0);
		evidence.put(myQuestions[1],0);
		evidence.put(myQuestions[2],0);
		evidence.put(myQuestions[3],0);
		
		SparseModel<VertexFactor> vmodel2 = new RemoveBarren().execute(modelV,mySkills[0],evidence);
		
		System.out.println(new DotSerialize().run(vmodel2));
		
		// Variable elimination
		SePolyVE ve = new SePolyVE();
		VertexFactor factor = ve.run(vmodel2,mySkills[0],evidence);
		
		// Print the posterior credal set
		for (double[] v : factor.getVertices()) {
			System.out.println("vertex: "+Arrays.toString(v));}
//
		//		DotSerialize ser = new DotSerialize();
		//		//System.out.println(ser.run(vmodel));





		// -------------- //
		// SET THE SKILLS //
		// -------------- //
		// Prepare the domains of the skills (nLevels states each)
		// Local models over the skills initialized
		IntervalFactor[] sFact = new IntervalFactor[nSkills]; // Array of factors
		Strides domSkill[] = new Strides[nSkills];
		int[] skill = new int[nSkills];
		for(int s=0;s<nSkills;s++){
			skill[s] = model.addVariable(nLevels);
			domSkill[s] = Strides.as(skill[s],nLevels);	
			if(s==0){
				sFact[s] = new IntervalFactor(domSkill[s],Strides.EMPTY);
				sFact[s].setLower(cnPars[0]);
				sFact[s].setUpper(cnPars[1]);
				System.out.println(Arrays.toString(cnPars[0]));
				System.out.println(Arrays.toString(cnPars[1]));
				

			}
			else{
				sFact[s] = new IntervalFactor(domSkill[s],domSkill[s-1]);
				sFact[s].setLower(cnPars[2],0); //P(Si|Si-1=0)
				sFact[s].setUpper(cnPars[3],0);
				sFact[s].setLower(cnPars[4],1); //P(Si|Si-1=0)
				sFact[s].setUpper(cnPars[5],1);
				sFact[s].setLower(cnPars[6],2); //P(Si|Si-1=0)
				sFact[s].setUpper(cnPars[7],2);
				sFact[s].setLower(cnPars[8],3); //P(Si|Si-1=0)
				sFact[s].setUpper(cnPars[9],3);
			}
			model.setFactor(skill[s],sFact[s]);
		}

		// ----------------------------- //
		// PARSE THE QUESTION PARAMETERS //
		// ----------------------------- //
		double[][][] qType = new double[nLevels][nLevels][2];
		// FIXME PUT A LOOP HERE
		qType[0][0] = Arrays.copyOfRange(cnPars[10],0,2);
		qType[0][1] = Arrays.copyOfRange(cnPars[11],0,2);
		qType[0][2] = Arrays.copyOfRange(cnPars[12],0,2);
		qType[0][3] = Arrays.copyOfRange(cnPars[13],0,2);
		qType[1][0] = Arrays.copyOfRange(cnPars[14],0,2);
		qType[1][1] = Arrays.copyOfRange(cnPars[15],0,2);
		qType[1][2] = Arrays.copyOfRange(cnPars[16],0,2);
		qType[1][3] = Arrays.copyOfRange(cnPars[17],0,2);
		qType[2][0] = Arrays.copyOfRange(cnPars[18],0,2);
		qType[2][1] = Arrays.copyOfRange(cnPars[19],0,2);
		qType[2][2] = Arrays.copyOfRange(cnPars[20],0,2);
		qType[2][3] = Arrays.copyOfRange(cnPars[21],0,2);
		qType[3][0] = Arrays.copyOfRange(cnPars[22],0,2);
		qType[3][1] = Arrays.copyOfRange(cnPars[23],0,2);
		qType[3][2] = Arrays.copyOfRange(cnPars[24],0,2);
		qType[3][3] = Arrays.copyOfRange(cnPars[25],0,2);

		// ----------------- //
		// SET THE QUESTIONS //
		// ----------------- //

		// A question for each skill (embedding all the answers)
		int nQuestions = nSkills;

		// Local models over the questions initialized
		IntervalFactor[] qFact = new IntervalFactor[nQuestions]; // Array of factors	

		// Prepare the domains of the questions (two states each)
		Strides domQuestion[] = new Strides[nQuestions];		
		int[] question = new int[nQuestions];
		for(int s=0;s<nQuestions;s++){
			question[s] = model.addVariable(2);
			domQuestion[s] = Strides.as(question[s],2);
			qFact[s] = new IntervalFactor(domQuestion[s],domSkill[s]);
			double[][] myLogs = new double[nLevels][2];
			double[][] lP = new double[nLevels][2];
			double[][] uP = new double[nLevels][2];

			for(int l=0;l<nLevels;l++){
				for(int l2=0;l2<4;l2++){
					myLogs[l][0] += Math.log(qType[l2][l][0])*rightQ[s][l2];
					myLogs[l][1] += Math.log(qType[l2][l][1])*rightQ[s][l2];
					myLogs[l][0] += Math.log(1.0-qType[l2][l][1])*wrongQ[s][l2];
					myLogs[l][1] += Math.log(1.0-qType[l2][l][0])*wrongQ[s][l2];}}	

			double[][] probs = fromLogsToProbs(myLogs);
			for(int l=0;l<nLevels;l++){
				lP[l][0] = probs[l][0];
				uP[l][0] = probs[l][1];
				lP[l][1] = 1.0-probs[l][1];
				uP[l][1] = 1.0-probs[l][0];
				qFact[s].setLower(lP[l].clone(),l);
				qFact[s].setUpper(uP[l].clone(),l);
			}
			model.setFactor(question[s],qFact[s]);}

		// Dummy variable implementing the observation of the questions
		// this is a common child of the three questions
		int dummy = model.addVariable(2);
		BayesianFactor fDummy = new BayesianFactor(model.getDomain(question[0],question[1],question[2],question[3],dummy), false);
		fDummy.setValue(1.0, 0,0,0,0, 1);	
		fDummy.setValue(1.0, 0,0,0,1, 0);	
		fDummy.setValue(1.0, 0,0,1,0, 0);
		fDummy.setValue(1.0, 0,0,1,1, 0);	
		fDummy.setValue(1.0, 0,1,0,0, 0);	
		fDummy.setValue(1.0, 0,1,0,1, 0);	
		fDummy.setValue(1.0, 0,1,1,0, 0);	
		fDummy.setValue(1.0, 0,1,1,1, 0);	
		fDummy.setValue(1.0, 1,0,0,0, 0);	
		fDummy.setValue(1.0, 1,0,0,1, 0);	
		fDummy.setValue(1.0, 1,0,1,0, 0);
		fDummy.setValue(1.0, 1,0,1,1, 0);	
		fDummy.setValue(1.0, 1,1,0,0, 0);	
		fDummy.setValue(1.0, 1,1,0,1, 0);	
		fDummy.setValue(1.0, 1,1,1,0, 0);	
		fDummy.setValue(1.0, 1,1,1,1, 0);	
		model.setFactor(dummy,fDummy);
		model = new RemoveBarren().execute(model, skill[queriedSkill], dummy);

		// Compute the inferences
		Inference approx = new Inference();
		approx.initialize(new HashMap<String, Object>(){{
			put(ISearch.MAX_TIME, "8");
			put(GreedyWithRandomRestart.MAX_RESTARTS, "4");
			put(GreedyWithRandomRestart.MAX_PLATEAU, "2");
		}});
		IntervalFactor resultsALP = null;
		try {
			resultsALP = approx.query(model,skill[queriedSkill],dummy);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Return the results of ApproxLP
		double[][] output = new double[2][nLevels];
		output[0] = resultsALP.getLower();
		output[1] = resultsALP.getUpper();
		return output;
	}}