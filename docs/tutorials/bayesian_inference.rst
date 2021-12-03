==============================
Bayesian Inference
==============================

Crema provides useful algorithms for both precise and approximate inference on Bayesian networks and graphs.

.. contents:: Table of Contents
    :local:


Exact Inference
===============

Variable Elimination
~~~~~~~~~~~~~~~~~~~~

The ``VariableElimination`` inference algorithm uses a given *elimination sequence* in order to perform the inference.
Each elimination sequence depends on the structure of the model and the variable to query.

The implementation of this algorithm in Crema need an *algebra* to work. If this algebra is available externally, it is
possible to use the ``VariableElimination<F>`` implementation; while if the existing ``FactorAlgebra`` is enough for the
used factor, the wrapper class ``FactorVariableElimination<F>`` can be used.


Belief Propagation
~~~~~~~~~~~~~~~~~~

The ``BeliefPropagation`` inference algorithm works by analyzing the model and build a ``JunctionTree``. Then it will
use the *message passing* algorithm to performing the inference.

Each call to the ``query()`` method will build a new ``JunctionTree`` from zero.

To perform an inference on a variable, as an example if you want the marginal of ``P(A)``, use the ``query()`` method as
in the example below:

.. literalinclude:: ../../examples/BeliefPropagationExample.java
   :language: java
   :start-after: [p2]
   :end-before: [p3]
   :dedent: 2


If you want to use evidence, you need to create first a ``TIntIntHashMap`` that will include the state of the various
variables, in the belo case  we query for ``P(A | B=0)``:

.. literalinclude:: ../../examples/BeliefPropagationExample.java
   :language: java
   :start-after: [p3]
   :end-before: [p4]
   :dedent: 2


This algorithm offers other ways to perform an inference. It is possible to build such tree once and query multiple variables at the same time. First instantiate the inference
algorithm object. The inference engine will build an internal ``JunctionTree`` that will be used for the following queries.

.. literalinclude:: ../../examples/BeliefPropagationExample.java
   :language: java
   :start-after: [p4]
   :end-before: [p5]
   :dedent: 2


Then remember to call ``fullPropagation()`` to update the tree. This will
return the posterior of a variable considered the root of the internal ``JunctionTree``. This root variable is also the
query variable.

.. literalinclude:: ../../examples/BeliefPropagationExample.java
   :language: java
   :start-after: [p5]
   :end-before: [p6]
   :dedent: 2


.. Full example:
   .. literalinclude:: ../../examples/BeliefPropagationExample.java
      :language: java


Approximate Inference
=============================


Sampling
~~~~~~~~~~~~~~~~~~~~~~~~

Crema offers two implementation of ``StochasticSampling`` for ``BayesianFactor``: the ``LogicSampling`` and the
``LikelihoodWeightingSampling``. These sampling algorithms have different levels of precision based on the number of
iterations performed.


Loopy Belief Propagation
~~~~~~~~~~~~~~~~~~~~~~~~

This is an approximate version of the ``BeliefPropagation``: is uses the same *message passing* algorithm but without the
burden to build a *junction tree*. The performance, and quality, of the algorithm can be managed by the number of
iterations to execute.
