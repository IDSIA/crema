.. highlight:: java


=======
Factors
=======

.. contents:: Table of Contents
    :local:

Crema supports different ways to represent the probability functions defined over the variables. A structure of different categorization
and abstraction around factors have been implemented. At the top of this all we have the concept of :code:`GenericFactor`.

The basic idea behind the whole class hierarchy is to have immutable implementation of different interface. As an example,
a ``VertexFactor`` is an interface for many different implementation such as ``VertexLogFactor`` and ``VertexFunctionFactor``.
Inference algorithm should always work with the factor interfaces, such as ``VertexFactor``. This let us hide the different
kind of implementation and their complexity: to perform an inference the algorithm do not care how a factor implementation
stores its data or if the data are generated by a function. This will grant Crema a high flexibility on multiple definitions
of a factor.


Generic Factors Interfaces
==========================

.. graphviz::

    digraph GenericFactors {
        gf [shape=box,label="(I) GenericFactor"];
        ff [shape=box,label="(I) FilterableFactor<F>"];
        of [shape=box,label="(I) OperableFactor<F>"];
        lf [shape=box,label="(I) LinearFactor"];
        ss  [shape=box,label="(I) SeparatelySpecified<F>"];

        evf [shape=box,label="(I) ExtensiveVertexFactor"];
        sf  [shape=box,label="(I) SymbolicFactor"];
        bf  [shape=box,label="(I) BayesianFactor"];
        vf  [shape=box,label="(I) VertexFactor"];
        slf [shape=box,label="(I) SeparateLinearFactor<F>"];
        elf [shape=box,label="(I) ExtensiveLinearFactor<F>"];

        if  [shape=box,label="(I) IntervalFactor"];
        shf [shape=box,label="(I) SeparateHalfspaceFactor"];

        if  -> slf [color=green];
        shf -> slf [color=green];
        ss  -> ff  [color=green];

        of  -> ff   [color=green];
        ff  -> gf   [color=green];
        lf  -> gf   [color=green];

        evf -> of   [color=green];
        sf  -> of   [color=green];

        bf  -> of   [color=green];
        bf  -> ss   [color=green];

        vf  -> of   [color=green];
        vf  -> ss   [color=green];

        slf -> ss   [color=green];
        slf -> lf   [color=green];

        elf -> lf   [color=green];
    }

The image above shows the main class hierarchy for the factors in Crema. The simplest definition of a factor is represented
by the ``GenericFactor`` interface. This interface defines the two most basic methods of any factor: the ``copy()`` and
the ``getDomain()`` methods.

In Crema we have two main different kind of factors: ``FilterableFactor<F>`` and ``LinearFactor``. The first is a group of
factors that are able to perform the ``filter(int, int)`` operation on itself, while the second represents the group of
factors defined with a linear problem.

.. Note::
    Note that these two groups are not separated: as an example, the ``SeparateLinearFactor`` is a particular type of
    factor that is defined with a linear problem but can also perform the filter operation.

Two other important groups are below the ``FilterableFactor<F>``: the ``OperableFactor<F>`` and ``SeparatelySpecified<F>``
factors. The first defines the capability to perform operations such as ``combine(factor)``, ``marginalize(int...)``,
``divide(factor)``, and ``normalize(int...)``. These are all operation used by particular inference algorithm, in particular
Bayesian-base algorithms. The second group, instead, defines the factors that have particular operations over their domains.

Below these main interfaces, we find the implementation of all the types of factors.


Credal Factors
--------------

.. graphviz::

    digraph CredalFactors {
        lf  [shape=box,label="(I) LinearFactor"];
        ss  [shape=box,label="(I) SeparatelySpecified<F>"];

        elf  [shape=box,label="(I) ExtensiveLinearFactor<F>"];
        ehf  [shape=box,label="ExtensiveHalfspaceFactor"];

        slf  [shape=box,label="(I) SeparateLinearFactor<F>"];

        if   [shape=box,label="(I) IntervalFactor"];
        iaf  [shape=box,label="(A) IntervalAbstractFactor"];
        idf  [shape=box,label="IntervalDefaultFactor"];
        ilf  [shape=box,label="IntervalLogFactor"];
        ivf  [shape=box,label="IntervalVacuousFactor"];

        shf  [shape=box,label="(I) SeparateHalfspaceFactor"];
        shaf [shape=box,label="(A) SeparateHalfspaceAbstractFactor"];
        shdf [shape=box,label="SeparateHalfspaceDefaultFactor"];

        vf   [shape=box,label="(I) VertexFactor"];
        vaf  [shape=box,label="VertexAbstractFactor"];
        vdf  [shape=box,label="VertexDefaultFactor"];
        vff  [shape=box,label="VertexFunctionFactor"];
        vlf  [shape=box,label="VertexLogFactor"];
        vtf  [shape=box,label="VertexDeterministicFactor"];

        sscf [shape=box,label="SeparatelySpecifiedCredalFactor<S>"];
        cf   [shape=box,label="(A) ConditionalFactor<F>"];

        evf  [shape=box,label="(I) ExtensiveVertexFactor"];
        evaf [shape=box,label="(A) ExtensiveVertexAbstractFactor"];
        evdf [shape=box,label="ExtensiveVertexDefaultFactor"];
        evlf [shape=box,label="ExtensiveVertexLogFactor"];

        slf -> lf  [color=green];
        slf -> ss  [color=green];
        vf  -> ss  [color=green];

        if  -> slf [color=green];
        shf -> slf [color=green];

        elf -> lf  [color=green];
        ehf -> elf [style=dashed,color=green];

        iaf -> if  [style=dashed,color=green];
        idf -> iaf;
        ivf -> idf;
        ilf -> idf;

        shaf -> shf [style=dashed,color=green];
        shdf -> shaf;

        vlf -> vdf;
        vtf -> vdf;
        vdf -> vaf;
        vff -> vaf;
        vaf -> vf   [style=dashed,color=green];

        sscf -> ss  [style=dashed,color=green];
        cf   -> ss  [style=dashed,color=green];

        evaf -> evf [style=dashed,color=green];
        evdf -> evaf;
        evlf -> evdf;
    }

The credal factors are the main factors that works with Crema. The idea of this library is to offer inferences algorithm
to perform computation over these kind of factors. There we can find the most used factors, such as ``VertexFactor`` and
``IntervalFactor`` that are used to specify imprecise probability factors.


Bayesian Factors
----------------

.. graphviz::

   digraph BayesianFactors {
        bf  [shape=box,label="(I) BayesianFactor"];
        baf [shape=box,label="(A) BayesianAbstractFactor"];
        bff [shape=box,label="(A) BayesianFunctionFactor"];
        bdf [shape=box,label="BayesianDefaultFactor"];

        blf [shape=box,label="BayesianLogFactor"];
        btf [shape=box,label="BayesianDeterministicFactor"];

        bflf [shape=box,label="(A) BayesianFunctionLogFactor"];
        bgf [shape=box,label="BayesianLogicFactor"];
        bnf [shape=box,label="BayesianNotFactor"];

        nor [shape=box,label="BayesianNoisyOrFactor"];
        and [shape=box,label="BayesianAndFactor"];
        or  [shape=box,label="BayesianOrFactor"];

        nor -> bgf;
        and -> bgf;
        or ->  bgf;

        bflf -> bff;
        bgf -> bff;
        bnf -> bff;

        blf -> bdf;
        btf -> bdf;

        bdf -> baf;
        bff -> baf;

        baf -> bf [style=dashed,color=green];
   }

``Bayesian Factors`` are just a single type of factors that have an huge potential in Crema. These factors implements all
the algebra to perform Bayesian inference over ``BayesianNetworks`` and other particular kind of models.

These factors also contains a special version of the bayesian factor: the ``logic`` factors. These factors implement a
logic binary operation, such as ``and``, ``or``, or ``not``, that can be used to implement logics in a Bayesian network.


Symbolic Factors
----------------

.. graphviz::

   digraph SymbolicFactors {
        sf  [shape=box,label="(I) SymbolicFactor"];
        saf [shape=box,label="(A) SymbolicAbstractFactor"];
        cf  [shape=box,label="CombinedFactor"];
        df  [shape=box,label="DividedFactor"];
        mf  [shape=box,label="MarginalizedFactor"];
        ff  [shape=box,label="FilteredFactor"];
        pf  [shape=box,label="PriorFactor"];
        nf  [shape=box,label="NormalizedFactor"];

        cf -> saf;
        df -> saf;
        mf -> saf;
        ff -> saf;
        pf -> saf;
        nf -> saf;

        saf -> sf [style=dashed,color=green];
   }

A ``SymbolicFactor`` is a special factor that does not perform any kind of operation. The use of these factors is to
build a diagram of the operations so that it is possible to visualize the operations done and at the same time optimize
and reuse them by changing the input factors.

.. Note::
    The ``PriorFactor`` is a special factor that can wrap any kind of ``GenericFactor``. These are the inputs node of a
    workflow diagram produced by any inference algorithm that run over a ``DAGModel`` of ``SymbolicFactors``.


Implementation
==============

As stated before, the main idea is to let the algorithms work with a common interface that defines a factor while the
implementation, in other words how the data are stored and managed for each type of factor, is hidden. For this reason,
we have multiple implementations available for each factor interface.

.. Note::
    Since most of the implementation have a common set of fields and methods, the majority of these interfaces are first
    implemented in abstract classes. Then all the definition of factor extends this abstract class.

Across multiple hierarchies, we have some common way of implement a factor. As an example we can have ``FunctionFactors``.
These factors does not store the data in them but have a function (often a **lambda** function) that *generates* the
requested data on the fly. One interesting implementation of this mechanics is available in the ``BayesianLogicFactor``
and the classes that extends this one. These logic factors implements a logic function and does not have any kind of storage,
making them faster and more efficient at runtime.

Another common implementation pattern is to differentiate between factors in **log-space** and not. All the factors that
are called like ``*DefaultFactor`` are the most simple implementation of a factor in a normal space. The factors
that works and are optimizer for the **log-space**, instead, are called ``*LogFactor``. Most of the factor interfaces
offers two methods to access the values: one for log (as an example, ``BayesianFactor#getLogValue(int)``) and one for
normal space (following the example, ``BayesianFactor#getValue(int)``).


Factory
=======

Although all factors can be instantiated directly with the ``new`` keyord, many factor groups have a so called *factory*
class. This is an helper class that simplify the build of the factors with helper methods and functions. All factor
classes have the ``factory()`` static method that will instantiate the factory. All the methods of a factory can be
chained together in a fluent way.

To obtain a factor once the factory setup is complete, just call one of the builder methods like ``get()`` or ``log()``.

.. note::

   Check the latest version of the `JavaDoc <https://idsia.github.io/crema/javadoc/>`_ to find more on this argument.


Conversion
==========

In the package ``ch.idsia.crema.factor.convert`` we collected a conversion classes that can be used to convert one factor
to another. These converter classes does not cover all the possible and doable combination. In certain cases, to perform
a conversion, multiple converter need to be used.
