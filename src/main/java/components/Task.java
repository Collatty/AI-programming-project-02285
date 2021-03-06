package components;

import java.util.List;

import components.state.Block;
import components.state.Goal;
import components.state.State;

//TODO: Consider if attributes should be private and have getters and setters
public class Task {
    private static int counter = 0;
    private final Block block;
    private long id;
    private Color color;
    private List<Long> dependencies;
    private TaskType taskType;
    private State prestate;
    private Goal goal;

    public boolean isSolved() {
	return solved;
    }

    public void setSolved(boolean solved) {
	this.solved = solved;
    }

    private boolean solved;

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public Color getColor() {
	return color;
    }

    public void setColor(Color color) {
	this.color = color;
    }

    public List<Long> getDependencies() {
	return dependencies;
    }

    public void setDependencies(List<Long> dependencies) {
	this.dependencies = dependencies;
    }

    public TaskType getTaskType() {
	return taskType;
    }

    public void setTaskType(TaskType taskType) {
	this.taskType = taskType;
    }

    public Task(Color color, List<Long> dependencies, Goal goal, Block block) {
	this.id = counter;
	counter++;
	this.color = color;
	this.dependencies = dependencies;
	this.goal = goal;
	this.solved = false;
	this.block = block;

    }

    public State getPrestate() {
	return prestate;
    }

    @Override
    public String toString() {
	return "Task: " + this.id + "of color: " + this.color;
    }

    public void setPrestate(State prestate) {
	this.prestate = prestate;
    }

    public Goal getGoal() {
	return this.goal;
    }

    public Block getBlock() {
	return block;
    }

    public static class MoveAgentTask extends Task {
	private int agentNumber;
	private List<Action> occupiedTiles;

	public MoveAgentTask(Color color, List<Long> dependencies, int agentNumber, List<Action> occupiedTiles) {
	    super(color, dependencies, null, null);
	    this.agentNumber = agentNumber;
	    this.occupiedTiles = occupiedTiles;
	}

	public int getAgentNumber() {
	    return agentNumber;
	}

	public List<Action> getOccupiedTiles() {
	    return occupiedTiles;
	}
    }

    public static class MoveBlockTask extends Task {

	public List<Action> getOccupiedTiles() {
	    return occupiedTiles;
	}

	private List<Action> occupiedTiles;

	public MoveBlockTask(Color color, List<Long> dependencies, List<Action> occupiedTiles, Block block) {
	    super(color, dependencies, null, block);
	    this.occupiedTiles = occupiedTiles;
	}

    }

}
