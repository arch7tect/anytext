package ru.neoflex.anytext;

/**
 * Created by orlov on 13.12.2016.
 */
public interface Consumer {
    void consume(Node node, CharSequence rest);
}
