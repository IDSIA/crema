package ch.idsia.crema.alessandro;

import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;

import java.io.*;
import java.util.Locale;
import java.util.regex.Pattern;

public class AdaptiveFileTools {

	//public static void main(String[] args) {}

	public static boolean isNumeric(String str)  
	{  try { double d = Double.parseDouble(str);}  
	catch(NumberFormatException nfe)  
	{ return false;}  
	return true;}

	// Read a credal network specification and
	// write a consistent Bayesian network specification
	public void writeBNFile(String nameFile){
		String[] parts = nameFile.split(Pattern.quote(".")); // Split on period.
		String newNameFile = parts[0]+"Bayes."+parts[1];		
		double[][] credalValues = readMyFile(nameFile);
		double[][] bayesianValues = credalValues.clone();		
		double[][] bounds = new double[2][4];
		CNGenerator myGen = new CNGenerator();
		for(int i=0;i<=8;i=i+2){
			bounds[0]=credalValues[i];
			bounds[1]=credalValues[i+1];
			bayesianValues[i] = myGen.fromIntervalsToCoM(bounds);
			bayesianValues[i+1] = bayesianValues[i];}		
		for(int i=0;i<credalValues.length;i++){
			if(i>9){
				bayesianValues[i][0]=(credalValues[i][0]+credalValues[i][1])/2;
				bayesianValues[i][1]=bayesianValues[i][0];
			}}

		try{
			PrintStream output = new PrintStream(new File(newNameFile));
			output.println("// Sample Credal network specification");
			for(int i=0;i<bayesianValues.length;i++){
				for(double value : bayesianValues[i]){    				 
					// display ss to make sure program works correctly
					output.print(String.format(Locale.ROOT, "%2.10f ", value));}
				output.print("\n");}
			output.close();
		}catch(FileNotFoundException e){
			System.out.println("Cannot write file!");
		}




	}

	public double[][] readMyFile(String nameFile){
		double[][] output = new double[26][4];
		for(int i=0;i<output.length;i++)
			for(int j=0;j<output[0].length;j++)
				output[i][j]=Double.NaN;	
		String scan;//,current;
		FileReader file;
		try {
			file = new FileReader(nameFile);
			BufferedReader br = new BufferedReader(file);
			String first;
			int rowNumber = 0;
			int col;
			first = br.readLine();
			//System.out.println(first);
			while((scan = br.readLine()) != null) {
				if(!(scan.startsWith("//"))){ // Ignore comments
					col = 0;
					for(String element : scan.split(" ")){
						if(isNumeric(element)){
							output[rowNumber][col]=Double.parseDouble(element);
							col++;}}
					rowNumber++;}}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();}
		return output;
	}
}