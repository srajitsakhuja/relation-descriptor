/** Statistical Natural Language Processing System
    Copyright (C) 2014-2016  Lu, Wei

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.statnlp.commons.ml.opt;

import org.statnlp.commons.ml.opt.GradientDescentOptimizer.AdaptiveStrategy;

/**
 * The factory class to construct the respective gradient descent optimizer with specified parameters
 * @author Aldrian Obaja <aldrianobaja.m@gmail.com>
 *
 */
public class GradientDescentOptimizerFactory extends OptimizerFactory {
	
	private static final long serialVersionUID = -5188815585483903945L;
	private AdaptiveStrategy adaptiveStrategy;
	
	private double learningRate;
	private double learningRateDecay;
	
	private double adadeltaPhi;
	private double adadeltaEps;
	private double adadeltaGradDecay;
	
	private double rmsPropDecay;
	private double rmsPropEps;
	
	private double adamBeta1;
	private double adamBeta2;
	private double adamEps;
	
	private boolean gradientClipping;
	private double gradientClippingThreshold;
	
	GradientDescentOptimizerFactory(AdaptiveStrategy adaptiveStrategy, double learningRate) {
		this(adaptiveStrategy, learningRate, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false, 0.0);
	}
	
	GradientDescentOptimizerFactory(AdaptiveStrategy adaptiveStrategy, double learningRate, double gradientClippingThreshold) {
		this(adaptiveStrategy, learningRate, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, gradientClippingThreshold);
	}
	
	GradientDescentOptimizerFactory(AdaptiveStrategy adaptiveStrategy, double learningRate, double adadeltaPhi, double adadeltaEps) {
		this(adaptiveStrategy, learningRate, 1.0, adadeltaPhi, adadeltaEps, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false, 0.0);
	}

	GradientDescentOptimizerFactory(AdaptiveStrategy adaptiveStrategy, double learningRate, double adadeltaPhi, double adadeltaEps, double adadeltaDecay) {
		this(adaptiveStrategy, learningRate, 1.0, adadeltaPhi, adadeltaEps, adadeltaDecay, 0.0, 0.0, 0.0, 0.0, 0.0, false, 0.0);
	}

	GradientDescentOptimizerFactory(AdaptiveStrategy adaptiveStrategy, double learningRate, double learningRateDecay, double adadeltaPhi, double adadeltaEps, double adadeltaGradDecay, double rmsPropDecay, double rmsPropEps, double adamBeta1, double adamBeta2, double adamEps, boolean gradientClipping, double gradientClippingThreshold) {
		this.adaptiveStrategy = adaptiveStrategy;
		this.learningRate = learningRate;
		this.learningRateDecay = learningRateDecay;
		this.adadeltaPhi = adadeltaPhi;
		this.adadeltaEps = adadeltaEps;
		this.adadeltaGradDecay = adadeltaGradDecay;
		this.rmsPropDecay = rmsPropDecay;
		this.rmsPropEps = rmsPropEps;
		this.adamBeta1 = adamBeta1;
		this.adamBeta2 = adamBeta2;
		this.adamEps = adamEps;
		this.gradientClipping = gradientClipping;
		this.gradientClippingThreshold = gradientClippingThreshold;
	}

	@Override
	public GradientDescentOptimizer create(int numWeights) {
		return new GradientDescentOptimizer(adaptiveStrategy, learningRate, learningRateDecay, adadeltaPhi, adadeltaEps, adadeltaGradDecay, rmsPropDecay, rmsPropEps, adamBeta1, adamBeta2, adamEps, numWeights, gradientClipping, gradientClippingThreshold);
	}

}
