package ch.idsia.crema.alessandro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.search.ISearch;
import ch.idsia.crema.search.impl.GreedyWithRandomRestart;
import ch.idsia.crema.utility.ArraysUtil;

public class AdaptiveTestsForDavid {

	// Global variables
	public static final int nSkills = 4; // Number of skill variables
	public static final int nLevels = 4; // Number of state for the skill variables
	public static final double cutOff = 1E-2; // Cutoff to remove numerical problems
	public static final String myPath = "src/test/resources/adaptive/"; // Path to find input/output files
	public static final int nStudents = 1; // Number of students FIXME

	public static void main(String[] args) {

		// Text file where we store the BN and CN pars
		String credalFileName = "cnParameters.txt";
		String bayesFileName = "cnParametersBayes.txt";

		// Read the credal net and create a file with the Bayesian network
		System.out.println("Converting the credal in a Bayesian net ...");
		new AdaptiveFileTools().writeBNFile(myPath+credalFileName);
		System.out.println("Inference time ...");
		double[][][] right2 = {{{0.0, 6.0,9.0, 10.0},{0.0, 0.0,0.0, 0.0},{0.0, 4.0,5.0, 4.0},{0.0, 3.0,6.0, 0.0}}};
		double[][][] wrong2 = {{{0.0, 4.0,1.0, 0.0},{0.0, 8.0,8.0, 8.0},{0.0, 1.0,0.0, 1.0},{0.0, 3.0,4.0, 10.0}}};
		// Demonstrative answers sequence
		//double[][][] right2 = {{{1.0, 7.0, 9.0, 10.0},{0.0, 8.0, 8.0, 7.0},{0.0, 5.0, 5.0, 5.0},{0.0, 4.0, 6.0, 1.0}}};
		//double[][][] wrong2 = {{{0.0, 1.0, 2.0, 5.0},{0.0, 4.0, 7.0, 3.0},{0.0, 1.0, 2.0, 5.0},{0.0, 5.0, 8.0, 9.0}}};
		// Whole set of answers
		//double[][][] right2 = {{{0.0, 4.0, 5.0, 5.0},{0.0, 4.0, 1.0, 5.0},{0.0, 4.0, 3.0, 0.0},{0.0, 1.0, 2.0, 1.0}}, {{0.0, 3.0, 6.0, 2.0},{0.0, 4.0, 1.0, 1.0},{0.0, 3.0, 2.0, 0.0},{0.0, 2.0, 4.0, 4.0}}, {{0.0, 5.0, 0.0, 0.0},{0.0, 1.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}}};
		//double[][][] wrong2 = {{{10.0, 6.0, 5.0, 5.0},{0.0, 4.0, 7.0, 3.0},{0.0, 1.0, 2.0, 5.0},{0.0, 5.0, 8.0, 9.0}}, {{0.0, 7.0, 4.0, 8.0},{0.0, 4.0, 7.0, 7.0},{0.0, 2.0, 3.0, 5.0},{0.0, 4.0, 6.0, 6.0}}, {{0.0, 5.0, 10.0, 10.0},{0.0, 7.0, 8.0, 8.0},{0.0, 5.0, 5.0, 5.0},{0.0, 6.0, 10.0, 10.0}}};

		// Initialise objects
		AdaptiveTestsForDavid myTest = new AdaptiveTestsForDavid();
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
				probs[i][j]=Math.exp(logTab[i][j]-maximumLog-0.0);}}
				
		for(int i=0;i<nLevels;i++)
			System.out.println(Arrays.toString(probs[i]));
				//if(probs[i][j]<cutOff)
				//	probs[i][j]=cutOff;
				//if(probs[i][j]>1.0-cutOff)
				//	probs[i][j]=1.0-cutOff;

				//				if(probs[i][0]>probs[i][1]){
				//					double tmp;
				//					tmp = probs[i][0];
				//					probs[i][0] = probs[i][1];
				//					probs[i][1] = tmp;
				//				}
				//System.out.println(Arrays.toString(probs[i]));
			//}}

		return probs;
	}


	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	//@Test
	// s is the skill under consideration
	public double[][] germanTest(String fileName, int queriedSkill, double[][] rightQ, double[][] wrongQ) {

		// S0 -> S1 -> S2 -> S3
		//  v     v     v     v
		// Q0     Q1    Q2    Q3

		SparseModel<GenericFactor> model = new SparseModel<>();

		// Read probabilities from external file
		AdaptiveFileTools myTools = new AdaptiveFileTools();
		double[][] cnPars = myTools.readMyFile(fileName);

		// -------------- //
		// SET THE SKILLS //
		// -------------- //
		// Prepare the domains of the skills (nLevels states each)
		// Local models over the skills initialized
		IntervalFactor[] sFact = new IntervalFactor[nSkills]; // Array of factors
		Strides domSkill[] = new Strides[nSkills];
		int[] skill = new int[nSkills];
		double myEps = 1E-10;


		for(int s=0;s<nSkills;s++){
			skill[s] = model.addVariable(nLevels);
			domSkill[s] = Strides.as(skill[s],nLevels);	
			//for(int i=0;i<9;i++)
			//	ArraysUtil.roundArrayToTarget(cnPars[i],1.0,myEps);

			for(int i=0;i<10;i++)
				ArraysUtil.roundArrayToTarget(cnPars[i],1.0,1E-2);


			if(s==0){
				sFact[s] = new IntervalFactor(domSkill[s],Strides.EMPTY);
				sFact[s].setLower(cnPars[0]);
				sFact[s].setUpper(cnPars[1]);
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
			//System.out.println();
			for(int l=0;l<nLevels;l++){
				lP[l][0] = probs[l][0];
				uP[l][0] = probs[l][1];
				lP[l][1] = 1.0-probs[l][1];
				uP[l][1] = 1.0-probs[l][0];
				//				if(uP[l][1]==1.0)
				//					lP[l][0]= 0.0;
				//				if(uP[l][0]==1.0)
				//					lP[l][1]=0.0;
				//				if(lP[l][1]==1.0)
				//					uP[l][0]=0.0;
				//				if(lP[l][0]==1.0)
				//					lP[l][1] = 0.99999;
				//				if(uP[l][1]==1.0)
				//					lP[l][0] = 0.99999;
				//				if(uP[l][0]==1.0)
				//					lP[l][1] = 0.99999;
				//				if(lP[l][1]==1.0)
				//					lP[l][0] = 0.99999;
				//				if(lP[l][0]==1.0)
				//					lP[l][1] = 0.99999;
				//lP[l][1] = 0.0;-probs[l][1];
				//uP[l][1] = 1.0-probs[l][0];				
				System.out.println("L"+Arrays.toString(lP[l]));
				System.out.println("U"+Arrays.toString(uP[l]));
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