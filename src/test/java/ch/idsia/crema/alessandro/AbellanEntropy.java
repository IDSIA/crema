package ch.idsia.crema.alessandro;

import java.util.stream.DoubleStream;
public class AbellanEntropy {

	public static void main(String[] args) {
		//INPUT
		double[] l = new double[] {0.3,0.4,0.0,0.0,0.0};
		double[] u = new double[] {1.0,1.0,0.04,1.0,1.0};
		
		//double[] l = new double[] {0.08771929824561402, 0.21052631578947367, 0.3508771929824561, 0.3508771929824561};
		//double[] u = new double[] {0.08771929824561402, 0.21052631578947367, 0.3508771929824561, 0.3508771929824561};

		//double[] l = new double[] {0.3164556962025316, 0.3164556962025316, 0.05063291139240505, 0.3164556962025316};
		//double[] u = new double[] {0.31645569620253167, 0.31645569620253167, 0.05063291139240508, 0.31645569620253167};
		
		new AbellanEntropy().getMaxEntro(l,u);
	}

	// Return the index of the minimum value of arr
	// among the values such that the corresponding
	// elements of b are true
	private int minLS(double[] arr, boolean[] b){
		int index=0;
		boolean[] b2 = b.clone();
		double myMin;		
		if(!allFalse(b2)){
			for(int i=0;i<arr.length;i++){
				if(b2[i]){
					index = i;
					break;}}
			myMin = arr[index];
			for(int i=0;i<b2.length;i++){
				if(b2[i]){
					if(arr[i]<myMin){
						myMin = arr[i];
						index = i;}}}}
		else{ index = -1;}
		return index;}

	// Return the number of occurrences of the minimum of arr 
	// only over the values of arr such that the corresponding
	// element of b is true
	private int nMinLS(double[] arr, boolean[] b){
		double myMin = arr[minLS(arr,b)];
		int q = 0;
		for(int i=0;i<b.length;i++){
			if(b[i]){
				if(Math.abs(arr[i]-myMin)<1E-10){
					q++;
				}
			}
		}
		return q;}

	// Find the index of the second smallest element of arr
	// among the values corresponding to the true values of b
	private int secondminLS(double[] arr, boolean[] b){
		boolean[] b2 = b.clone();
		int index = minLS(arr,b2);
		double min1 = arr[index];
		for(int i=0;i<arr.length;i++)
			if(arr[i]==min1)
				b2[i]=false;			
		int index2 = -1;
		//if(index!=-1){ b[index]=false;
		index2 = minLS(arr,b2);//}
		return index2;}

	// Boolean function to check whether or not all
	// the elements of arr are false
	private boolean allFalse(boolean[] arr){
		for(boolean b: arr)
			if(b)
				return false;
		return true;
	}

	public double[] getMaxEntro(double[] l,double[] u) {
		// ALGORITHM
		double ss;
		int r,f,m;
		boolean[] S = new boolean[l.length];
		for(int i=0;i<l.length;i++){
			S[i]=true;} // S initialisation
		while(DoubleStream.of(l).sum()<1.0){
			for(int i=0;i<l.length;i++){
				if(u[i]==l[i]){
					S[i]=false;}}
			ss = DoubleStream.of(l).sum();
			r = minLS(l,S);
			f = secondminLS(l,S);
			m = nMinLS(l,S);
			for(int i=0;i<l.length;i++){
				if(l[i]==l[minLS(l,S)]){
					if(f==-1){
						l[i]+=Math.min(u[i]-l[i],Math.min((1-ss)/m,1));
					}
					else{
						l[i]+=Math.min(u[i]-l[i],Math.min(l[f]-l[r],(1-ss)/m));}				
				}				
			}
		}
		return l;
	}	
}