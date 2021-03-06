package components.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import components.Action;
import components.Agent;
import components.Color;
import utilities.LevelReader;

public class State {

    // INITIAL STATE STRINGS STORED HERE
    private static String STRING_COLORS = LevelReader.getColors();
    private static String STRING_INITIAL = LevelReader.getInitial();
    private static String STRING_GOALS = LevelReader.getGoals();

    private static List<List<Tile>> INITIAL_STATE = new ArrayList<>();
    private static List<Goal> GOALS = new ArrayList<>();
    private static List<Block> blocks = new ArrayList<>();
    private static List<Agent> agents = new ArrayList<>();
    private static boolean[][] wallMatrix;
    private static int maxCol = 0;
    private static boolean solved;
    private static State state = new State();
    private static List<Color> agentColors;

    private List<List<Tile>> currentTiles;

    public State() {
	State.createState();
	this.currentTiles = INITIAL_STATE;
    }

    // created for testing purposes
    public static void loadNewState() {
	INITIAL_STATE = new ArrayList<>();
	GOALS = new ArrayList<>();
	blocks = new ArrayList<>();
	agents = new ArrayList<>();
	maxCol = 0;
	STRING_COLORS = LevelReader.getColors();
	STRING_INITIAL = LevelReader.getInitial();
	STRING_GOALS = LevelReader.getGoals();
	solved = false;
	State.createState();

    }

    public static Agent getAgentByNumber(int number) {

	for (Agent agent : agents) {
	    if (agent.getAgentNumber() == number) {
		return agent;
	    }
	}
	return null;
    }

    private static void createState() {
	int row = 0;
	int col = 0;
	List<Tile> currentList = new ArrayList<>();
	setAgentColors();

	for (char character : STRING_INITIAL.toCharArray()) {
	    if (character == '\n') {
		if (col > maxCol) {
		    maxCol = col;
		}
		col = 0;
		row++;
		INITIAL_STATE.add(currentList);
		currentList = new ArrayList<>();
		continue;
	    }

	    if (character == '+') { // Wall.
		Tile tile = new Tile(row, col);
		tile.setWall(true);
		tile.setNotUsedInLastMove(true);
		currentList.add(tile);
	    } else if ('0' <= character && character <= '9') { // Agent.
		Tile tile = new Tile(row, col);
		Agent agt = new Agent(Character.getNumericValue(character),
			convertFromStringToColor(getColorAgent(character)), row, col);
		tile.setTileOccupant(agt);
		tile.setNotUsedInLastMove(true);
		currentList.add(tile);
		agents.add(agt);
	    } else if ('A' <= character && character <= 'Z') { // Box.
		Tile tile = new Tile(row, col);
		// CHECK IF THERE ARE UNMOVABLE BLOCKS
		if (!checkAgentsHasColor(convertFromStringToColor(getColorBlockAndGoal(character)))) {
		    tile.setWall(true);
		} else {
		    Block box = new Block(character, convertFromStringToColor(getColorBlockAndGoal(character)), row,
			    col);
		    tile.setTileOccupant(box);
		    tile.setNotUsedInLastMove(true);
		    blocks.add(box);
		}
		currentList.add(tile);
	    } else if (character == ' ') {
		Tile tile = new Tile(row, col);
		currentList.add(tile);
		tile.setNotUsedInLastMove(true);
	    } else {
		System.err.println("Something's fishy at: " + (int) character);
		System.exit(1);
	    }
	    col++;
	}
	col = 0;
	row = 0;

	for (char character : STRING_GOALS.toCharArray()) {

	    if (character == '\n') {
		row++;
		col = 0;
		continue;
	    }
	    if ('A' <= character && character <= 'Z') { // Goal.
		final Goal goal = new Goal(character, convertFromStringToColor(getColorBlockAndGoal(character)), row,
			col);
		INITIAL_STATE.get(row).get(col).setGoal(goal);
		GOALS.add(goal);
	    }
	    col++;
	}
	// REITERATING THROUGH TILES TO "CONNECT" THE BOARD
	setNeighbors(INITIAL_STATE);
	setReachableAgentsOnBlocks();
	setReachableBlocksOnGoals();
	wallMatrix = createWallBoard(INITIAL_STATE);
    }

    private static void setReachableBlocksOnGoals() {
	int maxRow = INITIAL_STATE.size();
	int maxCol = INITIAL_STATE.get(0).size();

	for (Goal goal : GOALS) {
	    boolean[][] visited = new boolean[maxRow][maxCol];
	    LinkedList<Tile> queue = new LinkedList<>();

	    Tile tile = INITIAL_STATE.get(goal.getRow()).get(goal.getCol());
	    visited[tile.getRow()][tile.getCol()] = true;

	    queue.add(tile);

	    while (queue.size() != 0) {
		tile = queue.poll();

		for (Tile neighbor : tile.getNeighbors()) {
		    if (!neighbor.isWall() && !visited[neighbor.getRow()][neighbor.getCol()]) {
			if (neighbor.hasBlock()) {
			    Block b = (Block) (neighbor.getTileOccupant());
			    if (b.getType() == goal.getType()) {
				goal.addReachableBlock(b);
			    }
			}
			visited[neighbor.getRow()][neighbor.getCol()] = true;
			queue.add(INITIAL_STATE.get(neighbor.getRow()).get(neighbor.getCol()));
		    }
		}
	    }
	}
    }

    private static void setReachableAgentsOnBlocks() {
	Iterator<Block> blockItr = blocks.iterator();
	while (blockItr.hasNext()) {
	    Block block = blockItr.next();
	    if (!isBlockReachable(block)) {
		INITIAL_STATE.get(block.getRow()).get(block.getCol()).setWall(true);
		INITIAL_STATE.get(block.getRow()).get(block.getCol()).removeTileOccupant();
		blockItr.remove();
	    }
	}
    }

    private static boolean isBlockReachable(Block block) {
	int maxRow = INITIAL_STATE.size();
	int maxCol = INITIAL_STATE.get(0).size();
	boolean[][] visited = new boolean[maxRow][maxCol];
	LinkedList<Tile> queue = new LinkedList<>();

	Tile tile = INITIAL_STATE.get(block.getRow()).get(block.getCol());
	visited[tile.getRow()][tile.getCol()] = true;

	queue.add(tile);

	while (queue.size() != 0) {
	    tile = queue.poll();

	    for (Tile neighbor : tile.getNeighbors()) {
		if (!neighbor.isWall() && !visited[neighbor.getRow()][neighbor.getCol()]) {
		    if (neighbor.hasAgent()) {
			Agent a = (Agent) (neighbor.getTileOccupant());
			if (a.getColor().equals(block.getColor())) {
			    block.addReachableAgent(a);
			}
		    }
		    visited[neighbor.getRow()][neighbor.getCol()] = true;
		    queue.add(INITIAL_STATE.get(neighbor.getRow()).get(neighbor.getCol()));
		}
	    }
	}

	return block.getReachableAgents().size() > 0;
    }

    public State(State copyState) {
	List<List<Tile>> copy = new ArrayList<>();
	for (List<Tile> row : copyState.getCurrentTiles()) {
	    ArrayList<Tile> copyRow = new ArrayList<>();
	    for (Tile tile : row) {
		Tile copyTile = new Tile(tile.getRow(), tile.getCol());
		copyTile.setNotUsedInLastMove(tile.notUsedInLastMove());
		if (tile.isGoal()) {
		    Goal goal = new Goal(tile.getGoal().getType(), tile.getGoal().getColor(), tile.getGoal().getRow(),
			    tile.getGoal().getCol());
		    copyTile.setGoal(goal);
		}
		if (tile.isWall()) {
		    copyTile.setWall(true);
		}
		if (tile.hasBlock()) {
		    Block block = new Block(((Block) tile.getTileOccupant()).getType(),
			    ((Block) tile.getTileOccupant()).getColor(), ((Block) tile.getTileOccupant()).getRow(),
			    ((Block) tile.getTileOccupant()).getCol());
		    copyTile.setTileOccupant(block);
		}
		if (tile.hasAgent()) {
		    Agent agent = new Agent(((Agent) tile.getTileOccupant()).getAgentNumber(),
			    ((Agent) tile.getTileOccupant()).getColor(), ((Agent) tile.getTileOccupant()).getRow(),
			    ((Agent) tile.getTileOccupant()).getCol());
		    copyTile.setTileOccupant(agent);
		}
		copyRow.add(copyTile);
	    }
	    copy.add(copyRow);
	}
	setNeighbors(copy);
	this.currentTiles = copy;
    }

    // GETTERS

    public static int getMaxCol() {
	return maxCol;
    }

    public List<List<Tile>> getCurrentTiles() {
	return currentTiles;
    }

    public static List<List<Tile>> getInitialState() {
	return INITIAL_STATE;
    }

    public static List<Goal> getGoals() {
	return GOALS;
    }

    public static boolean[][] getWallMatrix() {
	return wallMatrix;
    }

    public List<Agent> getAgents() {
	return agents;
    }

    public static List<Block> getBlocks() {
	return blocks;
    }

    // There should be no other numbers in the color string besides the agents
    private static String getColorAgent(char agent) {
	String[] colorsSplitted = STRING_COLORS.split("\n");
	for (String string : colorsSplitted) {
	    if (string.contains(Character.toString(agent))) {
		String[] splittedEvenMore = string.split(":");
		return splittedEvenMore[0];
	    }
	}
	return "NA";
    }

    private static String getColorBlockAndGoal(char blockAndGoal) {
	String[] colorsSplitted = STRING_COLORS.split("\n");
	for (String string : colorsSplitted) {
	    String[] splittedEvenMore = string.split(":");
	    if (splittedEvenMore[1].contains(Character.toString(blockAndGoal).toUpperCase())) {
		return splittedEvenMore[0];
	    }
	}
	return "NA";
    }

    private static void setNeighbors(List<List<Tile>> tiles) {
	for (List<Tile> row : tiles) {
	    for (Tile tile : row) {
		try {
		    tile.setNorthNeighbor(tiles.get(tile.getRow() - 1).get(tile.getCol()).getTile());
		} catch (Exception e) {
		    tile.setNorthNeighbor(null);
		    // e.printStackTrace();
		}
		try {
		    tile.setWestNeighbor(tiles.get(tile.getRow()).get(tile.getCol() - 1).getTile());
		} catch (Exception e) {
		    tile.setWestNeighbor(null);
		    // e.printStackTrace();
		}
		try {
		    tile.setEastNeighbor(tiles.get(tile.getRow()).get(tile.getCol() + 1).getTile());
		} catch (Exception e) {
		    tile.setEastNeighbor(null);
		    // e.printStackTrace();
		}
		try {
		    tile.setSouthNeighbor(tiles.get(tile.getRow() + 1).get(tile.getCol()).getTile());
		} catch (Exception e) {
		    tile.setSouthNeighbor(null);
		    // e.printStackTrace();
		}
	    }
	}
    }

    private static boolean checkAgentsHasColor(Color color) {
	return agentColors.contains(color);
    }

    private static void setAgentColors() {
	List<Color> agtColors = new ArrayList<>();
	String[] colorsSplitted = STRING_COLORS.split("\n");
	for (String string : colorsSplitted) {
	    if (string.matches(".*\\d.*")) {
		String[] splittedEvenMore = string.split(":");
		agtColors.add(convertFromStringToColor(splittedEvenMore[0]));
	    }
	}
	agentColors = agtColors;
    }

    private static boolean[][] createWallBoard(List<List<Tile>> state) {

	int max_row = state.size();
	int max_col = state.get(0).size();
	boolean[][] walls = new boolean[max_row][max_col];
	int i_row = 0;
	int i_col;
	for (List<Tile> row : state) {
	    i_col = 0;
	    for (Tile col : row) {
		if (col.isWall()) {
		    walls[i_row][i_col] = true;
		} else {
		    walls[i_row][i_col] = false;
		}
		i_col++;
	    }
	    i_row++;
	}
	return walls;
    }

    public static boolean[][] createWallBoardWithBlocks(Color colorOfBlockToBeMoved) {

	boolean[][] walls = createWallBoard(State.getInitialState());
	for (Block block : blocks) {
	    if (!block.getColor().equals(colorOfBlockToBeMoved)) {
		walls[block.getRow()][block.getCol()] = true;
	    }
	}
	return walls;
    }

    private static Color convertFromStringToColor(String stringColor) {
	for (Color enumColor : Color.values()) {
	    if (stringColor.toUpperCase().equals(enumColor.toString())) {
		return enumColor;
	    }
	}
	throw new RuntimeException("Color unknown: " + stringColor);
    }

    public static boolean isSolved() {
	return solved;
    }

    public static void setSolved(boolean solved) {
	State.solved = solved;
    }

    public boolean isLegalMove(Action action) {
	if (action.getActionType().equals(Action.ActionType.NoOp)) {
	    return true;
	} else if (action.getActionType().equals(Action.ActionType.Move)) {
	    if (this.getCurrentTiles().get(action.getEndAgent().getRow()).get(action.getEndAgent().getCol()).isFree()) {
		return true;
	    }
	} else if (action.getActionType().equals(Action.ActionType.Push)) {
	    return (this.getCurrentTiles().get(action.getEndAgent().getRow()).get(action.getEndAgent().getCol())
		    .isFree()
		    || this.getCurrentTiles().get(action.getEndAgent().getRow()).get(action.getEndAgent().getCol())
			    .equals(this.getCurrentTiles().get(action.getStartBox().getRow())
				    .get(action.getStartBox().getCol())))
		    && this.getCurrentTiles().get(action.getEndBox().getRow()).get(action.getEndBox().getCol())
			    .isFree();

	} else if (action.getActionType().equals(Action.ActionType.Pull)) {
	    return this.getCurrentTiles().get(action.getEndAgent().getRow()).get(action.getEndAgent().getCol()).isFree()
		    && (this.getCurrentTiles().get(action.getEndBox().getRow()).get(action.getEndBox().getCol())
			    .equals(this.getCurrentTiles().get(action.getStartAgent().getRow())
				    .get(action.getStartAgent().getCol()))
			    || this.getCurrentTiles().get(action.getEndBox().getRow()).get(action.getEndBox().getCol())
				    .isFree());
	}
	return false;
    }

    public void makeMove(Action action, boolean firstStateMoveIsMadeIn) {
	// TODO sometimes this agent variable is null... why?
	// Happens when we try to get an agent form a location in a state, where there
	// is no agent. If it happens, it is a bug
	Agent agent = (Agent) this.currentTiles.get(action.getStartAgent().getRow())
		.get(action.getStartAgent().getCol()).getTileOccupant();
	this.getCurrentTiles().get(action.getStartAgent().getRow()).get(action.getStartAgent().getCol())
		.removeTileOccupant();
	this.getCurrentTiles().get(action.getStartAgent().getRow()).get(action.getStartAgent().getCol())
		.setNotUsedInLastMove(!firstStateMoveIsMadeIn);
	agent.setRow(action.getEndAgent().getRow());
	agent.setCol(action.getEndAgent().getCol());
	if (action.getActionType().equals(Action.ActionType.Pull)
		|| action.getActionType().equals(Action.ActionType.Push)) {
	    Block block = (Block) this.currentTiles.get(action.getStartBox().getRow())
		    .get(action.getStartBox().getCol()).getTileOccupant();
	    this.getCurrentTiles().get(action.getStartBox().getRow()).get(action.getStartBox().getCol())
		    .removeTileOccupant();
	    this.getCurrentTiles().get(action.getStartBox().getRow()).get(action.getStartBox().getCol())
		    .setNotUsedInLastMove(!firstStateMoveIsMadeIn);
	    this.currentTiles.get(action.getEndBox().getRow()).get(action.getEndBox().getCol()).setTileOccupant(block);
	    block.setRow(action.getEndBox().getRow());
	    block.setCol(action.getEndBox().getCol());
	}
	this.getCurrentTiles().get(action.getEndAgent().getRow()).get(action.getEndAgent().getCol())
		.setTileOccupant(agent);
    }

    public static List<Agent> getInitialAgents() {
	return agents;
    }

    public static State getState() {
	return state;
    }

    public void setUsedInLastActionTilesFree() {
	for (List<Tile> tList : this.currentTiles) {
	    for (Tile t : tList) {
		t.setNotUsedInLastMove(true);
	    }
	}
    }

    public void print() {
	for (List<Tile> tList : this.currentTiles) {
	    for (Tile t : tList) {
		System.err.print(t.toString().substring(0, 1));
	    }
	    System.err.println();
	}
    }
}
