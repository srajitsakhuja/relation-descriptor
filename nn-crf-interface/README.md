## StatNLP Feature Value Provider Interface

This document describe how to integrate the feature value provider into usual StatNLP implementations. In some of the implementation, you may want to use continuous feature value instead of binary feature value, or a neural network component such as Long short-term memory (LSTM) to improve the model performance. We will go through the procedure step by step based on previous StatNLP implementations.

In general, we have two main feature value provider:
1. [Neural network feature value provider](#neural-network-feature-value-provider)
2. [Continuous feature value provider](#continuous-feature-value-provider)

### Table of Contents
- [Requirements](#requirements)
	- [Implementation on the Feature Value Provider](#implementation-on-the-feature-value-provider)
		- [Neural Network Feature Value Provider](#neural-network-feature-value-provider)
			- [BiLSTM Class](#bilstm-class)
			- [Implement BiLSTM in Torch](#implement-bilstm-in-torch)
			- [Specify the input and output of the BiLSTM through `FeatureManager`.](#specify-the-input-and-output-of-the-bilstm-through-featuremanager)
		- [Continuous Feature Value Provider](#continuous-feature-value-provider)

### Requirements
* Torch 7 (installed with Lua 5.2)
* [Element-Research/rnn](https://github.com/Element-Research/rnn)
* JNLua (under `install_jnlua` folder)

### Implementation on the Feature Value Provider

#### Neural Network Feature Value Provider
We describe the procedure with Bi-direction LSTM on a sequence labeling task (_i.e._ named entity recognition) as an example. The general steps are as follow:
1. Create a BiLSTM class which extends the `NeuralNetworkFeatureValueProvider`.
2. Implement the BiLSTM in Torch.
3. Specify the input and output of the BiLSTM through `FeatureManager`.

The code for this example can be found in `org.statnlp.example.linear_ne`.

##### BiLSTM Class
It requires you to put some configurations, such as hidden size, and override three methods in the class.
```java
public class BiLSTM extends NeuralNetworkFeatureValueProvider {

	public BiLSTM(int numLabels) {
		super(numLabels);
		config.put("class", "BiLSTM");
        config.put("hiddenSize", 100);
        config.put("embedding", "glove");
	}

	@Override
	public int getNNInputSize() {
		// TODO: count number of sentences and get max sentence length
		// using fvpInput2id: Map<BiLSTMInputObject, ID>.
		// {@link #fvpInput2id} is created during feature extraction.
		return numSents * maxSentLen;
	}

	@Override
	public int edgeInput2Index(Object edgeInput) {
		// TODO: get the row index of the output matrix for each hyperedge input
		// In BiLSTM, the hyperedge input here is represented by the sentence and the position.
		// In Torch RNN, output is a matrix with size (maxSentLength * numSents) * numLabels
		int row = position * numSents + sentenceID.
		return row;
	}

	@Override
	public Object edgeInput2FVPInput(Object edgeInput) {
		// TODO: convert the hyperedge input to the BiLSTM input
		// Recall the edge input is "sentence + position"
		// we need to retrive the BiLSTM through this function
		return edgeInput.sent;
	}

}
```

##### Implement BiLSTM in Torch
Basically, implment a `BiLSTM` class as usual Torch implementation and extend `AbstractNeuralNetwork` class.
```lua
---At the head of the file
local BiLSTM, parent = torch.class('BiLSTM', 'AbstractNeuralNetwork')
```
See the example under `neural_server` folder: `BidirectionalLSTM.lua`. Add some code to the `NetworkInterface.lua`:
```lua
include 'nn-crf-interface/neural_server/BidirectionalLSTM.lua'
...
function initialize(javadata, ...)
	...
	if networkClass == "BiLSTM" then
    	net = BiLSTM(optimizeInTorch, gpuid)
    ...
end
...
```


##### Specify the input and output of the BiLSTM through `FeatureManager`.
1. First, construct the `GlobalNetworkParam` using our BiLSTM.

```java
fvpList.add(new BiLSTM(numLabel))
GlobalNetworkParam gnp = new GlobalNetworkParam(optimizer, fvpList);
```
2. Extract the input-output pair in `FeatureManager`

```java
biLSTM = param_g.getFeatureValueProviders().get(0); // In the constructor function
...
edgeInput = new SimpleImmutableEntry<String, Integer>(sentence, position);
net.addHyperEdge(network, parent_k, children_k_index, edgeInput, outputLabel);
```

#### Continuous Feature Value Provider
Just need to create your own continuous feature value provider class which extends the abstract `ContinuousFeatureValueProvider` class.

```java
public class ECRFContinuousFeatureValueProvider extends ContinuousFeatureValueProvider {

	public ECRFContinuousFeatureValueProvider(int numFeatureValues, int numLabels) {
		super(numFeatureValues, numLabels);
	}

	@Override
	public void getFeatureValue(Object input, double[] featureValue) {
		// TODO: fill in the feature values of the input.
	}

}
```
