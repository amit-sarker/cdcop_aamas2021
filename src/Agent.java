import java.util.*;

public class Agent {
    private String state;
    private int agentNo;
    private Vector<Integer> neighbors;
    private double domain_lb, domain_ub;
    private HashMap<Integer, ArrayList<DomainPair>> inbox;
    private ArrayList<Edge> edgeList;
    private int[][] indexToEdge;
    private HashMap<Edge, Constraint> constraints;
    private double[] cons_cost;
    private double[] globalRootCost;
    long startTime;
    private HashMap<Integer, Double> cpa;
    private HashMap<Integer, String> neighborState;
    private ArrayList<Double> discreteDomain;
    private HashMap<Integer, ArrayList<Double>> neighborDiscreteDomain;
    private HashMap<Integer, Double> initial_point;
    private HashMap<Integer, Double> current_value;
    private double alpha;
    private int max_iters;

    public Agent(String state, int agentNo, Vector<Integer> neighbors, ArrayList<Edge> edgeList, int[][] indexToEdge, HashMap<Edge,
            Constraint> constraints, HashMap<Integer, Double> cpa, ArrayList<Double> discreteDomain, HashMap<Integer,
            ArrayList<Double>> neighborDiscreteDomain, HashMap<Integer, String> neighborState, double domain_lb, double domain_ub) {
        this.state = state;
        this.agentNo = agentNo;
        this.neighbors = neighbors;
        this.edgeList = edgeList;
        this.indexToEdge = indexToEdge;
        this.constraints = constraints;
        this.cpa = cpa;
        this.discreteDomain = discreteDomain;
        this.neighborDiscreteDomain = neighborDiscreteDomain;
        this.neighborState = neighborState;
        this.domain_ub = domain_ub;
        this.domain_lb = domain_lb;

        initial_point = new HashMap<>();
        alpha = 0.01;
        max_iters = 100;
        inbox = new HashMap<>();
        current_value = new HashMap<>();
    }

    public void UpdateStateMessage(Agent sender, String state, Problem problem) {
        this.neighborState.put(sender.getAgentNo(), state);
        if (state.equals("HOLD") && this.state.equals("HOLD") && IdleActiveNeighbors() == 0) {
            problem.setBeta(problem.getBeta() + 1);
            problem.RunAlgorithm(this);
        }
        else if (state.equals("DONE") && this.state.equals("HOLD")) {
            problem.RunAlgorithm(this);
        }
        else if (state.equals("DONE") && this.state.equals("IDLE")) {
            problem.RunAlgorithm(this);
        }
    }

    public ArrayList<DomainPair> InquiryMessage(Agent sender, HashMap<Integer, Double> sender_cpa) {
        ArrayList<Double> D_i_dis = this.neighborDiscreteDomain.get(sender.getAgentNo());
        ArrayList<Double> D_j_dis = new ArrayList<>();
        double assigned_value;
        ArrayList<DomainPair> cost_map = new ArrayList<>();
        boolean isAssigned = false;

        if (sender_cpa.containsKey(this.agentNo)) {
            D_j_dis.add(sender_cpa.get(this.agentNo));
        }
        else {
            D_j_dis = this.discreteDomain;
        }

        for (double x : D_i_dis) {
            double min = Double.MAX_VALUE;
            double argmin = -1.0;
            for (double y : D_j_dis) {
                Constraint cons = this.constraints.get(edgeList.get(indexToEdge[sender.getAgentNo()][this.agentNo]));
                int a = cons.getA();
                int b = cons.getB();
                int c = cons.getC();
                double temp_cost = a * x * x + b * x * y + c * y * y;
                if (temp_cost < min) {
                    min = temp_cost;
                    argmin = y;
                }
            }
            DomainPair domainPair = new DomainPair(argmin, min);
            cost_map.add(domainPair);

//                double minimum = Collections.min(cost.values());
//                int min_key = getKeyByValue(cost, minimum);
//                cost_map.put(min_key, minimum);
        }
        return cost_map;
    }

    public void SetValueMessage(Agent sender) {
        this.cpa.put(sender.getAgentNo(), sender.getCpa().get(sender.getAgentNo()));
    }

    public ArrayList<Integer> CalculateRho(Problem problem) {
        double sum = 0.0;
        double[] sumChildCost = new double[this.discreteDomain.size()];
//        int[] initial_point = new int[this.discreteDomain.size()];
        Arrays.fill(sumChildCost, 0.0);
        for(int neigh: this.neighbors) {
            ArrayList<DomainPair> temp_cost_map = this.inbox.get(neigh);
            int i = 0;
            for(DomainPair domainPair: temp_cost_map) {
                sumChildCost[i] += domainPair.getPoint2();
                i++;
            }
        }
        double min = Double.MAX_VALUE;
//        ArrayList<Double> min_array = new ArrayList<>();
        ArrayList<Integer> argmin_array = new ArrayList<>();
        int argmin = -1;
        for(int i = 0; i < sumChildCost.length; i++) {
            if(sumChildCost[i] < min) {
                min = sumChildCost[i];
                argmin = i;
            }
//            min_array.add(min);
//            argmin_array.add(argmin);
        }
        for(int i = 0; i < sumChildCost.length; i++) {
            if(sumChildCost[i] == min) {
                argmin_array.add(i);
            }
        }

//        HashMap<Integer, Integer> initial_point = new HashMap<>();
        for(int neigh: this.neighbors) {
            ArrayList<DomainPair> temp = inbox.get(neigh);
            DomainPair domainPair = temp.get(argmin_array.get(0));
            initial_point.put(neigh, domainPair.getPoint1());
        }
        return argmin_array;
    }

    public void GradientDescend() {
        int iters = 0;
        current_value = (HashMap<Integer, Double>) copyMap(initial_point);

        while(iters < max_iters) {
            int A = 0, B = 0, C = 0;
            for(int neigh: neighbors) {
                Constraint cons = this.constraints.get(edgeList.get(indexToEdge[this.agentNo][neigh]));
                A += cons.getA();
                B += cons.getB() * current_value.get(neigh);
            }
            double value = current_value.get(this.agentNo) - alpha * ((2 * current_value.get(this.agentNo) * A) + B);
            if(value < this.domain_lb) {
                value = this.domain_lb;
            }
            else if(value > this.domain_ub) {
                value = domain_ub;
            }
            current_value.put(this.agentNo, value);

            for(int neigh: neighbors) {
                if(this.cpa.containsKey(neigh)) {
                    continue;
                }
                Constraint cons = this.constraints.get(edgeList.get(indexToEdge[this.agentNo][neigh])); /////////////////////////////////////////
                double neigh_value = current_value.get(neigh) - alpha * ((current_value.get(this.agentNo) * cons.getB()) + (2 * cons.getC() * current_value.get(neigh)));
                if(neigh_value < this.domain_lb) {
                    neigh_value = this.domain_lb;
                }
                else if(neigh_value > this.domain_ub) {
                    neigh_value = domain_ub;
                }
                current_value.put(neigh, neigh_value);
            }
            iters++;
        }
    }

    public int IdleActiveNeighbors() {
        int count = 0;
        for (Object value : this.neighborState.values()) {
            if(value.equals("IDLE") || value.equals("ACTIVE")) {
                count++;
            }
        }
        return count;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static <K, V> Map<K, V> copyMap(Map<K, V> original) {
        return new HashMap<>(original);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAgentNo() {
        return agentNo;
    }

    public void setAgentNo(int agentNo) {
        this.agentNo = agentNo;
    }

    public Vector<Integer> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Vector<Integer> neighbors) {
        this.neighbors = neighbors;
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

    public ArrayList<Edge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(ArrayList<Edge> edgeList) {
        this.edgeList = edgeList;
    }

    public int[][] getIndexToEdge() {
        return indexToEdge;
    }

    public void setIndexToEdge(int[][] indexToEdge) {
        this.indexToEdge = indexToEdge;
    }

    public HashMap<Edge, Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(HashMap<Edge, Constraint> constraints) {
        this.constraints = constraints;
    }

    public double[] getCons_cost() {
        return cons_cost;
    }

    public void setCons_cost(double[] cons_cost) {
        this.cons_cost = cons_cost;
    }

    public double[] getGlobalRootCost() {
        return globalRootCost;
    }

    public void setGlobalRootCost(double[] globalRootCost) {
        this.globalRootCost = globalRootCost;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public HashMap<Integer, Double> getCpa() {
        return cpa;
    }

    public void setCpa(HashMap<Integer, Double> cpa) {
        this.cpa = cpa;
    }

    public HashMap<Integer, String> getNeighborState() {
        return neighborState;
    }

    public void setNeighborState(HashMap<Integer, String> neighborState) {
        this.neighborState = neighborState;
    }

    public ArrayList<Double> getDiscreteDomain() {
        return discreteDomain;
    }

    public void setDiscreteDomain(ArrayList<Double> discreteDomain) {
        this.discreteDomain = discreteDomain;
    }

    public HashMap<Integer, ArrayList<Double>> getNeighborDiscreteDomain() {
        return neighborDiscreteDomain;
    }

    public void setNeighborDiscreteDomain(HashMap<Integer, ArrayList<Double>> neighborDiscreteDomain) {
        this.neighborDiscreteDomain = neighborDiscreteDomain;
    }

    public HashMap<Integer, Double> getInitial_point() {
        return initial_point;
    }

    public void setInitial_point(HashMap<Integer, Double> initial_point) {
        this.initial_point = initial_point;
    }

    public HashMap<Integer, Double> getCurrent_value() {
        return current_value;
    }

    public void setCurrent_value(HashMap<Integer, Double> current_value) {
        this.current_value = current_value;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public int getMax_iters() {
        return max_iters;
    }

    public void setMax_iters(int max_iters) {
        this.max_iters = max_iters;
    }

    public HashMap<Integer, ArrayList<DomainPair>> getInbox() {
        return inbox;
    }

    public void setInbox(HashMap<Integer, ArrayList<DomainPair>> inbox) {
        this.inbox = inbox;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "state='" + state + '\'' +
                ", agentNo=" + agentNo +
                ", neighbors=" + neighbors +
                ", inbox=" + inbox +
                ", edgeList=" + edgeList +
                ", indexToEdge=" + Arrays.toString(indexToEdge) +
                ", constraints=" + constraints +
                ", cpa=" + cpa +
                ", neighborState=" + neighborState +
                ", discreteDomain=" + discreteDomain +
                ", neighborDiscreteDomain=" + neighborDiscreteDomain +
                ", initial_point=" + initial_point +
                ", current_value=" + current_value +
                ", alpha=" + alpha +
                '}';
    }
}
