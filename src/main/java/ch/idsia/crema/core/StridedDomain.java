package ch.idsia.crema.core;

public interface StridedDomain extends Domain {
	int getStride(int variable);
	int getStrideAt(int offset);
}
