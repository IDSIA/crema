Credal Inference
==============================

Crema provides exact and approximate inference algorithms over credal networks.
For the exact one, create an object of class ``CredalVariableElimination`` and
run the query. The result is an object of class VertexFactor.

.. literalinclude:: ../../examples/docs/inferEx1.java
   :language: java
   :lines: 39-42


Approximate inference can be done by means of linear programming. For this, create
the an object of class ``CredalApproxLP`` and then run the query. Note
that the output is an ``IntervalFactor``.

.. literalinclude:: ../../examples/docs/inferEx2.java
   :language: java
   :lines: 52-58
