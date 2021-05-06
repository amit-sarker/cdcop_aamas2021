import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphGenerator {
    public OutputGraph getGraph(String pathName) throws IOException {
        int MAX_NODE = 100;
//        File input_file = new File(path+"problem_config_tree.txt");
        File input_file = new File(pathName);
        ArrayList<Integer> all_nodes = new ArrayList<>();
        ArrayList<ArrayList<Edge>> all_edges = new ArrayList<>();
        ArrayList<HashMap<Edge, Constraint>> all_constraints = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(input_file));
        String st;
        int[][] indexToEdge = new int[MAX_NODE][MAX_NODE];
        int node_number = -1;
        while((st = br.readLine()) != null) {
            if(st.startsWith("nodes")) {
                String temp = st.replace("nodes=", "");
                node_number = Integer.parseInt(temp);
                for(int i = 0; i < node_number; i++) {
                    all_nodes.add(i);
                }
            }

            if(st.startsWith("edges")) {
                indexToEdge = new int[MAX_NODE][MAX_NODE];
                String temp = st.replace("edges=", "");
                String[] arrayOfEdge = temp.split(" ", -2);
                ArrayList<Edge> tempAllEdges = new ArrayList<>();
                int idx = 0;

                for(String s: arrayOfEdge) {
                    String[] anEdge = s.split(",", -2);
                    if(anEdge.length != 2)
                        continue;
                    int a = Integer.parseInt(anEdge[0]);
                    int b = Integer.parseInt(anEdge[1]);
                    Edge an_edge = new Edge(a, b);
                    tempAllEdges.add(an_edge);

                    Edge opposite_edge = new Edge(b, a);
                    tempAllEdges.add(opposite_edge);

                    indexToEdge[a][b] = idx;
                    idx += 1;
                    indexToEdge[b][a] = idx;
                    idx += 1;
                }
                all_edges.add(tempAllEdges);
            }

            if(st.startsWith("cons")) {
                String temp = st.replace("cons=", "");
                String[] arrOfCons = temp.split(">", -2);
                HashMap<Edge, Constraint> constraint = new HashMap<>();
                for(String s: arrOfCons) {
                    s = s.replace("(", "");
                    s = s.replace(")", "");

                    String[] aConstraint = s.split(":", -2);
                    if(aConstraint.length < 2)
                        continue;

                    String[] key = aConstraint[0].split(",");
                    String[] value = aConstraint[1].split(",");
                    int a = Integer.parseInt(key[0].trim());
                    int b = Integer.parseInt(key[1].trim());
                    int c = Integer.parseInt(value[0].trim());
                    int d = Integer.parseInt(value[1].trim());
                    int e = Integer.parseInt(value[2].trim());

                    Edge an_edge = all_edges.get(all_edges.size()-1).get(indexToEdge[a][b]);
                    Constraint a_con = new Constraint(c, d, e);
                    constraint.put(an_edge, a_con);
                }
                all_constraints.add(constraint);
            }
        }
        return new OutputGraph(all_nodes, all_edges, all_constraints, node_number);
    }
}
