package com.daytrip.sunrise.hack.pathfinding;

import java.util.List;

public class PathFindingTest {
    public static void main(String[] args) {
        int[][] map = {
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 1},
                {1, 0, 0, 1, 1},
                {0, 0, 0, 1, 0},
                {1, 1, 0, 0, 1}
        };

        Point start = new Point(0, 0);
        Point end = new Point(3, 4);
        List<Point> path = PathFinder.FindPath(map, start, end);
        if (path != null) {
            for (Point point : path) {
                System.out.println(point);
            }
        }
        else
            System.out.println("No path found");
    }
}
