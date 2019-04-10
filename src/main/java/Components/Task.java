package Components;

import java.util.ArrayList;

//TODO: Consider if attributes should be private and have getters and setters
public class Task {
    long id;
    int x;
    int y;
    Color color;
    ArrayList<Task> dependencies;
    TaskType taskType;

    public Task(int x, int y, Color color, ArrayList<Task> dependencies) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.dependencies = dependencies;
    }
}
