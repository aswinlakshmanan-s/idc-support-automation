package edu.northeastern;

public class Document {
    public String id;
    public String text;
    public float[] vector;

    public Document(String id, String text) {
        this.id = id;
        this.text = text;
    }
}