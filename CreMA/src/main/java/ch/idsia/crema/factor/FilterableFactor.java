package ch.idsia.crema.factor;

public interface FilterableFactor<F extends GenericFactor> extends GenericFactor {
  /**
   * <p>
   * Filter the factor by selecting only the values where the specified
   * variable is in the specified state.
   * </p>
   *
   * <p>
   * Can return this if the variable is not part of the domain of the factor.
   * </p>
   *
   * @param variable
   * @param state
   * @return
   */
  public F filter(int variable, int state);

}
