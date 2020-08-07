package ch.idsia.crema.factor.credal.linear;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import jdk.jshell.spi.ExecutionControl;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;

import ch.idsia.crema.model.Strides;

import javax.sound.sampled.Line;

/**
 * A separately specified Credal factor that has a list of linear constrains for each
 * separation.
 *
 * @author huber
 */
public class SeparateHalfspaceFactor extends SeparateFactor<SeparateHalfspaceFactor> implements SeparateLinearFactor<SeparateHalfspaceFactor> {

    private ArrayList<ArrayList<LinearConstraint>> data;

    public SeparateHalfspaceFactor(Strides content, Strides separation) {
        super(content, separation);

        data = new ArrayList<>(separation.getCombinations());
        for (int i = 0; i < separation.getCombinations(); i++) {
            data.add(new ArrayList<LinearConstraint>());
        }
    }

    public SeparateHalfspaceFactor(Strides content, Strides separation, ArrayList<ArrayList<LinearConstraint>> data) {
        super(content, separation);
        this.data = data;
    }


    public SeparateHalfspaceFactor(Strides left, double[][] coefficients, double[] values, Relationship... rel) {
        this(left, Strides.empty());

        // Build the constraints (including non-negative constraints)
        LinearConstraint[] C = buildConstraints(true, true, coefficients, values, rel);

        // add constraints to this factor
        for (LinearConstraint c : C) {
            this.addConstraint(c);
        }
    }


    public SeparateHalfspaceFactor(boolean normalized, boolean nonnegative, Strides left, double[][] coefficients, double[] values, Relationship... rel) {
        this(left, Strides.empty());

        LinearConstraint[] C = buildConstraints(normalized, nonnegative, coefficients, values, rel);

        // add constraints to this factor
        for (LinearConstraint c : C) {
            this.addConstraint(c);
        }
    }
    public SeparateHalfspaceFactor(Strides left, Strides right, double[][][] coefficients, double[][] values, Relationship rel) {
        this(left, right);

        for (int i = 0; i < right.getCombinations(); i++) {
            // Build the constraints (including normalization and non-negative one)
            LinearConstraint[] C = buildConstraints(true, true, coefficients[i], values[i], rel);
            // add constraints to this factor
            for (LinearConstraint c : C) {
                this.addConstraint(c, i);
            }
        }
    }


    public static LinearConstraint[] buildConstraints(boolean normalized, boolean nonnegative, double[][] coefficients, double[] values, Relationship... rel) {

        int left_combinations = coefficients[0].length;
        List<LinearConstraint> C = new ArrayList<LinearConstraint>();


        // check the coefficient shape
        for (double[] c : coefficients) {
            if (c.length != left_combinations)
                throw new IllegalArgumentException("ERROR: coefficient matrix shape");
        }

        // check the relationship vector length
        if (rel.length == 0) rel = new Relationship[]{Relationship.EQ};
        if (rel.length == 1) {
            Relationship[] rel_aux = new Relationship[coefficients.length];
            for (int i = 0; i < coefficients.length; i++)
                rel_aux[i] = rel[0];
            rel = rel_aux;
        } else if (rel.length != coefficients.length) {
            throw new IllegalArgumentException("ERROR: wrong relationship vector length: " + rel.length);
        }

        for (int i = 0; i < coefficients.length; i++) {
            C.add(new LinearConstraint(coefficients[i], rel[i], values[i]));
        }


        // normalization constraint
        if (normalized) {
            double[] ones = new double[left_combinations];
            for (int i = 0; i < ones.length; i++)
                ones[i] = 1.;
            C.add(new LinearConstraint(ones, Relationship.EQ, 1.0));
        }

        // non-negative constraints
        if (nonnegative) {
            double[] zeros = new double[left_combinations];
            for (int i = 0; i < left_combinations; i++) {
                double[] c = zeros.clone();
                c[i] = 1.;
                C.add(new LinearConstraint(c, Relationship.GEQ, 0));

            }
        }

        return C.toArray(LinearConstraint[]::new);
    }


    @Override
    public SeparateHalfspaceFactor copy() {
        ArrayList<ArrayList<LinearConstraint>> new_data = new ArrayList<>(groupDomain.getCombinations());
        for (int i = 0; i < data.size(); i++) {
            ArrayList original = new ArrayList<>(data.get(i));
            ArrayList new_matrix = new ArrayList(original.size());
            for (int j = 0; j < original.size(); j++) {
                LinearConstraint original_row = (LinearConstraint) original.get(j);
                LinearConstraint copy = new LinearConstraint(original_row.getCoefficients().copy(), original_row.getRelationship(), original_row.getValue());
                new_matrix.add(copy);
            }
            new_data.add(new_matrix);
        }
        return new SeparateHalfspaceFactor(dataDomain, groupDomain, new_data);
    }
/*
	public SeparateHalfspaceFactor copy() {
		ArrayList<ArrayList<LinearConstraint>> new_data = new ArrayList<>(groupDomain.getCombinations());
		for (ArrayList<LinearConstraint> original : data) {
			ArrayList<LinearConstraint> new_matrix = new ArrayList<>(original.size());
			for (LinearConstraint original_row : original) {
				LinearConstraint copy = new LinearConstraint(original_row.getCoefficients().copy(), original_row.getRelationship(), original_row.getValue());
				new_matrix.add(copy);
			}
			new_data.add(new_matrix);
		}
		return new SeparateHalfspaceFactor(dataDomain, groupDomain, new_data);
	}
*/


    public void addConstraint(double[] data, Relationship rel, double value, int... states) {
        this.addConstraint(new LinearConstraint(data, rel, value), states);
    }

    public void addConstraint(LinearConstraint c, int... states) {
        int offset = groupDomain.getOffset(states);
        this.data.get(offset).add(c);
    }


    public double[] getRandomVertex(int... states) {
        Random random = new Random();
        int offset = groupDomain.getOffset(states);
        SimplexSolver solver = new SimplexSolver();

        double[] coeffs = new double[dataDomain.getCombinations()];
        for (int i = 0; i < dataDomain.getCombinations(); ++i) {
            coeffs[i] = random.nextDouble() + 1;
        }

        LinearObjectiveFunction c = new LinearObjectiveFunction(coeffs, 0);
        LinearConstraintSet Ab = new LinearConstraintSet(data.get(offset));

        PointValuePair pvp = solver.optimize(Ab, c);
        return pvp.getPointRef();
    }


    /**
     * Filter the factor by setting a specific variable to a state. This only works when the
     * var is in the grouping/separated part.
     *
     * <p>Note that the constraits sets are not copied. So changing this factor will update
     * also the filtered one.</p>
     *
     * @param variable
     * @param state
     * @return
     */

    @Override
    public SeparateHalfspaceFactor filter(int variable, int state) {
        int var_offset = groupDomain.indexOf(variable);

        ArrayList new_constraints = new ArrayList();
        Strides new_datadomain = dataDomain, new_groupdomain = groupDomain;

        // todo: consider case with more than one variable on the left

        if (dataDomain.contains(variable)) {
            for (int i = 0; i < groupDomain.getCombinations(); i++)
                new_constraints.add(SeparateHalfspaceFactor.deterministic(dataDomain, state).getLinearProblem().getConstraints());

        } else {
            IndexIterator it = this.getSeparatingDomain().getFiteredIndexIterator(new int[]{variable}, new int[]{state});
            while (it.hasNext())
                new_constraints.add(data.get(it.next()));

            new_groupdomain = groupDomain.removeAt(var_offset);

        }
        return new SeparateHalfspaceFactor(new_datadomain, new_groupdomain, new_constraints);

    }


/*	@Override
	public SeparateHalfspaceFactor filter(int variable, int state) {
		int var_offset = groupDomain.indexOf(variable);
		int var_stride = groupDomain.getStrideAt(var_offset);
		int next_stride = groupDomain.getStrideAt(var_offset + 1);
		
		int state_offset = var_stride * state;
		int block_count = next_stride / var_stride;
		
		Strides new_domain = groupDomain.removeAt(var_offset);
		int new_size = new_domain.getCombinations();
		ArrayList<ArrayList<LinearConstraint>> new_constraints = new ArrayList<>(new_size);
		
		for (int i = 0; i < block_count; ++i) {
			int offset = i * next_stride + state_offset;
			new_constraints.addAll(data.subList(offset, offset + var_stride));
		}
		
		return new SeparateHalfspaceFactor(dataDomain, new_domain, new_constraints);
	}*/

    @Override
    public LinearConstraintSet getLinearProblem(int... states) {
        int offset = groupDomain.getOffset(states);
        return new LinearConstraintSet(data.get(offset));
    }

    public void printLinearProblem(int... states) {

        if (states.length > 0) {
            Iterator it = this.getLinearProblem(states).getConstraints().iterator();
            while (it.hasNext()) {
                LinearConstraint c = (LinearConstraint) it.next();
                System.out.println(c.getCoefficients() + "\t" + c.getRelationship() + "\t" + c.getValue());
            }
        } else {

            for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
                this.printLinearProblem(i);
                System.out.println("-------");
            }


        }


    }

    @Override
    public LinearConstraintSet getLinearProblemAt(int offset) {
        return new LinearConstraintSet(data.get(offset));
    }

    /**
     * Modifies the linear problem at a given offset.
     *
     * @param offset
     * @param constraints
     */
    public void setLinearProblemAt(int offset, LinearConstraintSet constraints) {
        ArrayList<LinearConstraint> list = new ArrayList<LinearConstraint>();
        for (LinearConstraint c : constraints.getConstraints())
            list.add(c);
        data.set(offset, list);
    }


    /**
     * Static method that builds a deterministic factor (values can only be ones or zeros).
     * Thus, children variables are determined by the values of the parents
     *
     * @param left        Strides - children variables.
     * @param right       Strides - parent variables
     * @param assignments assignments of each combination of the parent
     * @return
     */

    public static SeparateHalfspaceFactor deterministic(Strides left, Strides right, int... assignments) {

        if (assignments.length != right.getCombinations())
            throw new IllegalArgumentException("ERROR: length of assignments should be equal to the number of combinations of the parents");

        if (Ints.min(assignments) < 0 || Ints.max(assignments) >= left.getCombinations())
            throw new IllegalArgumentException("ERROR: assignments of deterministic function should be in the inteval [0," + left.getCombinations() + ")");


        SeparateHalfspaceFactor f = new SeparateHalfspaceFactor(left, right);

        int left_combinations = left.getCombinations();

        for (int i = 0; i < right.getCombinations(); i++) {
            double[][] coeff = new double[left_combinations][left_combinations];
            for (int j = 0; j < left_combinations; j++) {
                coeff[j][j] = 1.;
            }
            double[] values = new double[left_combinations];
            values[assignments[i]] = 1.;


            // Build the constraints
            LinearConstraint[] C = SeparateHalfspaceFactor.buildConstraints(true, true, coeff, values, Relationship.EQ);

            // Add the constraints
            for (LinearConstraint c : C) {
                f.addConstraint(c, i);
            }
        }

        return f;
    }

    /**
     * Static method that builds a deterministic factor (values can only be ones or zeros)
     * without parent variables.
     *
     * @param left       Strides - children variables.
     * @param assignment int - single value to assign
     * @return
     */


    public static SeparateHalfspaceFactor deterministic(Strides left, int assignment) {
        return SeparateHalfspaceFactor.deterministic(left, Strides.empty(), assignment);
    }

    /**
     * Static method that builds a deterministic factor (values can only be ones or zeros)
     * without parent variables.
     *
     * @param var        int - id for the single children variable.
     * @param assignment int - single value to assign
     * @return
     */

    public SeparateHalfspaceFactor get_deterministic(int var, int assignment) {
        return SeparateHalfspaceFactor.deterministic(this.getDomain().intersection(var), assignment);
    }


    public ArrayList<ArrayList<LinearConstraint>> getData() {
        return data;
    }


    public SeparateHalfspaceFactor getPerturbed(double eps) {

        SeparateHalfspaceFactor newFactor = new SeparateHalfspaceFactor(this.getDataDomain(), this.getSeparatingDomain());

        for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
            for (LinearConstraint c : this.getLinearProblem(i).getConstraints()) {

                if (c.getRelationship() == Relationship.EQ && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() - eps, i);
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.LEQ, c.getValue() + eps, i);
                } else if (c.getRelationship() == Relationship.GEQ && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() + eps, i);
                } else if (c.getRelationship() == Relationship.LEQ && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.LEQ, c.getValue() - eps, i);
                } else {
                    newFactor.addConstraint(c, i);
                }

            }
        }

        return newFactor;

    }


    public SeparateHalfspaceFactor getPerturbedZeroConstraints(double eps) {

        SeparateHalfspaceFactor newFactor = new SeparateHalfspaceFactor(this.getDataDomain(), this.getSeparatingDomain());

        for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
            for (LinearConstraint c : this.getLinearProblem(i).getConstraints()) {

                if (c.getRelationship() == Relationship.EQ && c.getValue() == 0 && eps > 0) {

                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, 0.0, i);
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.LEQ, eps, i);
                } else if (c.getRelationship() == Relationship.GEQ && c.getValue() == 0 && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() + eps, i);
                } else {
                    newFactor.addConstraint(c, i);
                }


            }
        }

        return newFactor;

    }


    public SeparateHalfspaceFactor getPerturbedEqualitiesToOne(double eps) {

        SeparateHalfspaceFactor newFactor = new SeparateHalfspaceFactor(this.getDataDomain(), this.getSeparatingDomain());

        for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
            for (LinearConstraint c : this.getLinearProblem(i).getConstraints()) {

                if (c.getRelationship() == Relationship.EQ && c.getValue() == 1 && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, 1 - eps, i);
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.LEQ, 1 + eps, i);
                }
					/*else if(c.getRelationship() == Relationship.GEQ && eps>0) {
						newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() + eps, i);
					}*/
                else {
                    newFactor.addConstraint(c, i);
                }
            }

        }


        return newFactor;

    }


    public SeparateHalfspaceFactor getNoisedInequalities(double eps) {

        SeparateHalfspaceFactor newFactor = new SeparateHalfspaceFactor(this.getDataDomain(), this.getSeparatingDomain());

        for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
            for (LinearConstraint c : this.getLinearProblem(i).getConstraints()) {

                if (c.getRelationship() == Relationship.GEQ && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() + eps, i);
                } else if (c.getRelationship() == Relationship.LEQ && eps > 0) {
                    newFactor.addConstraint(c.getCoefficients().toArray(), Relationship.LEQ, c.getValue() - eps, i);
                } else {
                    newFactor.addConstraint(c, i);
                }

            }
        }

        return newFactor;

    }


    public SeparateHalfspaceFactor removeNormConstraints() {
        SeparateHalfspaceFactor newFactor = new SeparateHalfspaceFactor(this.getDataDomain(), this.getSeparatingDomain());
        for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
            for (LinearConstraint c : this.getLinearProblem(i).getConstraints()) {
                if (!(c.getRelationship() == Relationship.EQ && c.getValue() == 1
                        && DoubleStream.of(c.getCoefficients().toArray()).allMatch(x -> x == 1))) {
                    newFactor.addConstraint(c, i);
                }
            }
        }
        return newFactor;
    }


    public static Collection<LinearConstraint> getNoisedConstraintSet(Collection<LinearConstraint> constraints, double eps) {

        Collection<LinearConstraint> newConstraints = new ArrayList<LinearConstraint>();

        for (LinearConstraint c : constraints) {

            if (c.getRelationship() == Relationship.EQ && eps > 0) {
                newConstraints.add(new LinearConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() - eps));
                newConstraints.add(new LinearConstraint(c.getCoefficients().toArray(), Relationship.LEQ, c.getValue() + eps));
            } else if (c.getRelationship() == Relationship.GEQ && eps > 0) {
                newConstraints.add(new LinearConstraint(c.getCoefficients().toArray(), Relationship.GEQ, c.getValue() + eps));
            } else if (c.getRelationship() == Relationship.LEQ && eps > 0) {
                newConstraints.add(new LinearConstraint(c.getCoefficients().toArray(), Relationship.LEQ, c.getValue() - eps));
            } else {
                newConstraints.add(c);
            }

        }
        return newConstraints;

    }





    /**
     * Replaces the IDs of the variables in the domain
     *
     * @param new_vars
     * @return
     */

    @Override
    public SeparateHalfspaceFactor renameDomain(int... new_vars) {

        int[] leftIdx = IntStream.range(0, this.getDataDomain().getVariables().length).toArray();
        int[] rightIdx = IntStream.range(this.getDataDomain().getVariables().length, new_vars.length).toArray();

        int[] sizes = Ints.concat(getDataDomain().getSizes(), getSeparatingDomain().getSizes());


        Strides leftStrides = new Strides(
                ArraysUtil.slice(new_vars, leftIdx),
                ArraysUtil.slice(sizes, leftIdx)
        );

        Strides rightStrides = new Strides(
                ArraysUtil.slice(new_vars, rightIdx),
                ArraysUtil.slice(sizes, rightIdx)
        );

        return new SeparateHalfspaceFactor(leftStrides, rightStrides, this.getData());
    }




    /**
     * Retruns all the constraints
     *
     * @param data
     */
    public void setData(ArrayList<ArrayList<LinearConstraint>> data) {
        this.data = data;
    }


    /**
     * Sorts the parents following the global variable order
     *
     * @return
     */
    @Override
    public SeparateHalfspaceFactor sortParents() {
        Strides oldLeft = getSeparatingDomain();
        Strides newLeft = oldLeft.sort();
        int parentComb = this.getSeparatingDomain().getCombinations();
        IndexIterator it = oldLeft.getReorderedIterator(newLeft.getVariables());
        int j;

        SeparateHalfspaceFactor newFactor = new SeparateHalfspaceFactor(getDataDomain(), newLeft);

        // i -> j
        for (int i = 0; i < parentComb; i++) {
            j = it.next();
            newFactor.setLinearProblemAt(i, getLinearProblemAt(j));
        }
        return newFactor;
    }


}