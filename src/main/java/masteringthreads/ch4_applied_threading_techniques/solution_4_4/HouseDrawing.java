package masteringthreads.ch4_applied_threading_techniques.solution_4_4;

import masteringthreads.ch4_applied_threading_techniques.exercise_4_4.StupidFramework;

public class HouseDrawing extends StupidFramework {
    private final String colour;

    private static final ThreadLocal<String> colourInit = new ThreadLocal<>();

    public HouseDrawing(String title, String colour) {
        super(setThreadLocals(colour, title));
        this.colour = colour;
        colourInit.remove();
    }

    private static String setThreadLocals(String colour, String title) {
        colourInit.set(colour);
        return title;
    }

    private String getColour() {
        if (colour == null) {
            return colourInit.get();
        }
        return colour;
    }

    public void draw() {
        System.out.println("Drawing house with colour " + getColour());
    }
}
