package ch.idsia.crema.solver;

import ch.idsia.crema.solver.lpsolve.Simplex;

public class SolverFactory {
	public static int defaultLib = 0;
	
	public static LinearSolver getInstance(int lib) {
		if (lib == 0) 
			return new ch.idsia.crema.solver.commons.Simplex();
		else if (lib == 1) 
			return new Simplex();
		else 
			return null;
	}
	
	public static LinearSolver getInstance() {
		return getInstance(defaultLib);
	}
}
