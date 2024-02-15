package ch.idsia.crema.core;

import java.util.Arrays;

public class UnsortedDomain implements StridedDomain {
	private Strides sortedDomain;
	
	public UnsortedDomain(Variable[] variables) {
		
	}
	
	public UnsortedDomain(int[] variables, int[] sizes) {
		
	}
	
	@Override
	public int getCardinality(int variable) {
		return 0;
	}

	@Override
	public int getSizeAt(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int indexOf(int variable) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean contains(int variable) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getSizes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removed(int variable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStride(int variable) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStrideAt(int offset) {
		// TODO Auto-generated method stub
		return 0;
	}

}
