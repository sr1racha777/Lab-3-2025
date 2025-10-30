package functions;

public class ArrayTabulatedFunction implements TabulatedFunction {
    private FunctionPoint[] points;      // массив точек функции
    private int pointsCount;             // текущее количество точек
    private static final double EPS = Math.ulp(1.0); // машинный эпсилон для сравнения double

    // кэш последнего обращения
    private int lastAccessedIndex = -1;      // индекс последней использованной точки
    private FunctionPoint lastAccessedPoint = null; // последняя использованная точка

    // конструктор 1: равномерное распределение точек по X
    public ArrayTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX)
            throw new IllegalArgumentException("левая граница >= правая граница");
        if (pointsCount < 2)
            throw new IllegalArgumentException("Кол-во точек < 2");

        this.pointsCount = pointsCount;
        points = new FunctionPoint[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1); // шаг по X между точками
        for (int i = 0; i < pointsCount; i++)
            points[i] = new FunctionPoint(leftX + i * step, 0); // создаем точки с Y=0
    }

    // конструктор 2: по массиву Y-значений
    public ArrayTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX)
            throw new IllegalArgumentException("левая граница >= правая граница");
        if (values.length < 2)
            throw new IllegalArgumentException("Кол-во точек < 2");

        pointsCount = values.length;
        points = new FunctionPoint[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1); // шаг по X между точками
        for (int i = 0; i < pointsCount; i++)
            points[i] = new FunctionPoint(leftX + i * step, values[i]); // создаем точки с заданными Y
    }

    public int getPointsCount() { return pointsCount; } // вернуть текущее количество точек
    public double getLeftDomainBorder() { return points[0].getX(); } // левая граница области определения
    public double getRightDomainBorder() { return points[pointsCount - 1].getX(); } // правая граница области определения

    // получение значения функции в точке x (линейная интерполяция)
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() - EPS || x > getRightDomainBorder() + EPS)
            return Double.NaN; // x вне области определения

        for (int i = 0; i < pointsCount - 1; i++) {
            if (Math.abs(x - points[i].getX()) < EPS)
                return points[i].getY(); // если совпадает с точкой, вернуть её Y

            if (x > points[i].getX() - EPS && x < points[i + 1].getX() + EPS) {
                // линейная интерполяция между соседними точками
                double x1 = points[i].getX();
                double x2 = points[i + 1].getX();
                double y1 = points[i].getY();
                double y2 = points[i + 1].getY();
                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
        }

        if (Math.abs(x - points[pointsCount - 1].getX()) < EPS)
            return points[pointsCount - 1].getY(); // если совпадает с последней точкой

        return Double.NaN; // если x не найден
    }

    // проверка корректности индекса
    private void checkIndex(int index) {
        if (index < 0 || index >= pointsCount)
            throw new FunctionPointIndexOutOfBoundsException("Индекс" + index + "выходит за границы");
    }

    // оптимизированный доступ к точке с использованием кэша
    private FunctionPoint getCachedPoint(int index) {
        checkIndex(index);
        if (index == lastAccessedIndex && lastAccessedPoint != null)
            return lastAccessedPoint; // вернуть кэшированную точку
        lastAccessedIndex = index;
        lastAccessedPoint = points[index]; // обновить кэш
        return lastAccessedPoint;
    }

    public FunctionPoint getPoint(int index) {
        return new FunctionPoint(getCachedPoint(index)); // вернуть копию точки
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        checkIndex(index);
        double x = point.getX();
        if ((index > 0 && x <= points[index - 1].getX()) ||
                (index < pointsCount - 1 && x >= points[index + 1].getX()))
            throw new InappropriateFunctionPointException("X вне порядка"); // проверка порядка X
        points[index] = new FunctionPoint(point); // заменить точку
        lastAccessedIndex = index; // обновить кэш
        lastAccessedPoint = points[index];
    }

    public double getPointX(int index) { return getCachedPoint(index).getX(); } // получить X точки
    public double getPointY(int index) { return getCachedPoint(index).getY(); } // получить Y точки

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        checkIndex(index);
        if ((index > 0 && x <= points[index - 1].getX()) ||
                (index < pointsCount - 1 && x >= points[index + 1].getX()))
            throw new InappropriateFunctionPointException("Х вне порядка"); // проверка порядка X
        points[index].setX(x); // установить новое X
        lastAccessedIndex = index; // обновить кэш
        lastAccessedPoint = points[index];
    }

    public void setPointY(int index, double y) {
        checkIndex(index);
        points[index].setY(y); // установить новое Y
        lastAccessedIndex = index; // обновить кэш
        lastAccessedPoint = points[index];
    }

    // добавление новой точки
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        for (int i = 0; i < pointsCount; i++)
            if (Math.abs(points[i].getX() - point.getX()) < EPS)
                throw new InappropriateFunctionPointException("дубликат Х"); // проверка на дублирование X

        if (pointsCount == points.length) {
            // расширение массива при необходимости
            FunctionPoint[] newPoints = new FunctionPoint[pointsCount + 1];
            System.arraycopy(points, 0, newPoints, 0, pointsCount);
            points = newPoints;
        }

        // найти позицию для вставки
        int index = 0;
        while (index < pointsCount && points[index].getX() < point.getX())
            index++;

        // сдвинуть точки вправо для вставки
        System.arraycopy(points, index, points, index + 1, pointsCount - index);
        points[index] = new FunctionPoint(point); // вставить точку
        pointsCount++;

        lastAccessedIndex = index; // обновить кэш
        lastAccessedPoint = points[index];
    }

    // удаление точки
    public void deletePoint(int index) {
        checkIndex(index);
        if (pointsCount <= 2)
            throw new IllegalStateException("удаление невозможно: кол-во точек < 3"); // минимальное количество точек

        // сдвинуть оставшиеся точки влево
        System.arraycopy(points, index + 1, points, index, pointsCount - index - 1);
        pointsCount--;

        lastAccessedIndex = -1; // сброс кэша
        lastAccessedPoint = null;
    }
}
