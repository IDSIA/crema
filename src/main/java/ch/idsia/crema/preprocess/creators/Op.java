package ch.idsia.crema.preprocess.creators;

@FunctionalInterface
public interface Op {
    double execute(double a, double b);
}
