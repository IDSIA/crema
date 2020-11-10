0.1.5
===========

- fixed #33, #32, #30.
- BayesianFactor.filter method accepts a TIntIntHashMap for specifying the states of the variables.
- EM abstract class.
- Operations for calculating the log-likelihood.
- Static method combineAll at BayesianFactor.
- Improved ObservationBuilder
- CSV support #18
- Bayes writer in UAI format #26
- VCREDAL HCREDAL writers 
- VE conditionalQuery
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