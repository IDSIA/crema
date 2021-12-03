0.2.2
===========

- Fixes and updates to `BayesianLogicFactors` serialization ( #92 #94 #95 #103 )
- Added new capabilities for `BayesianNetworkSampling` ( #96 )
- Added possibility to use lambda functions to `RandomUtil#getRandom()` ( #97 )
- Added multi query to `LoopyBeliefPropagation` ( #101 )
- Restored behavior of pre 1.7.0 for `Neighborhood#random()` method ( #100 )
- Fixed an issue with `BayesianEntropy `
- New documentation ( #102 )


0.2.1
===========

- New *logics* Bayesian factors, such as `Not`, `And`, `Or`, and `NoisyOr` ( #86 )
- New ApproxLP0 algorithm ( #83 )
- Renamed `approxlp` package to `approxpl1`  ( #84 )
- Added new `VertexToRandomBayesian` converter ( #84 )

Extra:
- Replaced CI/CD pipeline with GitHub actions ( #84 )
- Added CITATION.cff and CITATION.bib files

0.2.0
===========

- New Factor hierarchy ( #74 ). Major changes are:
  * All factors are now **interfaces**.
  * For each `factor` we can have multiple implementations.
  * `Factor`s are **immutable** now: the creation of a new factor is delegated to the `constructor` or to a dedicated `Factory` class.
  * `SymbolicFactor`s can now be used to keep track of operations done by algorithms.
- Added code for isipta21benchamrk ( #76 #77 )
- Removed old methods marked as `@deprecated`

0.1.7.a
===========

- Fixed an issue with sampling of `BayesianFactors` #75

0.1.7
===========

- Refactoring of `Inference` and `Preprocessing` hierarchies ( #66 )  
- Added `Loopy Belief Propagation` algorithm ( #63 #64 #65 #67 )
- Fixed #48, an issue with `Belief Propagation` caused by wrong management of messages.
- Fixed a minor issue in BeliefPropagation (introduced in 0.1.6.b)
- Added a parser for the `BIF` format ( #51 )
- Fixed #56, #62
- Minor change to `BayesianNetworkSampling` class
- Updated `Sampling`algorithms ( #67 )
- Merged #70 #71

0.1.6
===========

- fixed #44, #40.
- New class hierarchy for Models:
    * the main interface is `Model`,
    * the graphical models now implements the `GraphicalModel` interface,
    * basic _Directed Acyclic Graph_ is `DAGModel`,
    * the `BayesianNetwork` class is now a specialized case of `DAGModel`,
- Added *Join Tree* and *Belief Propagation* algorithms for BayesianFactor-based models. 
- Added algorithms for entropy of Bayesian and Credal networks. 
- General code cleanup (mainly code style).
- Removed main methods from the library and moved to separate unit test classes.
- Updated tests and removed old experiments.
- Removed examples relative to Adaptive project.
- Updated tutorials.

0.1.5
===========

- fixed #33, #32, #30.
- BayesianFactor.filter method accepts a TIntIntHashMap for specifying the states of the variables.
- EM abstract class.
- Operations for calculating the log-likelihood.
- Static method combineAll at BayesianFactor.
- Improved ObservationBuilder.
- CSV support #18.
- Bayes writer in UAI format #26.
- VCREDAL HCREDAL writers. 
- VE conditionalQuery.
- Builder of a CN from a set of precise BNs.



0.1.4
===========

- Common interface for inference algorithms.
- Solved bugs at renaming and sort parents.
- Bug at LP #23.
- Documentation available.



0.1.3
===========

- Parsers for: V-CREDAL, BAYES and evidence.
- observation builder #20.
- Inference with EM algorithm.
- Migration of causality code to [Credici](https://github.com/IDSIA/credici).


0.1.2
===========

- Parser for h-credal models in uai format.
- Polco and lpsolve made accessible from maven.
- Travis support
 

0.1.1
===========

First published version of the software with all the code developed so far.