package ch.idsia.crema.factor.symbolic.serialize;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.extensive.ExtensiveLinearFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.symbolic.*;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class MOD implements SolverSerializer {

    private HashMap<SymbolicFactor, String> factorname = new HashMap<>();

    private StringBuilder variables = new StringBuilder();

    private StringBuilder constraints = new StringBuilder();
    private int constraint_count = 0;

    private String getVariable(SymbolicFactor factor, int left) {
        String name;
        if (!factorname.containsKey(factor)) {
            name = "v" + factorname.size();
            factorname.put(factor, name);
        } else {
            name = factorname.get(factor);
        }
        return name + "l" + left;
    }

    private void dispatch(SymbolicFactor factor) {
        if (factor instanceof PriorFactor) {
            processPriorFactor((PriorFactor) factor);
        } else if (factor instanceof CombinedFactor) {
            processCombinedFactor((CombinedFactor) factor);
        } else if (factor instanceof MarginalizedFactor) {
            processMarginalizedFactor((MarginalizedFactor) factor);
        } else if (factor instanceof DividedFactor) {
            processDividedFactor((DividedFactor) factor);
        } else if (factor instanceof FilteredFactor) {
            processFilteredFactor((FilteredFactor) factor);
        }
    }

    private void processDividedFactor(DividedFactor factor) {
        Strides new_dom = factor.getDomain();

        IndexIterator new_iter = new_dom.getIterator();
        IndexIterator num_iter = factor.getNumerator().getDomain().getIterator(new_dom);
        IndexIterator den_iter = factor.getDenominator().getDomain().getIterator(new_dom);

        while (new_iter.hasNext()) {
            int new_offset = new_iter.next();
            String name = getVariable(factor, new_offset);

            String num_name = getVariable(factor.getNumerator(), num_iter.next());
            String den_name = getVariable(factor.getDenominator(), den_iter.next());
            addDivision(name, num_name, den_name);
        }
    }

    private void processFilteredFactor(FilteredFactor factor) {
        int var = factor.getVariable();
        int state = factor.getState();

        Strides new_dom = factor.getDomain();
        Strides old_dom = factor.getSource().getDomain();

        IndexIterator new_iter = new_dom.getIterator();
        IndexIterator old_iter = old_dom.getIterator(new_dom, ObservationBuilder.observe(var, state));
        while (new_iter.hasNext()) {
            int new_offset = new_iter.next();
            int old_offset = old_iter.next();

            String new_name = getVariable(factor, new_offset);
            String old_name = getVariable(factor, old_offset);

            addBounds(new_name, 0, 1);
            addEquality(new_name, old_name);
        }
    }

    private void processMarginalizedFactor(MarginalizedFactor factor) {
        int var = factor.getVariable();
        Strides new_dom = factor.getDomain();
        Strides old_dom = factor.getSource().getDomain();

        int states = old_dom.getCardinality(var);

        int[] vars = ArraysUtil.preAppend(new_dom.getVariables(), var);

        IndexIterator sourceIter = old_dom.getReorderedIterator(vars);
        IndexIterator targetIter = new_dom.getIterator();
        while (targetIter.hasNext()) {
            int offset = targetIter.next();
            String name = getVariable(factor, offset);
            addBounds(name, 0, 1);

            String[] items = new String[states];
            for (int state = 0; state < states; state++) {
                int src_offset = sourceIter.next();
                //System.out.println(src_offset + Arrays.toString(old_dom.getStatesFor(src_offset)));
                items[state] = getVariable(factor.getSource(), src_offset);
            }
            addSum(name, items);
        }

    }

    private void processCombinedFactor(CombinedFactor combined) {
        Strides new_dom = combined.getDomain();

        IndexIterator new_iter = new_dom.getIterator();
        IndexIterator[] old_iters = Arrays.stream(combined.getSources()).map(SymbolicFactor::getDomain)
                .map(a -> a.getIterator(new_dom)).toArray(IndexIterator[]::new);

        while (new_iter.hasNext()) {
            int new_offset = new_iter.next();
            String name = getVariable(combined, new_offset);
            addBounds(name, 0, 1);
            String[] items = new String[old_iters.length];
            for (int i = 0; i < old_iters.length; ++i) {
                int offset = old_iters[i].next();
                items[i] = getVariable(combined.getSources()[i], offset);
            }
            addProduct(name, items);
        }
    }

    private void processPriorFactor(PriorFactor prior) {
        GenericFactor factor = prior.getFactor();
        if (factor instanceof IntervalFactor) {
            IntervalFactor iFactor = (IntervalFactor) factor;
            Strides separator = iFactor.getSeparatingDomain();
            Strides datadom = iFactor.getDataDomain();

            Strides full = iFactor.getDomain();

            IndexIterator iter = full.getIterator();
            IndexIterator sep = separator.getIterator(full);
            IndexIterator data = datadom.getIterator(full);

            while (iter.hasNext()) {
                int offset = iter.next();
                int left = data.next();
                int right = sep.next();

                double[] lowers = iFactor.getLowerAt(right);
                double[] uppers = iFactor.getUpperAt(right);

                String var = getVariable(prior, offset);
                addBounds(var, lowers[left], uppers[left]);
            }

        } else if (factor instanceof ExtensiveLinearFactor) {
            ExtensiveLinearFactor lfactor = (ExtensiveLinearFactor) factor;
            Strides full = lfactor.getDomain();

            // register variables
            for (int i = 0; i < full.getCombinations(); ++i) {
                String var = getVariable(prior, i);
                addBounds(var, 0, 1);
            }

            // add constraints
            for (LinearConstraint constraint : lfactor.getLinearProblem().getConstraints()) {
                // include only names for non zero coefficients
                String[] items = IntStream.range(0, full.getCombinations())
                        .filter(offset -> constraint.getCoefficients().getEntry(offset) != 0)
                        .mapToObj(offset -> getVariable(prior, offset)).toArray(String[]::new);

                // remove zero coefficients
                double[] coeffs = Arrays.stream(constraint.getCoefficients().toArray()).filter(value -> value != 0)
                        .toArray();

                addContraint(items, coeffs, constraint.getRelationship(), constraint.getValue());
            }
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private void addContraint(String[] items, double[] coeffs, Relationship relationship, double value) {
        constraints.append("s.t. c").append(constraint_count++).append(": ");
        for (int i = 0; i < items.length; ++i) {
            if (i != 0) constraints.append(" + ");
            constraints.append(coeffs[i]).append(" * ").append(items[i]);
        }

        switch(relationship){
            case EQ:
                constraints.append(" = ");
                break;
            case GEQ:
                constraints.append(" >= ");
                break;
            case LEQ:
                constraints.append(" = ");
                break;
        }
        constraints.append(value);
    }

    /**
     * Add a variable with a bound to the mod var section
     * 
     * @param var   the unique name of the variable
     * @param lower the lower bound
     * @param upper the upper bound
     */
    protected void addBounds(String var, double lower, double upper) {
        variables.append("var ").append(var).append(" >= ").append(lower).append(" <= ").append(upper).append(";\n");
    }
    
    protected void addEquality(String new_name, String old_name) {
        constraints .append("s.t. c")
                    .append(constraint_count++)
                    .append(": ")
                    .append(new_name)
                    .append(" = ")
                    .append(old_name)
                    .append(";\n");
    }
    
    private void addSum(String name, String[] items) {
        constraints .append("s.t. c")
                    .append(constraint_count++)
                    .append(": ")
                    .append(name)
                    .append(" = ")
                    .append(String.join(" + ", items))
                    .append(";\n");
    }
    
    private void addProduct(String name, String[] items) {
        constraints .append("s.t. c")
                    .append(constraint_count++)
                    .append(": ")
                    .append(name)
                    .append(" = ")
                    .append(String.join(" * ", items))
                    .append(";\n");
    }
    
    private void addDivision(String name, String num_name, String den_name) {
        constraints .append("s.t. c")
                    .append(constraint_count++)
                    .append(": ")
                    .append(name)
                    .append(" = ")
                    .append(num_name)
                    .append(" / ")
                    .append(den_name)
                    .append(";\n");
    }

    @Override
    public String serialize(SymbolicFactor target, int state, boolean maximize) {
    
        LinkedList<SymbolicFactor> fifo = new LinkedList<>();
        fifo.addLast(target);
        while(!fifo.isEmpty()) {
            SymbolicFactor factor = fifo.pollFirst();
            fifo.addAll(Arrays.asList(factor.getSources()));
            dispatch(factor);
        }

        StringBuilder post = new StringBuilder();
        post.append(maximize ? "maximize goal: " : "minimize goal: ");
        post.append(getVariable(target, state)).append(";\n");

        post.append("option solver couenne;\n");
        post.append("solve;\n");
        post.append("display goal;\n");
        post.append("display {j in 1.._nvars} (_varname[j],_var[j]);\n");
        
        
        return  variables.toString() + "\n" + constraints.toString() + "\n" + post.toString();
    }
    
}
