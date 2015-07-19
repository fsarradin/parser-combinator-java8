package me.parsing;

public class CharReader {

    public static final char EOF_CHAR = '\u001a';

    public final CharSequence source;
    public final int offset;

    public CharReader(CharSequence source) {
        this(source, 0);
    }

    public CharReader(CharSequence source, int offset) {
        this.source = source;
        this.offset = offset;
    }

    public char first() {
        if (offset < source.length()) return source.charAt(offset);
        else return EOF_CHAR;
    }

    public CharReader rest() {
        if (offset < source.length()) return new CharReader(source, offset + 1);
        else return this;
    }

    public boolean atEnd() {
        return offset >= source.length();
    }

    public CharReader drop(int n) {
        return new CharReader(source, offset + n);
    }

    public boolean sizeIsGreaterOrEqualTo(int size) {
        return source.length() - offset >= size;
    }

    public CharSequence peek(int size) {
        int length = offset + size;
        if (length > source.length())
            return source.subSequence(offset, source.length()).toString() + EOF_CHAR;
        return source.subSequence(offset, length);
    }

    @Override
    public String toString() {
        return "CharReader(offset: " + offset + ", source: \"" + source + "\")";
    }
}
