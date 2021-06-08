# NC-SubclassNeurons
## Introduction
Neural networks are commonly used for classifying a number of inputs into specific categories such as dividing colours into "light" and "dark". However, in this paper we will work with an ANN which creates subclasses for each individual class. In our case, categorizing images of dogs into its different species could be of considerable interest. Naturally, compared to the "light"/"dark" colour classification there will be a relatively high number of classes.

Therefore, instead of using an usual classifier setup of having output neurons that each represent one category, an ANN with several neurons per class will be used. With said ANN, this paper will explore the idea of whether expanding the subclasses with more neurons will give more precise results in comparison to an ANN with only one neuron per subclass.

The main idea behind this approach is not only to obtain better and more accurate results but also to see whether the neural net will use the added neurons to form subclasses within the already existing subclasses.

The ANN's training will start with one neuron per subclass. There are different techniques for the process of adding neurons to a subclass, for example having the numbers of neurons that are added to the subclass depend on how high the error rate is. Meaning that we add one neuron if the error of the subclass is relatively "low" and expanding the subclass by more neurons if the error rate is high. However, we will take the approach of adding exactly one neuron to a subclass after each training. This means that after a training one neuron will be added to the subclass which has the highest error rate of all the subclasses. During this process the error rate of the whole net will be observed and once the error rate either stays the same or gets higher after several training runs the whole process will be aborted.

New targets for our model or code extensions are not always explained or described in this file. 

## Code
To get a quick overview how the code is structured, take a look at the [pseudocode](https://github.com/ManuelSperl/NC-SubclassNeurons/blob/main/Pseudocode_Algorithm.pdf). This is only our current idea and will be extendet by time.
