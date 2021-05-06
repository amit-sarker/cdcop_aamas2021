import java.util.ArrayList;
import java.util.HashMap;

public class OutputGraph {
    private ArrayList<Integer> all_nodes;
    private ArrayList<ArrayList<Edge>> all_edges;
    private ArrayList<HashMap<Edge, Constraint>> all_constraints;
    private int MAX_NODE;

    public OutputGraph(ArrayList<Integer> all_nodes, ArrayList<ArrayList<Edge>> all_edges, ArrayList<HashMap<Edge, Constraint>> all_constraints, int MAX_NODE) {
        this.all_nodes = all_nodes;
        this.all_edges = all_edges;
        this.all_constraints = all_constraints;
        this.MAX_NODE = MAX_NODE;
    }

    public ArrayList<Integer> getAll_nodes() {
        return all_nodes;
    }

    public void setAll_nodes(ArrayList<Integer> all_nodes) {
        this.all_nodes = all_nodes;
    }

    public ArrayList<ArrayList<Edge>> getAll_edges() {
        return all_edges;
    }

    public void setAll_edges(ArrayList<ArrayList<Edge>> all_edges) {
        this.all_edges = all_edges;
    }

    public ArrayList<HashMap<Edge, Constraint>> getAll_constraints() {
        return all_constraints;
    }

    public void setAll_constraints(ArrayList<HashMap<Edge, Constraint>> all_constraints) {
        this.all_constraints = all_constraints;
    }

    public int getMAX_NODE() {
        return MAX_NODE;
    }

    public void setMAX_NODE(int MAX_NODE) {
        this.MAX_NODE = MAX_NODE;
    }

    @Override
    public String toString() {
        return "OutputGraph{" +
                "all_nodes=" + all_nodes +
                ", all_edges=" + all_edges +
                ", all_constraints=" + all_constraints +
                ", MAX_NODE=" + MAX_NODE +
                '}';
    }
}
