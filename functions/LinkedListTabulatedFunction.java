package functions;

public class LinkedListTabulatedFunction implements TabulatedFunction {
    // вложенный класс узла списка
    private static class FunctionNode {
        FunctionPoint point;     // точка функции
        FunctionNode next;       // ссылка на следующий узел
        FunctionNode prev;       // ссылка на предыдущий узел
        FunctionNode(FunctionPoint p) { point = p; }
    }

    private final FunctionNode head = new FunctionNode(null); // фиктивный узел (голова)
    private int pointsCount;                                    // текущее количество точек
    private static final double EPS = Math.ulp(1.0);           // машинный эпсилон для сравнения double

    // поля для кэширования последнего использованного узла
    private FunctionNode lastAccessedNode = null;
    private int lastAccessedIndex = -1;

    // конструктор 1: равномерное распределение точек по X
    public LinkedListTabulatedFunction(double leftX, double rightX, int count) {
        if (leftX >= rightX)
            throw new IllegalArgumentException("левый >= правый");
        if (count < 2)
            throw new IllegalArgumentException("кол-во точек < 2");

        head.next = head.prev = head;  // инициализация пустого кольцевого списка
        pointsCount = 0;
        double step = (rightX - leftX) / (count - 1); // шаг по X между точками
        for (int i = 0; i < count; i++)
            addNodeToTail().point = new FunctionPoint(leftX + i * step, 0); // создаем точки с Y=0
    }

    // конструктор 2: по массиву Y-значений
    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX)
            throw new IllegalArgumentException("левый >= правый");
        if (values.length < 2)
            throw new IllegalArgumentException("кол-во точек < 2");

        head.next = head.prev = head;  // инициализация пустого кольцевого списка
        pointsCount = 0;
        double step = (rightX - leftX) / (values.length - 1); // шаг по X между точками
        for (int i = 0; i < values.length; i++)
            addNodeToTail().point = new FunctionPoint(leftX + i * step, values[i]); // создаем точки с заданными Y
    }

    // добавление узла в конец списка
    private FunctionNode addNodeToTail() {
        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0)); // создаем новый узел

        if (head.next == head) { // если список пустой
            head.next = head.prev = newNode;
            newNode.next = newNode.prev = head;
        } else { // вставка в конец
            newNode.next = head;
            newNode.prev = head.prev;
            head.prev.next = newNode;
            head.prev = newNode;
        }

        pointsCount++;                  // обновляем количество точек
        lastAccessedNode = newNode;     // обновляем кэш
        lastAccessedIndex = pointsCount - 1;
        return newNode;
    }

    // добавление узла по индексу
    private FunctionNode addNodeByIndex(int index) {
        if (index < 0 || index > pointsCount)
            throw new FunctionPointIndexOutOfBoundsException("индекс" + index + "вне допустимого диапазона для вставки");

        if (index == pointsCount)           // добавление в конец
            return addNodeToTail();

        FunctionNode currentNode = getNodeByIndex(index);  // узел, перед которым вставляем
        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0));

        // вставка нового узла перед currentNode
        newNode.prev = currentNode.prev;
        newNode.next = currentNode;
        currentNode.prev.next = newNode;
        currentNode.prev = newNode;

        pointsCount++;                     // обновляем количество точек
        if (lastAccessedIndex >= index)    // корректируем кэш
            lastAccessedIndex++;
        lastAccessedNode = newNode;

        return newNode;
    }

    // удаление узла по индексу
    private FunctionNode deleteNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount)
            throw new FunctionPointIndexOutOfBoundsException("индекс" + index + "вне допустимого диапазона");
        if (pointsCount <= 2)
            throw new IllegalStateException("удаление невозможно: кол-во точек меньше 3"); // минимум 2 точки оставлять нельзя

        FunctionNode nodeToDelete = getNodeByIndex(index);

        // переподключение соседних узлов
        nodeToDelete.prev.next = nodeToDelete.next;
        nodeToDelete.next.prev = nodeToDelete.prev;

        if (nodeToDelete == head.next)
            head.next = nodeToDelete.next;

        pointsCount--;                     // обновляем количество точек

        // обновление кэша
        if (lastAccessedIndex == index) {
            lastAccessedNode = null;
            lastAccessedIndex = -1;
        } else if (lastAccessedIndex > index) {
            lastAccessedIndex--;
        }

        return nodeToDelete;
    }

    // получение узла по индексу с кэшем
    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount)
            throw new FunctionPointIndexOutOfBoundsException("индекс " + index + " вне диапазона");

        // если есть кэш и расстояние до него небольшое, идем от кэша
        if (lastAccessedNode != null && Math.abs(index - lastAccessedIndex) <= pointsCount / 2) {
            FunctionNode node = lastAccessedNode;
            if (index > lastAccessedIndex) {
                for (int i = lastAccessedIndex; i < index; i++)
                    node = node.next;
            } else if (index < lastAccessedIndex) {
                for (int i = lastAccessedIndex; i > index; i--)
                    node = node.prev;
            }
            lastAccessedNode = node;
            lastAccessedIndex = index;
            return node;
        }

        // обычный обход от головы
        FunctionNode node = head.next;
        for (int i = 0; i < index; i++)
            node = node.next;

        lastAccessedNode = node; // обновляем кэш
        lastAccessedIndex = index;
        return node;
    }

    public int getPointsCount() { return pointsCount; } // вернуть количество точек
    public double getLeftDomainBorder() { return head.next.point.getX(); }  // левая граница области определения
    public double getRightDomainBorder() { return head.prev.point.getX(); } // правая граница области определения

    // получение значения функции в точке x (линейная интерполяция)
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() - EPS || x > getRightDomainBorder() + EPS)
            return Double.NaN; // x вне области определения

        FunctionNode node = head.next;
        while (node.next != head) { // проход по списку
            double x1 = node.point.getX();
            double x2 = node.next.point.getX();
            if (Math.abs(x - x1) < EPS) return node.point.getY(); // совпадение с узлом
            if (x > x1 - EPS && x < x2 + EPS) // линейная интерполяция
                return node.point.getY() + (node.next.point.getY() - node.point.getY()) * (x - x1) / (x2 - x1);
            node = node.next;
        }
        if (Math.abs(x - head.prev.point.getX()) < EPS)
            return head.prev.point.getY(); // совпадение с последним узлом
        return Double.NaN; // значение вне диапазона
    }

    public FunctionPoint getPoint(int index) {
        return new FunctionPoint(getNodeByIndex(index).point); // вернуть копию точки
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);
        double x = point.getX();
        if ((node.prev != head && x <= node.prev.point.getX()) ||
                (node.next != head && x >= node.next.point.getX()))
            throw new InappropriateFunctionPointException("Х вне порядка"); // проверка порядка X
        node.point = new FunctionPoint(point); // установка новой точки
    }

    public double getPointX(int index) { return getNodeByIndex(index).point.getX(); } // получить X точки

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);
        if ((node.prev != head && x <= node.prev.point.getX()) ||
                (node.next != head && x >= node.next.point.getX()))
            throw new InappropriateFunctionPointException("Х вне порядка"); // проверка порядка X
        node.point.setX(x); // установить новое X
    }

    public double getPointY(int index) { return getNodeByIndex(index).point.getY(); } // получить Y точки

    public void setPointY(int index, double y) { getNodeByIndex(index).point.setY(y); } // установить Y

    // добавление новой точки с проверкой на дублирование X
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        FunctionNode node = head.next;
        while (node != head) {
            if (Math.abs(node.point.getX() - point.getX()) < EPS)
                throw new InappropriateFunctionPointException("дубликат Х"); // проверка на дублирование
            node = node.next;
        }

        FunctionNode newNode = new FunctionNode(new FunctionPoint(point));
        if (head.next == head) { // если список пустой
            head.next = head.prev = newNode;
            newNode.next = newNode.prev = head;
        } else { // вставка в правильное место по X
            node = head.next;
            while (node != head && node.point.getX() < point.getX())
                node = node.next;
            newNode.prev = node.prev;
            newNode.next = node;
            node.prev.next = newNode;
            node.prev = newNode;
        }
        pointsCount++; // обновляем количество точек
    }

    public void deletePoint(int index) {
        deleteNodeByIndex(index); // удаление точки через приватный метод
    }
}
