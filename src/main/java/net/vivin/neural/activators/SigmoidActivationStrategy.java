package net.vivin.neural.activators;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: vivin
 * Date: 11/5/11
 * Time: 6:45 PM
 */
public class SigmoidActivationStrategy implements ActivationStrategy, Serializable {
    public double activate(double weightedSum) {
        return 1.0 / (1 + Math.exp(-1.0 * weightedSum));
    }

    /**
     * Calculated the derivative of the functikon in point 'weightedSum'
     * @param weightedSum
     * @return // this is possibly wrong: http://www.ai.mit.edu/courses/6.892/lecture8-html/sld015.htm
     */
    public double derivative(double weightedSum) {
        return weightedSum * (1.0 - weightedSum);
    }

    public SigmoidActivationStrategy copy() {
        return new SigmoidActivationStrategy();
    }
}
