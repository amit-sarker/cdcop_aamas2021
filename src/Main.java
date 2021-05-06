import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException, InterruptedException {
        int MAX_SAME_PROB = 1;    //Specify number of same problem you want to run
        int MAX_DIFF_PROB = 1;    //Specify number of different problems you want to eun
        Problem p1 = new Problem(MAX_SAME_PROB, MAX_DIFF_PROB, 3, -50, 50, 0);
        p1.newProblem();
    }
}