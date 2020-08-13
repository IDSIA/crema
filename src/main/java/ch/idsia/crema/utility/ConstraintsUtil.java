package ch.idsia.crema.utility;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class ConstraintsUtil {

    public static Collection<LinearConstraint> perturbZeroConstraints(Collection<LinearConstraint> set, double eps) {
        Collection<LinearConstraint> newSet = new ArrayList();
        for (LinearConstraint c : set) {
            if (c.getValue() == 0 && eps > 0) {
                if(c.getRelationship() == Relationship.EQ) {
                    LinearConstraint c1 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.GEQ, 0.0);
                    LinearConstraint c2 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.LEQ, eps);
                    newSet.add(c1);
                    newSet.add(c2);
                    //    }else if(c.getRelationship() == Relationship.GEQ){
                }else{
                    LinearConstraint c1 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.GEQ, eps);
                    newSet.add(c1);
            //    }else{
            //        LinearConstraint c1 = new LinearConstraint(c.getCoefficients().toArray(), Relationship.LEQ, 0 - eps);
            //        newSet.add(c1);
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


    public static void print(Collection<LinearConstraint> set) {
        print(set.toArray(LinearConstraint[]::new));
    }

    public static void print(LinearConstraint... set){
        for(LinearConstraint c : set)
            System.out.println(c.getCoefficients() + "\t" + c.getRelationship() + "\t" + c.getValue());
    }


    public static LinearConstraint expandCoeff(LinearConstraint c, int size, int offset) {

        double[] newCoeff = new double[size];
        double[] oldCoeff = c.getCoefficients().toArray();

        System.arraycopy(oldCoeff, 0, newCoeff, offset, oldCoeff.length);

        return new LinearConstraint(newCoeff, c.getRelationship(), c.getValue());
    }

    public static Collection<LinearConstraint> expandCoeff(Collection<LinearConstraint> set, int size, int offset){
        Collection<LinearConstraint> newSet = new ArrayList();
        for (LinearConstraint c : set) {
            newSet.add(expandCoeff(c, size, offset));
        }
        return newSet;
    }


    public static Collection<LinearConstraint> getCompatible(LinearConstraint k, Collection<LinearConstraint> set) {
        Predicate<LinearConstraint> cond = c ->
                (c.getValue() == k.getValue() && c.getCoefficients().equals(k.getCoefficients())) ||
                        (c.getValue() == -1*k.getValue() && c.getCoefficients().getDistance(k.getCoefficients().mapMultiply(-1)) == 0);

        return set.stream().filter(cond).collect(Collectors.toList());

    }

    public static boolean areCompatible(Collection<LinearConstraint> set) {

        LinearConstraint k = (LinearConstraint) set.toArray()[0];

        Predicate<LinearConstraint> cond = c ->
                (c.getValue() == k.getValue() && c.getCoefficients().equals(k.getCoefficients())) ||
                        (c.getValue() == -1*k.getValue() && c.getCoefficients().getDistance(k.getCoefficients().mapMultiply(-1)) == 0);

        return set.size() == set.stream().filter(cond).collect(Collectors.toList()).size();

    }


    public static LinearConstraint merge(Collection<LinearConstraint> set){

        if(!areCompatible(set))
            throw new IllegalArgumentException("Non-compatible constraints");

        LinearConstraint k0 = (LinearConstraint) set.toArray()[0];


        Set<Relationship> rel = set.stream()
                .map(c -> {
                    if(c.getValue() == k0.getValue())
                        return c.getRelationship();
                    else
                        return c.getRelationship().oppositeRelationship();
                })
                .filter(x -> x!= Relationship.EQ)
                .distinct().collect(Collectors.toSet());

        Relationship r;

        if(rel.isEmpty() || (rel.contains(Relationship.GEQ) && rel.contains(Relationship.LEQ)))
            r = Relationship.EQ;
        else
            r = (Relationship) rel.toArray()[0];
        return new LinearConstraint(k0.getCoefficients(), r, k0.getValue());
    }


    public LinearConstraint inverse(LinearConstraint c){
        double[] coeff = DoubleStream.of(c.getCoefficients().toArray())
                .map(x -> {
                    if(x!=0.0) return -1*x;
                    else return 0.0;
                }).toArray();
        return  new LinearConstraint(coeff, Relationship.GEQ.oppositeRelationship(), c.getValue()*-1);
    }

    public static Collection<LinearConstraint> mergeCompatible(Collection<LinearConstraint> set){
        Collection<LinearConstraint> set_ = (Collection) set.stream().collect(Collectors.toList());
        Collection<LinearConstraint> out = new ArrayList<>();

        while(!set_.isEmpty()){
            LinearConstraint c = (LinearConstraint) set_.toArray()[0];
            Collection<LinearConstraint> K = getCompatible(c, set_);
            out.add(merge(K));
            set_.removeAll(K);
        }
        return out;
    }


}
