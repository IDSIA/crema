package ch.idsia.crema.utility;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.DoubleStream;

public class ConstraintsUtil {

    public static Collection<LinearConstraint> perturbZeroConstraints(Collection<LinearConstraint> set, double eps) {
        Collection<LinearConstraint> newSet = new ArrayList();
        for (LinearConstraint c : set) {
            if (c.getValue() == 0 && eps > 0) {
                if(c.getRelationship() == Relationship.EQ){
                    LinearConstraint c1 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.GEQ, 0.0);
                    LinearConstraint c2 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.LEQ, eps);
                    newSet.add(c1);
                    newSet.add(c2);
                }else if(c.getRelationship() == Relationship.GEQ){
                    LinearConstraint c1 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.GEQ, eps);
                    newSet.add(c1);
                }else{
                    LinearConstraint c1 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.LEQ, 0 - eps);
                    newSet.add(c1);
            }

        }else{
                newSet.add(c);
            }
        }
        return newSet;
    }

    public static Collection<LinearConstraint> removeNormalization(Collection<LinearConstraint> set) {
        Collection<LinearConstraint> newSet = new ArrayList();
        for (LinearConstraint c : set) {
            if (!isNormalization(c)) {
                newSet.add(c);
            }
        }
        return newSet;

    }

    public static Collection<LinearConstraint> removeNonNegative(Collection<LinearConstraint> set) {
        Collection<LinearConstraint> newSet = new ArrayList();
        for (LinearConstraint c : set) {
            if (!isNonNegative(c)) {
                newSet.add(c);
            }
        }
        return newSet;

    }

    public static boolean isNormalization(LinearConstraint c){
        return (c.getRelationship() == Relationship.EQ && c.getValue() == 1
                && DoubleStream.of(c.getCoefficients().toArray()).allMatch(x -> x == 1)) ||
                (c.getRelationship() == Relationship.EQ && c.getValue() == -1
                        && DoubleStream.of(c.getCoefficients().toArray()).allMatch(x -> x == -1));
    }

    public static boolean isNonNegative(LinearConstraint c) {
        return (c.getRelationship() == Relationship.GEQ && c.getValue() == 0 &&
                ArraysUtil.isOneHot(c.getCoefficients().toArray())) ||
                (c.getRelationship() == Relationship.LEQ && c.getValue() == -0 &&
                        ArraysUtil.isOneHot(c.getCoefficients().mapMultiply(-1).toArray()));
    }




        public static Collection<LinearConstraint> changeEQtoLEQ(Collection<LinearConstraint> set) {
        Collection<LinearConstraint> newSet = new ArrayList();

        for (LinearConstraint c : set) {
            if(c.getRelationship() == Relationship.EQ) {
                LinearConstraint c1 = new LinearConstraint(c.getCoefficients(), Relationship.LEQ, c.getValue());
                LinearConstraint c2 = new LinearConstraint(c.getCoefficients().mapMultiply(-1), Relationship.LEQ, -1*c.getValue());
                newSet.add(c1);
                newSet.add(c2);
            } else{
                newSet.add(c);
            }
        }

        return newSet;
    }

    public static Collection<LinearConstraint> changeGEQtoLEQ(Collection<LinearConstraint> set) {
        Collection<LinearConstraint> newSet = new ArrayList();
        for (LinearConstraint c : set) {
            if(c.getRelationship() == Relationship.GEQ) {
                LinearConstraint c1 = new LinearConstraint(c.getCoefficients().mapMultiply(-1), Relationship.LEQ, -1*c.getValue());
                newSet.add(c1);
            } else{
                newSet.add(c);
            }
        }

        return newSet;
    }


    public static void printConstraints(Collection<LinearConstraint> set) {
        Iterator it = set.iterator();
        while (it.hasNext()) {
            LinearConstraint c = (LinearConstraint) it.next();
            System.out.println(c.getCoefficients() + "\t" + c.getRelationship() + "\t" + c.getValue());
        }
    }

}
