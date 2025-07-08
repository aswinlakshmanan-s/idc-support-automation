package edu.northeastern;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.repository.zoo.ZooProvider;
import ai.djl.translate.TranslateException;
import ai.djl.training.util.ProgressBar;

import java.io.IOException;

public class SentenceEmbedder {

    private Predictor<String, float[]> predictor;

    public SentenceEmbedder() throws IOException, ModelException {
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                .optEngine("PyTorch")
                .optProgress(new ProgressBar())
                .build();

        ZooModel<String, float[]> model = criteria.loadModel();
        this.predictor = model.newPredictor();
    }

    public float[] embed(String text) throws TranslateException {
        return predictor.predict(text);
    }
}