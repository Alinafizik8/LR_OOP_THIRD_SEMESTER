package functions;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Iterable<Point>{

    private static class Node {
        double x;
        double y;
        Node next;
        Node prev;

        Node(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private int count = 0;
    private Node head = null; // указатель на голову двусвязного циклического списка

    // <<<<>>>> Добавление узла в конец циклического списка
    private void addNode(double x, double y) {
        Node newNode = new Node(x, y);
        if (head == null) {
            // Первый узел
            head = newNode;
            head.next = head;
            head.prev = head;
        } else {
            // Вставка в конец
            Node last = head.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = head;
            head.prev = newNode;
        }
        count += 1;
    }

    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }

        // Получаем узел для удаления
        Node toRemove = getNode(index);

        // Если в списке только один узел
        if (count == 1) {
            head = null;
            count = 0;
            return;
        }

        // Отвязываем узел от соседей
        toRemove.prev.next = toRemove.next;
        toRemove.next.prev = toRemove.prev;

        // Если удаляем голову — обновляем head
        if (toRemove == head) {
            head = toRemove.next;
        }

        count--;
    }

    // <<<<>>>> Конструктор c двумя параметрами: массивы xValues, yValues
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Таблица должна содержать минимум 2 точки");
        }
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Массивы должны быть одинаковой длины");
        }
        // Проверка на строгую упорядоченность и уникальность x
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new IllegalArgumentException("xValues должны быть строго возрастающими");
            }
        }

        this.head = null;

        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // <<<<>>>> Конструктор с четырьмя параметрами: из функции, интервала и количества точек
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть >= 2");
        }
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        this.head = null;

        if (xFrom == xTo) {
            // Все точки одинаковые
            double y = source.apply(xFrom);
            for (int i = 0; i < count; ++i) {
                addNode(xFrom, y);
            }
        } else {
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; ++i) {
                double x = xFrom + i * step;
                addNode(x, source.apply(x));
            }
        }
    }

    // <<<<>>>> Реализация методов из TabulatedFunction

    public int getCount() {
        return count;
    }

    public double leftBound() {
        return head.x;
    }

    public double rightBound() {
        return head.prev.x;
    }

    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        Node current;
        if (index < count / 2) {
            current = head;
            for (int i = 0; i < index; ++i) current = current.next;
        } else {
            current = head.prev;
            int steps = count - 1 - index;
            for (int i = 0; i < steps; ++i) current = current.prev;
        }
        return current;
    }

    public double getX(int index) {
        return getNode(index).x;
    }

    public double getY(int index) {
        return getNode(index).y;
    }

    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; ++i) {
            if (Math.abs(x - current.x) < 1e-10) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; ++i) {
            if (Math.abs(y - current.y) < 1e-10) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    // <<<<>>>> Реализация абстрактных методов из AbstractTabulatedFunction

    
    protected int floorIndexOfX(double x) {
        if (x < leftBound()) {
            return 0;
        }
        if (x > rightBound()) {
            return count; // ← именно count, как требует задание!
        }
        // x == rightBound() или внутри интервала
        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (current.x <= x && x < current.next.x) {
                return i;
            }
            current = current.next;
        }
        // x == rightBound()
        return count - 1;
    }

    
    protected double extrapolateLeft(double x) {
        // Линейная экстраполяция по первым двум точкам
        double x0 = getX(0), y0 = getY(0);
        double x1 = getX(1), y1 = getY(1);
        return AbstractTabulatedFunction.interpolate(x, x0, x1, y0, y1);
    }

    
    protected double extrapolateRight(double x) {
        // Линейная экстраполяция по последним двум точкам
        double x0 = getX(count - 2), y0 = getY(count - 2);
        double x1 = getX(count - 1), y1 = getY(count - 1);
        return AbstractTabulatedFunction.interpolate(x, x0, x1, y0, y1);
    }

    protected double interpolate(double x, int floorIndex) {
        double x0 = getX(floorIndex);
        double y0 = getY(floorIndex);
        double x1 = getX(floorIndex + 1);
        double y1 = getY(floorIndex + 1);
        return AbstractTabulatedFunction.interpolate(x, x0, x1, y0, y1);
    }

    // <<<<>>>> X*: Оптимизированный apply() без двойного прохода
    
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int idx = indexOfX(x);
            if (idx != -1) {
                return getY(idx);
            } else {
                Node floorNode = floorNodeOfX(x);
                // floorNode — это узел с индексом floorIndex
                // следующий узел — floorNode.next
                return AbstractTabulatedFunction.interpolate(
                        x,
                        floorNode.x, floorNode.next.x,
                        floorNode.y, floorNode.next.y
                );
            }
        }
    }

    // <<<<>>>> Вспомогательный метод для X*
    private Node floorNodeOfX(double x) {
        if (x < leftBound()) {
            return head;
        }
        if (x >= rightBound()) {
            return head.prev;
        }

        Node current = head;
        while (current.next != head) {
            if (current.x <= x && x < current.next.x) {
                return current;
            }
            current = current.next;
        }

        // Теоретически невозможно, но на случай ошибок:
        throw new IllegalStateException("Не удалось найти floorNode для x = " + x);
    }

    public void insert(double x, double y) {
        // Если список пуст — просто добавляем узел
        if (head == null) {
            addNode(x, y);
            return;
        }

        // Проверяем, существует ли уже узел с таким x
        Node current = head;
        do {
            if (Math.abs(current.x - x) < 1e-10) {
                // Нашли — обновляем y и выходим
                current.y = y;
                return;
            }
            current = current.next;
        } while (current != head);

        // x не найден — ищем место для вставки
        Node prev = head.prev; // последний узел
        Node next = head;

        // Случай 1: вставка в начало (x < head.x)
        if (x < head.x) {
            Node newNode = new Node(x, y);
            // Вставка перед head
            newNode.prev = prev;
            newNode.next = next;
            prev.next = newNode;
            next.prev = newNode;
            head = newNode; // обновляем голову
            count++;
            return;
        }

        // Случай 2: вставка в конец (x > last.x)
        if (x > head.prev.x) {
            addNode(x, y); // уже реализовано
            return;
        }

        // Случай 3: вставка в середину
        current = head;
        while (current.next != head) {
            if (current.x < x && x < current.next.x) {
                Node newNode = new Node(x, y);
                newNode.prev = current;
                newNode.next = current.next;
                current.next.prev = newNode;
                current.next = newNode;
                ++count;
                return;
            }
            current = current.next;
        }
    }

    //@Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private Node node = head; // начинаем с головы

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Point point = new Point(node.x, node.y);
                // Если текущий узел — последний, то после него идёт head → завершаем итерацию
                if (node.next == head) {
                    node = null; // конец списка
                } else {
                    node = node.next;
                }
                return point;
            }
        };
    }
}