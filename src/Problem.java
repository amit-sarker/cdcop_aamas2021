import java.io.IOException;
import java.util.*;

public class Problem {
    private int MAX_SAME_PROB, MAX_DIFF_PROB, MAX_NODE;
    private int numberOfDiscretePoints;
    private Vector<Integer>[] adj;
    private int[][] indexToEdge;
    private double domain_lb, domain_ub;
    private OutputGraph outputGraph;
    private int beta;

    private ArrayList<Agent> agents;
    private HashMap<Integer, Double> cpa;
    private HashMap<Integer, ArrayList<Double>> neighborPoints;
    private HashMap<Integer, String> neighborState;
    private int problemNo;


    public Problem(int MAX_SAME_PROB, int MAX_DIFF_PROB, int numberOfDiscretePoints, double domain_lb, double domain_ub, int problemNo) {
        this.MAX_SAME_PROB = MAX_SAME_PROB;
        this.MAX_DIFF_PROB = MAX_DIFF_PROB;
        this.numberOfDiscretePoints = numberOfDiscretePoints;
        this.domain_lb = domain_lb;
        this.domain_ub = domain_ub;
        this.problemNo = problemNo;
        agents = new ArrayList<>();
        cpa = new HashMap<>();
        neighborPoints = new HashMap<>();
        neighborState = new HashMap<>();
    }

    public void newProblem() throws IOException, InterruptedException {
        String path = "Inputs/test.txt";
        this.outputGraph = new GraphGenerator().getGraph(path);
        this.MAX_NODE = outputGraph.getMAX_NODE();
        this.adj = new Vector[MAX_NODE];
        this.indexToEdge = new int[MAX_NODE][MAX_NODE];
        for(int diff = 0; diff < MAX_DIFF_PROB; diff++) {
            this.problemNo = diff;
            ArrayList<Edge> edgeList = outputGraph.getAll_edges().get(this.problemNo);
            for (int i = 0; i < MAX_NODE; i++)
                adj[i] = new Vector<>();

            int idx = 0;
            for (Edge e : edgeList) {
                int u = e.getNode1();
                int v = e.getNode2();
                indexToEdge[u][v] = idx;
                idx += 1;
                adj[u].addElement(v);
            }
            for(int same = 0; same < MAX_SAME_PROB; same++) {
                Init();
                Random random = new Random();
                int randomAgent = random.nextInt(MAX_NODE);
                RunAlgorithm(agents.get(randomAgent));
                CheckFurther();
                double total_cost = GetGlobalObjective();
                System.out.println("Global Objective Cost: " + total_cost);
            }
        }
    }

    private double GetGlobalObjective() {
        HashMap<Integer, Double> assignment = new HashMap<>();
        double total_cost = 0.0;
        HashMap<Edge, Constraint> constraints = this.outputGraph.getAll_constraints().get(this.problemNo);
        ArrayList<Edge> edges = this.outputGraph.getAll_edges().get(this.problemNo);

        for(Edge edge: edges) {
            Constraint constraint = constraints.get(edge);
            int e1 = edge.getNode1();
            int e2 = edge.getNode2();
            int a = constraint.getA();
            int b = constraint.getB();
            int c = constraint.getC();
            double x = this.agents.get(e1).getCpa().get(e1);
            double y = this.agents.get(e2).getCpa().get(e2);
            assignment.put(this.agents.get(e1).getAgentNo(), x);
            assignment.put(this.agents.get(e2).getAgentNo(), y);
            double temp_cost = a * x * x + b * x * y + c * y * y;
            total_cost += temp_cost;
        }
        System.out.println("Complete Assignment X* = " + assignment);
        return total_cost / 2.0;
    }

    public void RunAlgorithm(Agent agent_i) {
        double sum = 0.0;
        double[] sumChildCost = new double[agent_i.getDiscreteDomain().size()];
        agent_i.setState("ACTIVE");
        for(int neigh: this.adj[agent_i.getAgentNo()]) {
            this.agents.get(neigh).UpdateStateMessage(agent_i, "ACTIVE", this);
            ArrayList<DomainPair> cost_map = this.agents.get(neigh).InquiryMessage(agent_i, this.cpa);
            agent_i.getInbox().put(neigh, cost_map);
        }

        ArrayList<Integer> rho_array = agent_i.CalculateRho(this);
        if(rho_array.size() <= this.beta || agent_i.IdleActiveNeighbors() == 0) {
            double valueSoFar = agent_i.getDiscreteDomain().get(rho_array.get(0));
            agent_i.getInitial_point().put(agent_i.getAgentNo(), valueSoFar);
            agent_i.GradientDescend();
            agent_i.getCpa().put(agent_i.getAgentNo(), agent_i.getCurrent_value().get(agent_i.getAgentNo()));
            agent_i.setState("DONE");

            for(int neigh: this.adj[agent_i.getAgentNo()]) {
                this.agents.get(neigh).SetValueMessage(agent_i);
                this.agents.get(neigh).UpdateStateMessage(agent_i, "DONE", this);
            }
        }

        else {
            agent_i.setState("HOLD");
            for(int neigh: this.adj[agent_i.getAgentNo()]) {
                this.agents.get(neigh).UpdateStateMessage(agent_i, "HOLD", this);
            }
        }
    }

    public void Init() {
        this.agents = new ArrayList<>();
        this.cpa = new HashMap<>();
        this.beta = 1;
        ArrayList<Double> discrete_points = CalculateDiscretePoints();
//        ArrayList<Double> discrete_points = new ArrayList<>();
//        discrete_points.add(-10.0);
//        discrete_points.add(5.0);
//        discrete_points.add(12.0);
        for(int i = 0; i < MAX_NODE; i++) {
            for(int neigh: adj[i]) {
                this.neighborPoints.put(neigh, discrete_points);
                this.neighborState.put(neigh, "IDLE");
            }
            Agent anAgent = new Agent("IDLE", i, adj[i], outputGraph.getAll_edges().get(problemNo), this.indexToEdge,
                    outputGraph.getAll_constraints().get(problemNo), this.cpa, discrete_points, this.neighborPoints, this.neighborState, this.domain_lb, this.domain_ub);
            this.agents.add(anAgent);
            neighborState = new HashMap<>();
            neighborPoints = new HashMap<>();
        }
    }

    public void Init2() {
        this.beta = 1;
        ArrayList<Double> dp0 = new ArrayList<>();
        ArrayList<Double> dp1 = new ArrayList<>();
        ArrayList<Double> dp2 = new ArrayList<>();
        ArrayList<Double> dp3 = new ArrayList<>();
        dp0.add(1.0);
        dp0.add(2.0);
        dp1.add(3.0);
        dp1.add(4.0);
        dp2.add(7.0);
        dp2.add(8.0);
        dp3.add(5.0);
        dp3.add(9.0);

        this.neighborPoints.put(1, dp1);
        this.neighborPoints.put(2, dp2);
        this.neighborPoints.put(3, dp3);

        this.neighborState.put(1, "IDLE");
        this.neighborState.put(2, "IDLE");
        this.neighborState.put(3, "IDLE");

        Agent anAgent0 = new Agent("IDLE", 0, adj[0], outputGraph.getAll_edges().get(problemNo), this.indexToEdge,
                outputGraph.getAll_constraints().get(problemNo), this.cpa, dp0, this.neighborPoints, this.neighborState, this.domain_lb, this.domain_ub);
        neighborPoints = new HashMap<>();
        neighborState = new HashMap<>();

        this.neighborPoints.put(0, dp0);
        this.neighborPoints.put(2, dp2);

        this.neighborState.put(0, "IDLE");
        this.neighborState.put(2, "IDLE");

        Agent anAgent1 = new Agent("IDLE", 1, adj[1], outputGraph.getAll_edges().get(problemNo), this.indexToEdge,
                outputGraph.getAll_constraints().get(problemNo), this.cpa, dp1, this.neighborPoints, this.neighborState, this.domain_lb, this.domain_ub);

        neighborPoints = new HashMap<>();
//        neighborState = new HashMap<>();

        this.neighborPoints.put(0, dp0);
        this.neighborPoints.put(1, dp1);

        this.neighborState.put(0, "IDLE");
        this.neighborState.put(1, "IDLE");
        Agent anAgent2 = new Agent("IDLE", 2, adj[2], outputGraph.getAll_edges().get(problemNo), this.indexToEdge,
                outputGraph.getAll_constraints().get(problemNo), this.cpa, dp2, this.neighborPoints, this.neighborState, this.domain_lb, this.domain_ub);

        neighborPoints = new HashMap<>();
        neighborState = new HashMap<>();

        this.neighborPoints.put(0, dp0);
        this.neighborState.put(0, "IDLE");
        Agent anAgent3 = new Agent("IDLE", 3, adj[3], outputGraph.getAll_edges().get(problemNo), this.indexToEdge,
                outputGraph.getAll_constraints().get(problemNo), this.cpa, dp3, this.neighborPoints, this.neighborState, this.domain_lb, this.domain_ub);

        this.agents.add(anAgent0);
        this.agents.add(anAgent1);
        this.agents.add(anAgent2);
        this.agents.add(anAgent3);

        neighborState = new HashMap<>();
        neighborPoints = new HashMap<>();
    }

    public ArrayList<Double> CalculateDiscretePoints() {
        ArrayList<Double> discrete = new ArrayList<>();
        ArrayList<Double> list = new ArrayList<>();
        for (double i = this.domain_lb; i <= this.domain_ub; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < this.numberOfDiscretePoints; i++) {
            discrete.add(list.get(i));
        }
        return discrete;
    }

    private void CheckFurther() {
        while(true) {
            for(Agent agent: agents) {
                if(agent.getState().equals("IDLE")) {
                    RunAlgorithm(agent);
                }
            }
            break;
        }
    }

    public int getProblemNo() {
        return problemNo;
    }

    public void setProblemNo(int problemNo) {
        this.problemNo = problemNo;
    }

    public int getMAX_SAME_PROB() {
        return MAX_SAME_PROB;
    }

    public void setMAX_SAME_PROB(int MAX_SAME_PROB) {
        this.MAX_SAME_PROB = MAX_SAME_PROB;
    }

    public int getMAX_DIFF_PROB() {
        return MAX_DIFF_PROB;
    }

    public void setMAX_DIFF_PROB(int MAX_DIFF_PROB) {
        this.MAX_DIFF_PROB = MAX_DIFF_PROB;
    }

    public int getMAX_NODE() {
        return MAX_NODE;
    }

    public void setMAX_NODE(int MAX_NODE) {
        this.MAX_NODE = MAX_NODE;
    }

    public Vector<Integer>[] getAdj() {
        return adj;
    }

    public void setAdj(Vector<Integer>[] adj) {
        this.adj = adj;
    }

    public int[][] getIndexToEdge() {
        return indexToEdge;
    }

    public void setIndexToEdge(int[][] indexToEdge) {
        this.indexToEdge = indexToEdge;
    }

    public OutputGraph getOutputGraph() {
        return outputGraph;
    }

    public void setOutputGraph(OutputGraph outputGraph) {
        this.outputGraph = outputGraph;
    }

    public int getNumberOfDiscretePoints() {
        return numberOfDiscretePoints;
    }

    public void setNumberOfDiscretePoints(int numberOfDiscretePoints) {
        this.numberOfDiscretePoints = numberOfDiscretePoints;
    }

    public double getDomain_lb() {
        return domain_lb;
    }

    public void setDomain_lb(double domain_lb) {
        this.domain_lb = domain_lb;
    }

    public double getDomain_ub() {
        return domain_ub;
    }

    public void setDomain_ub(double domain_ub) {
        this.domain_ub = domain_ub;
    }

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }

    public ArrayList<Agent> getAgents() {
        return agents;
    }

    public void setAgents(ArrayList<Agent> agents) {
        this.agents = agents;
    }

    public HashMap<Integer, Double> getCpa() {
        return cpa;
    }

    public void setCpa(HashMap<Integer, Double> cpa) {
        this.cpa = cpa;
    }

    public HashMap<Integer, ArrayList<Double>> getNeighborPoints() {
        return neighborPoints;
    }

    public void setNeighborPoints(HashMap<Integer, ArrayList<Double>> neighborPoints) {
        this.neighborPoints = neighborPoints;
    }

    public HashMap<Integer, String> getNeighborState() {
        return neighborState;
    }

    public void setNeighborState(HashMap<Integer, String> neighborState) {
        this.neighborState = neighborState;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "MAX_SAME_PROB=" + MAX_SAME_PROB +
                ", MAX_DIFF_PROB=" + MAX_DIFF_PROB +
                ", MAX_NODE=" + MAX_NODE +
                ", numberOfDiscretePoints=" + numberOfDiscretePoints +
                ", adj=" + Arrays.toString(adj) +
                ", indexToEdge=" + Arrays.toString(indexToEdge) +
                ", domain_lb=" + domain_lb +
                ", domain_ub=" + domain_ub +
                ", outputGraph=" + outputGraph +
                ", beta=" + beta +
                ", agents=" + agents +
                ", cpa=" + cpa +
                ", neighborPoints=" + neighborPoints +
                ", neighborState=" + neighborState +
                ", problemNo=" + problemNo +
                '}';
    }
}
