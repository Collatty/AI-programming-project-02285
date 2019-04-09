package Components.State;

import Components.Agent;
import Components.BlackBoard;
import Components.Color;
import Utilities.LevelReader;

import java.util.ArrayList;
import java.util.List;

public class State {

    private final static List<List<Tile>> INITIAL_STATE = new ArrayList<>();
    private final static List<Goal> GOALS = new ArrayList<>();
    private List<List<Tile>> state;

    //INITIAL STATE STRINGS STORED HERE
    private final static String STRING_DOMAIN = LevelReader.getDomain();
    private final static String STRING_LEVEL_NAME = LevelReader.getLevelName();
    private final static String STRING_COLORS = LevelReader.getColors();
    private final static String STRING_INITIAL = LevelReader.getInitial();
    private final static String STRING_GOALS = LevelReader.getGoals();

    public State() {

        int row = 0;
        int col = 0;
        List<Tile> currentList = new ArrayList<>();

        for (char character : STRING_INITIAL.toCharArray()){
            if (character == '\n') {
                col = 0;
                row++;
                INITIAL_STATE.add(currentList);
                currentList = new ArrayList<>();
                continue;
            }

            if (character == '+') { // Wall.
                Tile tile = new Tile(col, row);
                tile.setTileOccupant(new Wall(col, row));
                currentList.add(tile);
            } else if ('0' <= character && character <= '9') { // Agent.
                Tile tile = new Tile(col, row);
                tile.setTileOccupant(new Agent(Character.getNumericValue(character), convertFromStringToColor(getColorAgent(character)), col, row, null));
                currentList.add(tile);
            } else if ('A' <= character && character <= 'Z') { // Box.
                Tile tile = new Tile(col, row);
                tile.setTileOccupant(new Block(character, getColorBlock(character), col, row));
                currentList.add(tile);
            }  else if (character == ' ') {
                Tile tile = new Tile(col, row);
                currentList.add(tile);
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
                col = 0;
                row++;
                continue;
            }
            if ('A' <= character && character <= 'Z') { // Goal.
                final Goal goal = new Goal(character, col, row);
                INITIAL_STATE.get(row).get(col).setGoal(goal);
                GOALS.add(goal);
            }
            col++;
        }
        //REITERATING THROUGH TILES TO "CONNECT" THE BOARD
        setNeighbors(INITIAL_STATE);
    }

    public static List<List<Tile>> copyState(List<List<Tile>> tiles) {
        List<List<Tile>> copy = new ArrayList<>();
        for (List<Tile> row: tiles) {
            ArrayList<Tile> copyRow = new ArrayList<>();
            for (Tile tile : row) {
                Tile copyTile = new Tile(tile.getRow(), tile.getColumn());
                if (tile.isGoal()) {
                    copyTile.setGoal(new Goal(tile.getGoal().getType(), tile.getGoal().getRow(),
                            tile.getGoal().getColumn()));
                }
                if (tile.isWall()) {
                    copyTile.setTileOccupant(new Wall(
                            ((Wall) tile.getTileOccupant()).getRow(),
                            ((Wall) tile.getTileOccupant()).getColumn()));
                }
                if (tile.hasBlock()) {
                    copyTile.setTileOccupant(new Block(
                            ((Block) tile.getTileOccupant()).getType(),
                            ((Block) tile.getTileOccupant()).getColor(),
                            ((Block) tile.getTileOccupant()).getRow(),
                            ((Block) tile.getTileOccupant()).getColumn()));
                }
                if (tile.hasAgent()) {
                    copyTile.setTileOccupant(new Agent(
                            ((Agent) tile.getTileOccupant()).getAgentNumber(),
                            ((Agent) tile.getTileOccupant()).getColor(),
                            ((Agent) tile.getTileOccupant()).getX(),
                            ((Agent) tile.getTileOccupant()).getY(),
                            null));
                }
                copyRow.add(copyTile);
            }
            copy.add(copyRow);
        }
        setNeighbors(copy);

        return copy;
    }

    //GETTERS

    public static List<List<Tile>> getInitialState() {
        return INITIAL_STATE;
    }

    public static List<Goal> getGoals() {
        return GOALS;
    }

    public static String getStringDomain() {
        return STRING_DOMAIN;
    }

    public static String getStringLevelName() {
        return STRING_LEVEL_NAME;
    }

    public static String getStringColors() {
        return STRING_COLORS;
    }

    public static String getStringInitial() {
        return STRING_INITIAL;
    }

    public static String getStringGoals() {
        return STRING_GOALS;
    }

    // There should be no other numbers in the color string besides the agents
    private String getColorAgent(char agent) {
        String[] colorsSplitted = STRING_COLORS.split("\n");
        for (String string : colorsSplitted) {
            if (string.contains(Character.toString(agent))){
                String[] splittedEvenMore = string.split(":");
                return splittedEvenMore[0];
            }
        }
        return "NA";
    }

    private String getColorBlock(char block) {
        String[] colorsSplitted = STRING_COLORS.split("\n");
        for (String string : colorsSplitted) {
            String[] splittedEvenMore = string.split(":");
            if (splittedEvenMore[1].contains(Character.toString(block).toUpperCase())) {
                return splittedEvenMore[0];
            }
        }
        return "NA";
    }

    private static void setNeighbors(List<List<Tile>> tiles) {
        for (List<Tile> row : tiles) {
            for (Tile tile : row) {
                try {
                    tile.setNorthNeighbor(tiles.get(tile.getRow()-1).get(tile.getColumn()).getTile());
                } catch (Exception e) {
                    tile.setNorthNeighbor(null);
                    e.printStackTrace();
                }
                try {
                    tile.setWestNeighbor(tiles.get(tile.getRow()).get(tile.getColumn()-1).getTile());
                } catch (Exception e) {
                    tile.setWestNeighbor(null);
                    e.printStackTrace();
                }
                try {
                    tile.setEastNeighbor(tiles.get(tile.getRow()).get(tile.getColumn()+1).getTile());
                } catch (Exception e) {
                    tile.setEastNeighbor(null);
                    e.printStackTrace();
                }
                try {
                    tile.setSouthNeighbor(tiles.get(tile.getRow()+1).get(tile.getColumn()).getTile());
                } catch (Exception e) {
                    tile.setSouthNeighbor(null);
                    e.printStackTrace();
                }
            }
        }
    }

    private Color convertFromStringToColor(String stringColor){
        switch ( stringColor ) {
            case "green":
                return Color.GREEN;
            case "blue":
                return Color.BLUE;
            case "red":
                return Color.RED;
            default:
                throw new RuntimeException("Color unknown: " + stringColor);

        }
    }
}
