.. highlight:: java

=================
Inference Engines
=================

Crema offers the generic interface ``Inference<M, F>`` to perform an inference on a model using the
``query(model, evidence, variable)`` method.

.. note::
    If not specified otherwise, all the algorithms implementations are state-less; this means that each query are
    considered unique and there is no memory of the previous inference queries done with the same object.

The interface requires to specify two generic types: the input *model* type ``M``, and the output factor type ``F``.

Below an example on how to run an inference on a model built with ``BayesianFactors`` using the ``BeliefPropagation``
inference algorithm. This is the simplest way to run an inference.

.. literalinclude:: ../../examples/BeliefPropagationExample.java
   :language: java
   :start-after: [p1]
   :end-before: [p2]
   :dedent: 2

Note how the inference engine works on a model of type ``DAGModel<BayesianFactor>`` and that the output of the inference
is an object of type ``BayesianFactor``.

Evidence
========

In Crema, an evidence is just a map, an object of type ``TIntIntMap``. If no evidence is needed, then the ``Inference<M, F>``
interface offers an utility ``query(model, variable)`` method without the need to pass an empty map.


Multiple queries
================

Crema offers other kind of inference interfaces. These interfaces are intended to offer a more optimized way to perform
multiple and joined queries.

.. note::
    In te current version, there are no algorithms that support and implement these interfaces. If an algorithm offers a
    special way to perform these queries, it will be required to instantiate it as its own class instead of using the
    ``Inference<M, F>`` interface.
