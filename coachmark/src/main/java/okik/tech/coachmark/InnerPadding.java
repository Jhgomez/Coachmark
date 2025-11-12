package okik.tech.coachmark;

/**
 * Lets you add padding to rounded corner surrounding, useful when applying
 * overlay effects to a rounded-corner surrounding area.
 */
public final class InnerPadding {
    private final float top;
    private final float bottom;
    private final float start;
    private final float end;

    public InnerPadding(float top, float bottom, float start, float end) {
        this.top = top;
        this.bottom = bottom;
        this.start = start;
        this.end = end;
    }

    public float getTop()   { return top; }
    public float getBottom(){ return bottom; }
    public float getStart() { return start; }
    public float getEnd()   { return end; }
}
