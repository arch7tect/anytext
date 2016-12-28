package ru.neoflex.anytext;

/**
 * Created by orlov on 22.12.2016.
 */
public class CharSequenceView implements CharSequence {
    CharSequence data;
    int start;
    int end;

    public CharSequenceView(CharSequence data) {
        this(data, 0, data.length());
    }

    public CharSequenceView(CharSequence data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
    }

    public int length() {
        return end - start;
    }

    public char charAt(int index) {
        return data.charAt(start + index);
    }

    public CharSequence subSequence(int start, int end) {
        return new CharSequenceView(data, this.start + start, this.start + end);
    }

    public String toString() {
        return new StringBuilder(end - start).append(data, start, end).toString();
    }
}
