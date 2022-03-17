package com.alibaba.ocr.demo.model;

public class Rect {
    private Integer top;
    private Integer left;
    private Integer width;
    private Integer angle;
    private Integer height;

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getAngle() {
        return angle;
    }

    public void setAngle(Integer angle) {
        this.angle = angle;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Rect{" +
                "top=" + top +
                ", left=" + left +
                ", width=" + width +
                ", angle=" + angle +
                ", height=" + height +
                '}';
    }
}
