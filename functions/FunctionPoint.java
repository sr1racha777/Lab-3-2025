package functions;

public class FunctionPoint {
    private double x; //координата по оси x
    private double y; //координата по оси y

    public FunctionPoint(double x, double y) {//конструктор с двумя параметрами
        this.x = x;
        this.y = y;
    }

    public FunctionPoint(FunctionPoint point) {//конструктор копирования
        this.x = point.x;
        this.y = point.y;
    }

    public FunctionPoint() {//конструктор по умолчанию (0;0)
        this.x = 0;
        this.y = 0;
    }

    public double getX() {//геттер для x
        return x;
    }

    public void setX(double x) {//сеттер для x
        this.x = x;
    }

    public double getY() { //геттер для y
        return y;
    }

    public void setY(double y) {//cеттер для y
        this.y = y;
    }

}