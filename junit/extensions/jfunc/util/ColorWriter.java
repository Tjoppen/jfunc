package junit.extensions.jfunc.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.io.IOException;

public class ColorWriter extends PrintWriter {

    /** 
     * Other attributes
     **/

    public static final int shift = 8; // byte size
    public static final int REGULAR = 0 << shift;
    public static final int LIGHT = 1 << shift;
    public static final int BRIGHT = LIGHT;
    public static final int BOLD = LIGHT;
    public static final int UNDERSCORE = 4 << shift;
    /** Use with caution if you are epileptic **/
    public static final int BLINK = 5 << shift;
    public static final int INVERSE = 7 << shift;
    public static final int CONCEALED = 8 << shift;
    public static final int NO_RESET = 9 << shift; // won't reset attributes
    
    private static final char ESCAPE = '\u001b';

    public static final int BLACK = 30;
    public static final int DARK_GRAY = LIGHT | BLACK;
    public static final int DARK_GREY = DARK_GRAY;
    public static final int RED = 31;
    public static final int GREEN = 32;
    public static final int YELLOW = 33;
    public static final int BROWN = YELLOW; // I don't know why
    public static final int BLUE = 34;
    public static final int PURPLE = 35;
    public static final int CYAN = 36;
    public static final int LIGHT_GRAY = 37;
    public static final int LIGHT_GREY = LIGHT_GRAY;
    public static final int WHITE = LIGHT | LIGHT_GRAY;
    //public static final int DEFAULT = LIGHT_GREY;
    public static final int DEFAULT = 0;
    /**
     * BACKGROUND needs to be added to the color, not or'd (|)
     **/
    public static final int BACKGROUND = 10;

    private boolean autoflush;
    private boolean useColor = true;

    public ColorWriter(Writer w, boolean autoflush) {
        super(w, autoflush);
        this.autoflush = autoflush;
    }

    public ColorWriter(Writer w) {
        this(w, true);
    }

    public ColorWriter(OutputStream w, boolean autoflush) {
        super(w, autoflush);
        this.autoflush = autoflush;
    }

    public ColorWriter(OutputStream w) {
        this(w, true);
    }

    public void enableColor(boolean yes) {
        useColor = yes;
    }

    public void setColor(int color) {
        if (!useColor) {
            return;
        }
        int attr = color >> shift;
        int clr = color & 0xff;
        print(ESCAPE + "[" + attr + ";" + clr + "m");
        if (autoflush)
            flush();
    }

    /**
     * Set color on any Writer
     **/
    public static void setColor(Writer writer, int color) 
        throws IOException {
        int attr = color >> shift;
        int clr = color & 0xff;
        writer.write(ESCAPE + "[" + attr + ";" + clr + "m");
    }

    public void setColor(int fg, int bg) {
        setColor(fg | NO_RESET);
        setColor((bg | NO_RESET) + BACKGROUND);
    }

    public static void main(String[] args) throws Exception {
        ColorWriter out = new ColorWriter(System.out);
        out.setColor(ColorWriter.BLACK | ColorWriter.BOLD, 
                     ColorWriter.BLUE | ColorWriter.BOLD);
        out.println("hi");
        out.setColor(ColorWriter.LIGHT_GRAY);
    }
}
