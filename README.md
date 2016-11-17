# Neural-BoN
This code implements the Neural-BoN model proposed in AAAI-17 paper: [Bofang Li, Tao Liu, Zhe Zhao, Puwei Wang and Xiaoyong Du - **Neural Bag-of-Ngrams**].

## Code
This code has been tested on Windows, but it should work on Linux, OSX or any other operation system without any changes (Thanks to Java). 

All parameters your may need to change are in the top lines of src/main/Main.java.

To train Neural-BoN on an unlabeled corpus, you can just specify the corpus in the Main function of src/main/Main.java and run it. This will generate the learned n-gram representations in the results folder.

To train Neural-BoN on a labeled corpus, you can specify the corpus and implement an getXXXDataset function in src/myUtils/Dataset.java or change the getIMDBDataset function in src/myUtils/Dataset.java for the specific format.

## More Information

This code is implemented upon the [DV-ngram code] (https://github.com/libofang/DV-ngram) of our ICLR 2016 workshop paper [Bofang Li, Tao Liu, Xiaoyong Du, Deyuan Zhang and Zhe Zhao - **Learning Document Embedding by Predicting N-grams for Sentiment Classification of Long Movie Reviews**] (http://arxiv.org/abs/1512.08183). We highly recommend you to try that code for simpler generation of document vectors.

Again, we thank Grégoire Mesnil et al. for their implementation of Paragraph Vector. Both their code and [iclr 2015 workshop paper](http://arxiv.org/abs/1412.5335) have influenced us a lot.

