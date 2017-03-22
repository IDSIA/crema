package ch.idsia.crema.alessandro;

import java.util.Arrays;

public class CredalClassifiersEvaluation {

	// Printing utility
	public void computeArray(String label,double[] arr){
		System.out.printf(label+":\t%2.2f  [skills:\t",Arrays.stream(arr).sum()/arr.length);
		for(double el : arr)
			System.out.printf("%2.2f\t",el);
		System.out.print("]\n");}

	public static double utility(double x,int level){
		if(level==65)
			return -0.6*x*x+1.6*x;
		else
			return -1.2*x*x+2.2*x;
	}
	//@Test
	public void analyzer(int[][] truth,boolean[][][] credal,int[][] bayesian){

		// Parsing dimensions
		int nStudents = truth.length;
		int nSkills = truth[0].length;

		// Initializing descriptors
		int[] bayesianRight  = new int[nSkills];
		double[] bayesianAcc  = new double[nSkills];
		int[] credalRight  = new int[nSkills];
		double[] credalSetAcc  = new double[nSkills];
		double[] credalDiscount = new double[nSkills];
		double[] credalDiscAcc  = new double[nSkills];
		int[] credalDeterm = new int[nSkills];
		double[] credalDet  = new double[nSkills];
		int[] indetSize = new int[nSkills];
		int nCredalLevels;
		int[] bayesianRightDet  = new int[nSkills];
		int[] bayesianRightIndet  = new int[nSkills];
		double[] bayesianAccDet  = new double[nSkills];
		double[] bayesianAccIndet  = new double[nSkills];
		double[] credalU65  = new double[nSkills];
		double[] credalU80  = new double[nSkills];		
		double[] indetOutSize = new double[nSkills];
		double discountedGain;


		// Loop over all the skills
		for(int skill=0;skill<nSkills;skill++){

			// COLLECT COUNTS
			
			// Loop over all the students
			for(int student=0;student<nStudents;student++){

				// Check whether the Bayesian was right
				if(bayesian[student][skill]==truth[student][skill])
					bayesianRight[skill]++;

				// Count the number of levels returned by the credal
				nCredalLevels = 0;
				for(Boolean level : credal[student][skill])
					if (level.booleanValue())
						nCredalLevels++;

				// Check whether the right level is 
				// included in the set of credal options
				if(credal[student][skill][truth[student][skill]]){
					credalRight[skill]++;

					// If the credal returned the right level
					// Compute the discounted/u65/u90 utility
					discountedGain = 1.0/nCredalLevels;
					credalDiscount[skill] += discountedGain;
					credalU65[skill] += utility(discountedGain,65);
					credalU80[skill] += utility(discountedGain,80);
				}

				// Check whether or not the credal was
				// determinate (i.e., a single option
				// was returned)
				if(nCredalLevels==1){
					credalDeterm[skill]++;

					// When determinate, check whether or not 
					// the Bayesian classifier was right
					if(bayesian[student][skill]==truth[student][skill])
						bayesianRightDet[skill]++;
				}
				else{
					// When determinate, check whether or not 
					// the Bayesian classifier was right
					if(bayesian[student][skill]==truth[student][skill])
						bayesianRightIndet[skill]++;

					// Count the overall number of levels
					// returned when indeterminate
					indetSize[skill] += nCredalLevels;
				}
			}

			// COMPUTE DESCRIPTORS
			
			// Bayesian accuracy (i.e., percentage
			// of instances in which the Bayesian
			// was right)
			bayesianAcc[skill]=100.0*bayesianRight[skill]/nStudents;
			
			// Bayesian Det-accuracy (i.e., percentage
			// of instances in which the Bayesian
			// was right when the credal was determinate
			if(credalDeterm[skill]>0)
				bayesianAccDet[skill]=100.0*bayesianRightDet[skill]/credalDeterm[skill];
			else
				bayesianAccDet[skill]=Double.NaN;

			// Bayesian Indet-accuracy (i.e., percentage
			// of instances in which the Bayesian
			// was right when the credal was indeterminate
			if(credalDeterm[skill]<nStudents)
				bayesianAccIndet[skill]=100.0*bayesianRightIndet[skill]/(nStudents-credalDeterm[skill]);
			else
				bayesianAccIndet[skill]=Double.NaN;					

			// credal set-accuracy (i.e., percentage
			// of instances in which the credal
			// was including the right option)
			credalSetAcc[skill]=100.0*credalRight[skill]/nStudents;

			// credal determinacy (i.e., percentage
			// of instances in which the credal
			// was returning a single option)
			credalDet[skill]=100.0*credalDeterm[skill]/nStudents;

			// credal discounted/u65/u80 accuracy
			credalDiscAcc[skill]=100.0*credalDiscount[skill]/nStudents;
			credalU65[skill]=100.0*credalU65[skill]/nStudents;
			credalU80[skill]=100.0*credalU80[skill]/nStudents;

			// indeterminate output size (i.e. average
			// number of levels returned in output)
			if(credalDeterm[skill]!=nStudents)
				indetOutSize[skill]=indetSize[skill]/(nStudents-credalDeterm[skill]);
			else
				indetOutSize[skill]=Double.NaN;
		}

		CredalClassifiersEvaluation myT = new CredalClassifiersEvaluation();
		myT.computeArray("[Bayes ACC]",bayesianAcc);
		myT.computeArray("[Credal U65]",credalU65);		
		myT.computeArray("[Credal DET]",credalDet);
		System.out.println();		
		myT.computeArray("[Bayes DetACC]",bayesianAccDet);
		myT.computeArray("[Bayes IndACC]",bayesianAccIndet);
		System.out.println();		
		myT.computeArray("[Credal setAcc]",credalSetAcc);
		myT.computeArray("[Credal disAcc]",credalDiscAcc);		
		myT.computeArray("[Credal u80Acc]",credalU80);		
		myT.computeArray("Credal (size)",indetOutSize);		
	}}