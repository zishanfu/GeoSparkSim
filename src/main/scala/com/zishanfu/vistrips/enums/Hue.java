package com.zishanfu.vistrips.enums;

import java.awt.Color;

public enum Hue {

    Cyan(Color.cyan), Magenta(Color.magenta), Yellow(Color.yellow),
    Red(Color.red), Green(Color.green), Blue(Color.blue),
    Orange(Color.orange), Pink(Color.pink);
    private final Color color;

    private Hue(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
