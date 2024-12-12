import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {
    private JFrame frame;
    private JPanel gridPanel;
    private JTextField rowsField;
    private JTextField colsField;
    private JButton runButton, clearButton, goalButton, setStartButton, startButton;
    private JComboBox<String> algorithmComboBox;
    private JTextArea stepsDisplay;
    private JButton[][] gridButtons;
    private Color blockColor = Color.LIGHT_GRAY;
    private Color barrierColor = Color.BLACK;
    private Color goalColor = Color.RED;
    private Color startColor = Color.BLUE;
    private Color pathColor = Color.GREEN;
    private int rows, cols;
    private Point start = null;
    private Point goal = null;
    private static final String DFS = "DFS";
    private static final String BFS = "BFS";
    private static final String ASTAR = "A*";

    public Main() {
        frame = new JFrame("Pathfinding Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Left Panel for Input and Controls
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        rowsField = new JTextField("");
        colsField = new JTextField("");
        Dimension textFieldSize = new Dimension(100, 30);
        rowsField.setPreferredSize(textFieldSize);
        colsField.setPreferredSize(textFieldSize);
        inputPanel.add(rowsField);
        inputPanel.add(colsField);
        leftPanel.add(inputPanel);

        // Algorithm selection (fixed size)
        algorithmComboBox = new JComboBox<>(new String[] { DFS, BFS, ASTAR });
        algorithmComboBox.setPreferredSize(new Dimension(150, 30));
        leftPanel.add(algorithmComboBox);


        // Steps display
        stepsDisplay = new JTextArea(5, 20);
        stepsDisplay.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(stepsDisplay);
        leftPanel.add(scrollPane);

        frame.add(leftPanel, BorderLayout.WEST);

        // Right Panel for Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        runButton = new JButton("Run");
        clearButton = new JButton("Clear");
        goalButton = new JButton("Set Goal");
        setStartButton = new JButton("Set Start");
        startButton = new JButton("Start");
        Dimension buttonSize = new Dimension(100, 40);
        runButton.setPreferredSize(buttonSize);
        clearButton.setPreferredSize(buttonSize);
        goalButton.setPreferredSize(buttonSize);
        setStartButton.setPreferredSize(buttonSize);
        startButton.setPreferredSize(buttonSize);
        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(setStartButton);
        buttonPanel.add(goalButton);
        buttonPanel.add(startButton);
        frame.add(buttonPanel, BorderLayout.EAST);

        // Center Panel for Grid
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(1, 1));
        frame.add(gridPanel, BorderLayout.CENTER);

        // Button Actions
        runButton.addActionListener(e -> createGrid());
        clearButton.addActionListener(e -> clearGrid());
        goalButton.addActionListener(e -> enableGoalSetting());
        setStartButton.addActionListener(e -> enableStartSetting());
        startButton.addActionListener(e -> findPath());

        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private void createGrid() {
        try {
            rows = Integer.parseInt(rowsField.getText());
            cols = Integer.parseInt(colsField.getText());

            gridPanel.removeAll();
            gridPanel.setLayout(new GridLayout(rows, cols));

            gridButtons = new JButton[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    JButton button = new JButton();
                    button.setBackground(blockColor);
                    button.setOpaque(true);
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    final int x = i, y = j;
                    button.addActionListener(evt -> toggleBarrier(button, x, y));

                    gridButtons[i][j] = button;
                    gridPanel.add(button);
                }
            }
            start = null;
            goal = null;
            frame.revalidate();
            frame.repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for rows and columns.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleBarrier(JButton button, int x, int y) {
        if (button.getBackground().equals(blockColor)) {
            button.setBackground(barrierColor);
        } else if (!button.getBackground().equals(goalColor) && !button.getBackground().equals(startColor)) {
            button.setBackground(blockColor);
        }
    }

    private void clearGrid() {
        if (gridButtons != null) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    gridButtons[i][j].setBackground(blockColor);
                }
            }
            start = null;
            goal = null;
        }
    }

    private void enableGoalSetting() {
        if (gridButtons != null) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    final int x = i, y = j;
                    gridButtons[i][j].addActionListener(evt -> setGoal(x, y));
                }
            }
        }
    }

    private void setGoal(int x, int y) {
        if (gridButtons[x][y].getBackground().equals(blockColor)) {
            if (goal != null) {
                gridButtons[goal.x][goal.y].setBackground(blockColor);
            }
            gridButtons[x][y].setBackground(goalColor);
            goal = new Point(x, y);
        }
    }

    private void enableStartSetting() {
        if (gridButtons != null) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    final int x = i, y = j;
                    gridButtons[i][j].addActionListener(evt -> setStart(x, y));
                }
            }
        }
    }

    private void setStart(int x, int y) {
        if (gridButtons[x][y].getBackground().equals(blockColor)) {
            if (start != null) {
                gridButtons[start.x][start.y].setBackground(blockColor);
            }
            gridButtons[x][y].setBackground(startColor);
            start = new Point(x, y);
        }
    }

    private void findPath() {
        if (start == null || goal == null) {
            JOptionPane.showMessageDialog(frame, "Please set a start and goal point.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clearPath();

        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        List<Point> path = null;
        int steps = 0;

        if (selectedAlgorithm.equals(ASTAR)) {
            path = aStarPathfinding(start, goal);
        } else if (selectedAlgorithm.equals(BFS)) {
            path = bfsPathfinding(start, goal);
        } else if (selectedAlgorithm.equals(DFS)) {
            path = dfsPathfinding(start, goal);
        }

        if (path == null) {
            JOptionPane.showMessageDialog(frame, "No path found.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Point point : path) {
                if (!point.equals(start) && !point.equals(goal)) {
                    gridButtons[point.x][point.y].setBackground(pathColor);
                }
            }
            steps = path.size();
            stepsDisplay.setText("Steps to Goal: " + steps);
        }
    }

    private void clearPath() {
        if (gridButtons != null) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (gridButtons[i][j].getBackground().equals(pathColor)) {
                        gridButtons[i][j].setBackground(blockColor);
                    }
                }
            }
        }
    }


    private List<Point> aStarPathfinding(Point start, Point goal) {

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.position.equals(goal)) {
                return reconstructPath(current);
            }

            closedSet.add(current.position);

            for (Point neighbor : getNeighbors(current.position)) {
                if (closedSet.contains(neighbor)) continue;

                int tentativeG = current.g + 1;
                Node neighborNode = allNodes.get(neighbor);
                if (neighborNode == null || tentativeG < neighborNode.g) {
                    int f = tentativeG + heuristic(neighbor, goal);
                    Node newNeighbor = new Node(neighbor, current, tentativeG, f);
                    openSet.add(newNeighbor);
                    allNodes.put(neighbor, newNeighbor);
                }
            }
        }

        return null;
    }

    private List<Point> bfsPathfinding(Point start, Point goal) {
        Queue<Node> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        queue.add(new Node(start, null, 0, 0));
        visited.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.position.equals(goal)) {
                return reconstructPath(current);
            }

            for (Point neighbor : getNeighbors(current.position)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new Node(neighbor, current, current.g + 1, 0));
                }
            }
        }

        return null;
    }

    private List<Point> dfsPathfinding(Point start, Point goal) {
        Stack<Node> stack = new Stack<>();
        Set<Point> visited = new HashSet<>();
        stack.push(new Node(start, null, 0, 0));
        visited.add(start);

        while (!stack.isEmpty()) {
            Node current = stack.pop();

            if (current.position.equals(goal)) {
                return reconstructPath(current);
            }


            List<Point> neighbors = getNeighbors(current.position);
            Collections.reverse(neighbors);

            for (Point neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    stack.push(new Node(neighbor, current, current.g + 1, 0));
                }
            }
        }

        return null;
    }


    private List<Point> getNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newX = point.x + dx[i];
            int newY = point.y + dy[i];
            if (newX >= 0 && newX < rows && newY >= 0 && newY < cols) {
                if (!gridButtons[newX][newY].getBackground().equals(barrierColor)) {
                    neighbors.add(new Point(newX, newY));
                }
            }
        }

        return neighbors;
    }


    private int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Point> reconstructPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(node.position);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private static class Node {
        Point position;
        Node parent;
        int g;
        int f;

        Node(Point position, Node parent, int g, int f) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }
    }
}
