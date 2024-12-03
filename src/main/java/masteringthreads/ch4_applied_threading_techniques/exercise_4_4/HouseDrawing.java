package masteringthreads.ch4_applied_threading_techniques.exercise_4_4;

public class HouseDrawing extends StupidFramework {
    private static final ThreadLocal<String> tempColour = new ThreadLocal<>();
    private final String colour;

    public HouseDrawing(String title, String colour) {
        tempColour.set(colour); // Java 22-preview
        super(title);
        this.colour = colour;
        tempColour.remove();
    }

    public void draw() {
        System.out.println("Drawing house with colour " + getColour());
    }

    private String getColour() {
        if (colour == null) return tempColour.get();
        return colour;
    }
}
