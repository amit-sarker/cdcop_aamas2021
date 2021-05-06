public class DomainPair {
    private double point1;
    private double point2;

    public DomainPair(double point1, double point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    public double getPoint1() {
        return point1;
    }

    public void setPoint1(double point1) {
        this.point1 = point1;
    }

    public double getPoint2() {
        return point2;
    }

    public void setPoint2(double point2) {
        this.point2 = point2;
    }

    @Override
    public String toString() {
        return "DomainPair{" +
                "point1=" + point1 +
                ", point2=" + point2 +
                '}';
    }
}
