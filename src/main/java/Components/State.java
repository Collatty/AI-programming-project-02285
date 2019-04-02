package Components;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State {


    private static final Random RNG = new Random(1);


    public static void setMaxRow(int maxRow) {
        MAX_ROW = maxRow;
    }

    public static int getMaxCol() {
        return MAX_COL;
    }

    public static void setMaxCol(int maxCol) {
        MAX_COL = maxCol;
    }

    public static int MAX_ROW = 70;
    public static int MAX_COL = 70;



    // Arrays are indexed from the top-left of the level, with first index being row and second being column.
    // Row 0: (0,0) (0,1) (0,2) (0,3) ...
    // Row 1: (1,0) (1,1) (1,2) (1,3) ...
    // Row 2: (2,0) (2,1) (2,2) (2,3) ...
    // ...
    // (Start in the top left corner, first go down, then go right)
    // E.g. this.walls[2] is an array of booleans having size MAX_COL.
    // this.walls[row][col] is true if there's a wall at (row, col)
    //

    public static boolean[][] walls = new boolean[MAX_ROW][MAX_COL];
    public char[][] boxes = new char[MAX_ROW][MAX_COL];
    public char[][] agents = new char [MAX_ROW][MAX_COL];
    public static char[][] goals = new char[MAX_ROW][MAX_COL];



    public State parent;
    public Action action;

    private int g;

    private int _hash = 0;

    public State(State parent) {
        this.parent = parent;
        if (parent == null) {
            this.g = 0;
        } else {
            this.g = parent.g() + 1;
        }
    }

    public State(String initialState, String goals) throws Exception {

        int row = 0;
        int col = 0;
        this.parent = null;

        for (char character : initialState.toCharArray()){
                if (character == '\n') {
                    col = 0;
                    row++;
                    continue;
                }

                if (character == '+') { // Wall.
                    State.walls[row][col] = true;
                } else if ('0' <= character && character <= '9') { // Agent.
                    this.agents[row][col] = character;
                } else if ('A' <= character && character <= 'Z') { // Box.
                    this.boxes[row][col] = character;
                }  else if (character == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) character);
                    System.exit(1);
                }
                col++;
            }
        State.setMaxRow(row+1);

        col = 0;
        row = 0;

        for (char character : goals.toCharArray()) {

                if (character == '\n') {
                    col = 0;
                    row++;
                    continue;
                }


                if ('A' <= character && character <= 'Z') { // Goal.
                    State.goals[row][col] = Character.toLowerCase(character);
                }

            col++;
            }


        }



    public int g() {
        return this.g;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public boolean isGoalState() {
        for (int row = 1; row < MAX_ROW - 1; row++) {
            for (int col = 1; col < MAX_COL - 1; col++) {
                char g = goals[row][col];
                char b = Character.toLowerCase(boxes[row][col]);
                if (g > 0 && b != g) {
                    return false;
                }
            }
        }
        return true;
    }

/*    public ArrayList<State> getExpandedStates() {
        ArrayList<State> expandedStates = new ArrayList<>(Action.EVERY.length);
        for (Action c : Action.EVERY) {
            // Determine applicability of action
            int newAgentRow = this.agentRow + Action.dirToRowChange(c.dir1);
            int newAgentCol = this.agentCol + Action.dirToColChange(c.dir1);

            if (c.actionType == Action.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    State n = this.ChildState();
                    n.action = c;
                    n.agentRow = newAgentRow;
                    n.agentCol = newAgentCol;
                    expandedStates.add(n);
                }
            } else if (c.actionType == Action.Type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Action.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Action.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        State n = this.ChildState();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
                        n.boxes[newAgentRow][newAgentCol] = 0;
                        expandedStates.add(n);
                    }
                }
            } else if (c.actionType == Action.Type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = this.agentRow + Action.dirToRowChange(c.dir2);
                    int boxCol = this.agentCol + Action.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        State n = this.ChildState();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        n.boxes[this.agentRow][this.agentCol] = this.boxes[boxRow][boxCol];
                        n.boxes[boxRow][boxCol] = 0;
                        expandedStates.add(n);
                    }
                }
            }
        }
        Collections.shuffle(expandedStates, RNG);
        return expandedStates;
    }*/

    private boolean cellIsFree(int row, int col) {
        return !this.walls[row][col] && this.boxes[row][col] == 0;
    }

    private boolean boxAt(int row, int col) {
        return this.boxes[row][col] > 0;
    }

    private State ChildState() {
        State copy = new State(this);
        for (int row = 0; row < MAX_ROW; row++) {
            System.arraycopy(this.walls[row], 0, copy.walls[row], 0, MAX_COL);
            System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
            System.arraycopy(this.goals[row], 0, copy.goals[row], 0, MAX_COL);
        }
        return copy;
    }

    public ArrayList<State> extractPlan() {
        ArrayList<State> plan = new ArrayList<>();
        State n = this;
        while (!n.isInitialState()) {
            plan.add(n);
            n = n.parent;
        }
        Collections.reverse(plan);
        return plan;
    }

    @Override
    public int hashCode() {
        if (this._hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.deepHashCode(this.agents);
            result = prime * result + Arrays.deepHashCode(this.boxes);
            result = prime * result + Arrays.deepHashCode(goals);
            result = prime * result + Arrays.deepHashCode(walls);
            this._hash = result;
        }
        return this._hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if (!Arrays.deepEquals(this.agents, other.agents))
            return false;
        if (!Arrays.deepEquals(this.boxes, other.boxes))
            return false;
        if (!Arrays.deepEquals(this.goals, other.goals))
            return false;
        return Arrays.deepEquals(this.walls, other.walls);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < MAX_ROW; row++) {
            if (!this.walls[row][0]) {
                break;
            }
            for (int col = 0; col < MAX_COL; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (this.goals[row][col] > 0) {
                    s.append(this.goals[row][col]);
                } else if (this.walls[row][col]) {
                    s.append("+");
                } else if (this.agents[row][col] > 0) {
                    s.append(this.agents[row][col]);
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }

}
