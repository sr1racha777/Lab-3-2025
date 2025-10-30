import functions.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("ТЕСТИРОВАНИЕ КЛАССОВ TABULATED FUNCTION\n");

        // Проверка конструкторов на некорректные параметры
        System.out.println("Проверка конструкторов на неверные параметры:");
        try {
            TabulatedFunction invalid1 = new ArrayTabulatedFunction(5, 2, new double[]{1, 4, 9});
        } catch (IllegalArgumentException e) {
            System.out.println("Ловим IllegalArgumentException ArrayTabulatedFunction (left >= right): " + e.getMessage());
        }

        try {
            TabulatedFunction invalid2 = new ArrayTabulatedFunction(0, 2, new double[]{1});
        } catch (IllegalArgumentException e) {
            System.out.println("Ловим IllegalArgumentException ArrayTabulatedFunction (points < 2): " + e.getMessage());
        }

        try {
            TabulatedFunction invalid3 = new LinkedListTabulatedFunction(4, 1, new double[]{0, 1, 4});
        } catch (IllegalArgumentException e) {
            System.out.println("Ловим IllegalArgumentException LinkedListTabulatedFunction (left >= right): " + e.getMessage());
        }

        try {
            TabulatedFunction invalid4 = new LinkedListTabulatedFunction(0, 1, new double[]{5});
        } catch (IllegalArgumentException e) {
            System.out.println("Ловим IllegalArgumentException LinkedListTabulatedFunction (points < 2): " + e.getMessage());
        }

        // тестирование ArrayTabulatedFunction
        System.out.println("\nТест: ArrayTabulatedFunction");
        testFunction(new ArrayTabulatedFunction(0, 4, new double[]{0, 1, 4, 9, 16}));

        // тестирование LinkedListTabulatedFunction
        System.out.println("\nТест: LinkedListTabulatedFunction");
        testFunction(new LinkedListTabulatedFunction(0, 4, new double[]{0, 1, 4, 9, 16}));

        System.out.println("\nВСЕ ТЕСТЫ ЗАВЕРШЕНЫ");
    }

    private static void testFunction(TabulatedFunction func) {
        System.out.println("Тип функции: " + func.getClass().getSimpleName());
        System.out.println();

        // 1. Проверка вычисления значений функции
        System.out.println("1. Проверяем значения функции:");
        for (double x = 1; x <= 5; x += 0.5) {
            double y = func.getFunctionValue(x);
            System.out.printf("f(%.1f) = %s\n", x, Double.isNaN(y) ? "NaN" : y);
        }

        // 2. Проверка доступа к точкам
        System.out.println("\n2. Проверяем доступ к точкам:");
        try {
            System.out.println("Количество точек: " + func.getPointsCount());
            for (int i = 0; i < func.getPointsCount(); i++) {
                FunctionPoint p = func.getPoint(i);
                System.out.printf("Точка %d: (%.2f, %.2f)\n", i, p.getX(), p.getY());
            }
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ошибка доступа к точке: " + e.getMessage());
        }

        // 3. Проверка ошибок индекса для всех методов
        System.out.println("\n3. Проверяем ошибки индекса:");
        try {
            func.getPoint(-1);
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException getPoint(-1): " + e.getMessage());
        }

        try {
            func.setPoint(func.getPointsCount(), new FunctionPoint(0, 0));
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException setPoint(index==count): " + e.getMessage());
        }

        try {
            func.getPointX(func.getPointsCount());
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException getPointX: " + e.getMessage());
        }

        try {
            func.setPointX(-1, 0);
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException setPointX: " + e.getMessage());
        }

        try {
            func.getPointY(func.getPointsCount());
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException getPointY: " + e.getMessage());
        }

        try {
            func.setPointY(-1, 0);
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException setPointY: " + e.getMessage());
        }

        try {
            func.deletePoint(func.getPointsCount());
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("Ловим FunctionPointIndexOutOfBoundsException deletePoint: " + e.getMessage());
        }

        // 4. Проверка нарушений порядка X
        System.out.println("\n4. Проверяем нарушение порядка X:");
        try {
            FunctionPoint bad = new FunctionPoint(100, 0);
            func.setPoint(1, bad);
        } catch (InappropriateFunctionPointException e) {
            System.out.println("Ловим InappropriateFunctionPointException setPoint: " + e.getMessage());
        }

        try {
            func.setPointX(1, func.getPointX(0) - 1);
        } catch (InappropriateFunctionPointException e) {
            System.out.println("Ловим InappropriateFunctionPointException setPointX: " + e.getMessage());
        }

        // 5. Проверка добавления точки с дублирующим X
        System.out.println("\n5. Проверяем добавление точки с повторяющимся X:");
        try {
            double existingX = func.getPointX(2);
            func.addPoint(new FunctionPoint(existingX, 123));
        } catch (InappropriateFunctionPointException e) {
            System.out.println("Ловим InappropriateFunctionPointException (дубль X): " + e.getMessage());
        }

        // 6. Проверка удаления точки при <2 точках
        System.out.println("\n6. Проверяем удаление, если останется <2 точек:");
        try {
            TabulatedFunction small = new ArrayTabulatedFunction(0, 1, new double[]{0, 1});
            small.deletePoint(0);
        } catch (IllegalStateException e) {
            System.out.println("Ловим IllegalStateException deletePoint: " + e.getMessage());
        }

        // 7. Дополнительно проверяем методы интерфейса
        System.out.println("\n7. Проверяем getPointX/getPointY и setPointY:");
        for (int i = 0; i < func.getPointsCount(); i++) {
            System.out.printf("Точка %d: x=%.2f, y=%.2f\n", i, func.getPointX(i), func.getPointY(i));
        }

        System.out.println("\nИзменяем Y у точки 2 на 100");
        func.setPointY(2, 100);
        System.out.printf("Новая точка 2: (%.2f, %.2f)\n", func.getPointX(2), func.getPointY(2));

        // 8. Успешное изменение X точки
        System.out.println("\nИзменяем X у точки 2 на 2.2");
        func.setPointX(2, 2.2);
        System.out.printf("Новая точка 2: (%.2f, %.2f)\n", func.getPointX(2), func.getPointY(2));

        // 9. Успешное addPoint
        System.out.println("\nДобавляем новую точку (2.5, 50)");
        func.addPoint(new FunctionPoint(2.5, 50));
        for (int i = 0; i < func.getPointsCount(); i++) {
            FunctionPoint p = func.getPoint(i);
            System.out.printf("Точка %d: (%.2f, %.2f)\n", i, p.getX(), p.getY());
        }

        // 10. Успешное deletePoint
        System.out.println("\nУдаляем точку с индексом 2");
        func.deletePoint(2);
        for (int i = 0; i < func.getPointsCount(); i++) {
            FunctionPoint p = func.getPoint(i);
            System.out.printf("Точка %d: (%.2f, %.2f)\n", i, p.getX(), p.getY());
        }

        // 11. Успешное setPoint
        System.out.println("\nУстанавливаем точку 1 на (1.5, 75)");
        func.setPoint(1, new FunctionPoint(1.5, 75));
        for (int i = 0; i < func.getPointsCount(); i++) {
            FunctionPoint p = func.getPoint(i);
            System.out.printf("Точка %d: (%.2f, %.2f)\n", i, p.getX(), p.getY());
        }

        System.out.println("\nПроверка " + func.getClass().getSimpleName() + " завершена успешно!\n");
    }
}
