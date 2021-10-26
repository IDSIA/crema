==============================
Bayesian Inference
==============================

Crema provides useful algorithm for precise and approximate inference on Bayesian networks.

.. contents:: Table of Contents
    :local:


Exact Inference
=============================

Belief Propagation
~~~~~~~~~~~~~~~~~~~~~~~~

The ``BeliefPropagation`` inference algorithm works on the ``BayesianFactors`` of a ``BayesainNetwork``.

First instantiate the inference algorithm object using the model. The inference engine will build an internal
``JunctionTree`` that will be used for the following queries. Then remember to call ``fullPropagation()`` to update
the model. This will return the posterior of a variable considered the root of the internal ``JunctionTree``.

.. literalinclude:: ../../examples/BeliefPropagation.java
   :language: java
   :lines: 42-44
   :dedent: 2


To perform an inference on a variable, as an example if you want the marginal of ``P(A)``, use the ``query()`` method as
in the example below:

.. literalinclude:: ../../examples/BeliefPropagation.java
   :language: java
   :lines: 55-60
   :dedent: 2


If you want to use evidence, you need to create first a ``TIntIntHashMap`` that will include the state of the various
variables, in the belo case  we query for ``P(A | B=0)``:

.. literalinclude:: ../../examples/BeliefPropagation.java
   :language: java
   :lines: 62-69
   :dedent: 2


Full example:

.. literalinclude:: ../../examples/BeliefPropagation.java
   :language: java


Approximate Inference
=============================


Sampling
~~~~~~~~~~~~~~~~~~~~~~~~


Loopy Belief Propagation
~~~~~~~~~~~~~~~~~~~~~~~~

