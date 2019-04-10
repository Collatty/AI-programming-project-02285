package AI;

import java.util.Comparator;
import java.util.List;

import Components.Agent;
import Components.State.Block;
import Components.State.Goal;
import Components.State.State;

public abstract class Heuristic implements Comparator<State> {

    private List<Goal> goals;
    private int heuristic = -1;

    public Heuristic() {
        goals = State.getGoals();
    }

    public int h(State n){
        int heuristicValue = 0;
        List<Block> blocks = n.getBlocks();
        List<Agent> agents = n.getAgents();

        for (Goal goal : goals) {
            char goalType = goal.getType();
            double distanceToBlock = 10000;
            double distanceToAgent = 10000;

            //CALCULATING DISTANCE FROM GOAL TO NEAREST POSSIBLE BLOCK
            for (Block block : blocks) {
                String blockColor = block.getColor();
                char blockType = block.getType();

                if(blockType == goalType) {
                    if (manhattanDistance(goal.getCol(), goal.getRow(), block.getCol(), block.getRow()) < distanceToBlock) {
                        distanceToBlock = manhattanDistance(goal.getCol(), goal.getRow(), block.getCol(), block.getRow());
                    }
                    /*if (manhattanDistance(goal.getColumn(), goal.getRow(), block.getColumn(), block.getRow()) == 0) {
                        distanceToBlock = -100;
                    }*/
                    System.err.println("Distance from block " + block.toString() + " to goal " + goal.toString() +
                            ": " + distanceToBlock +
                            "\t" + "Goal: [" + goal.getCol() + "," + goal.getRow() + "]" +
                            "\t" + "Block: [" + block.getCol() + "," + block.getRow() + "]");


                    //CALCULATING DISTANCE FROM BLOCK TO NEAREST POSSIBLE AGENT
                    for (Agent agent : agents) {
                        //blockColor should be consistent with enum color of agent
                        String agentColor = agent.getColor().toString().toLowerCase();
                        if (agentColor.equals(blockColor)) {
                            if (manhattanDistance(agent.getCol(), agent.getRow(), block.getCol(), block.getRow()) < distanceToAgent) {
                                distanceToAgent = manhattanDistance(agent.getCol(), agent.getRow(), block.getCol(),
                                        block.getRow()) - 1;
                                System.err.println("Distance from agent " + agent.toString() + " to block " + block
                                .toString() + ": " + distanceToAgent +
                                        "\t" + "Agent: [" + agent.getCol() + "," + agent.getRow() + "]" +
                                        "\t" + "Block: [" + block.getCol() + "," + block.getRow() + "]");
                            }
                        }
                    }
                }
            }
            System.err.println("Distance to Agent: " + distanceToAgent);
            System.err.println("Distance to Block: " + distanceToBlock);
            heuristicValue += distanceToBlock;
            heuristicValue += distanceToAgent;
        }
        this.heuristic = heuristicValue;
        return heuristicValue;
    }

    @Override
    public String toString() {
        return Integer.toString(this.heuristic);
    }

    //EUCLIDEAN DISTANCE
    public double euclidianDistance (double goalCordX, double goalCordY, double boxCordX, double boxCordY){
        return Math.hypot( (goalCordX-boxCordX), (goalCordY-boxCordY));
    }

    //MANHATTAN DISTANCE
    public double manhattanDistance (double goalCordX, double goalCordY, double boxCordX, double boxCordY) {
        return (Math.abs(goalCordX-boxCordX) + Math.abs(goalCordY-boxCordY));
    }

    /*public abstract int f(State n);

    @Override
    public int compare(State n1, State n2) {
        return this.f(n1) - this.f(n2);
    }

    public static class AStar extends Heuristic {
        public AStar(State initialState) {
            super(initialState);
        }

        @Override
        public int f(State n) {
            return n.g() + this.h(n);
        }

        @Override
        public String toString() {
            return "A* evaluation";
        }
    }

    public static class WeightedAStar extends Heuristic {
        private int W;

        public WeightedAStar(State initialState, int W) {
            super(initialState);
            this.W = W;
        }

        @Override
        public int f(State n) {
            return n.g() + this.W * this.h(n);
        }

        @Override
        public String toString() {
            return String.format("WA*(%d) evaluation", this.W);
        }
    }

    public static class Greedy extends Heuristic {
        public Greedy(State initialState) {
            super(initialState);
        }

        @Override
        public int f(State n) {
            return this.h(n);
        }

        @Override
        public String toString() {
            return "Greedy evaluation";
        }
    }
    */
}
