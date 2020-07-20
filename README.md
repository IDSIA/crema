[![GitHub version](https://badge.fury.io/gh/idsia%2Fcrema.svg)](https://badge.fury.io/gh/idsia%2Fcrema)
[![Build Status](https://travis-ci.org/IDSIA/crema.svg?branch=master)](https://travis-ci.org/IDSIA/crema)


<img src="./docs/_static/img/logo.png" alt="Crema" width="500"/>

CreMA is a open-source java toolbox that provides multiple
learning and inference algorithms for credal models.

An example of exact inference in a credal network is given below.

```java
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.ve.CredalVariableElimination;
import ch.idsia.crema.model.ObservationBuilder;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;

public class Starting {
    public static void main(String[] args) {
        double p = 0.2;
        double eps = 0.0001;

        /*  CN defined with vertex Factor  */

        // Define the model (with vertex factors)
        SparseModel model = new SparseModel();
        int A = model.addVariable(3);
        int B = model.addVariable(2);
        model.addParent(B,A);

        // Define a credal set of the partent node
        VertexFactor fu = new VertexFactor(model.getDomain(A), Strides.empty());
        fu.addVertex(new double[]{0., 1-p, p});
        fu.addVertex(new double[]{1-p, 0., p});
        model.setFactor(A,fu);


        // Define the credal set of the child
        VertexFactor fx = new VertexFactor(model.getDomain(B), model.getDomain(A));

        fx.addVertex(new double[]{1., 0.,}, 0);
        fx.addVertex(new double[]{1., 0.,}, 1);
        fx.addVertex(new double[]{0., 1.,}, 2);

        model.setFactor(B,fx);

        // Run exact inference
        CredalVariableElimination inf = new CredalVariableElimination(model);
        inf.query(A, ObservationBuilder.observe(B,0));

    }
}



``` 

## Installation

Add the following code in the  pom.xml of your project:

```xml
    <repositories>
        <repository>
            <id>cremaRepo</id>
            <url>https://raw.github.com/idsia/crema/mvn-repo/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>ch.idsia</groupId>
            <artifactId>crema</artifactId>
            <version>0.1.4</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```