package ch.idsia.crema.factor.symbolic.serialize;

import ch.idsia.crema.solver.ipopt.Ipopt;

public class IpropSolver extends Ipopt {

	@Override
	protected boolean get_bounds_info(int n, double[] x_l, double[] x_u, int m, double[] g_l, double[] g_u) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean get_starting_point(int n, boolean init_x, double[] x, boolean init_z, double[] z_L, double[] z_U, int m, boolean init_lambda, double[] lambda) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean eval_f(int n, double[] x, boolean new_x, double[] obj_value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean eval_grad_f(int n, double[] x, boolean new_x, double[] grad_f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean eval_g(int n, double[] x, boolean new_x, int m, double[] g) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean eval_jac_g(int n, double[] x, boolean new_x, int m, int nele_jac, int[] iRow, int[] jCol, double[] values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean eval_h(int n, double[] x, boolean new_x, double obj_factor, int m, double[] lambda, boolean new_lambda, int nele_hess, int[] iRow, int[] jCol, double[] values) {
		// TODO Auto-generated method stub
		return false;
	}

}
