package com.alibaba.ocr.demo.model;

public class RectVal {
    private Rect rect;
    private String word;

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return "RectVal{" +
                "rect=" + rect +
                ", word='" + word + '\'' +
                '}';
    }
}
