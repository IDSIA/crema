package ch.idsia.crema.factor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 16:56
 * <p>
 * This annotation defines that the data is specified in the log-space.
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogSpace {

}
