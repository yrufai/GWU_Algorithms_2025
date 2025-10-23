import java.util.*;

class Point {
    double x, y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}

public class ConvexHullDivideConquer {
    
    /**
     * Main method to compute convex hull
     * Time Complexity: O(n log n)
     */
    public static List<Point> convexHull(List<Point> points) {
        if (points.size() < 3) {
            return new ArrayList<>(points);
        }
        
        // Sort points by x-coordinate (O(n log n))
        List<Point> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> {
            int cmp = Double.compare(p1.x, p2.x);
            return cmp != 0 ? cmp : Double.compare(p1.y, p2.y);
        });
        
        return divideConquer(sorted, 0, sorted.size() - 1);
    }
    
    /**
     * Recursive divide and conquer
     */
    private static List<Point> divideConquer(List<Point> points, int left, int right) {
        int size = right - left + 1;
        
        // Base cases
        if (size <= 3) {
            return computeSmallHull(points, left, right);
        }
        
        // Divide
        int mid = (left + right) / 2;
        List<Point> leftHull = divideConquer(points, left, mid);
        List<Point> rightHull = divideConquer(points, mid + 1, right);
        
        // Conquer - Merge
        return mergeHulls(leftHull, rightHull);
    }
    
    /**
     * Merge two convex hulls
     * Time Complexity: O(n) where n is total points in both hulls
     */
    private static List<Point> mergeHulls(List<Point> leftHull, List<Point> rightHull) {
        // Find upper and lower tangents
        int[] upperTangent = findUpperTangent(leftHull, rightHull);
        int[] lowerTangent = findLowerTangent(leftHull, rightHull);
        
        List<Point> merged = new ArrayList<>();
        
        // Add points from left hull (from upper to lower tangent)
        int current = upperTangent[0];
        int end = lowerTangent[0];
        merged.add(leftHull.get(current));
        
        while (current != end) {
            current = (current + 1) % leftHull.size();
            merged.add(leftHull.get(current));
        }
        
        // Add points from right hull (from lower to upper tangent)
        current = lowerTangent[1];
        end = upperTangent[1];
        merged.add(rightHull.get(current));
        
        while (current != end) {
            current = (current + 1) % rightHull.size();
            merged.add(rightHull.get(current));
        }
        
        return merged;
    }
    
    /**
     * Find upper tangent between two hulls
     */
    private static int[] findUpperTangent(List<Point> leftHull, List<Point> rightHull) {
        int leftIdx = getRightmost(leftHull);
        int rightIdx = getLeftmost(rightHull);
        
        boolean done = false;
        
        while (!done) {
            done = true;
            
            // Move counterclockwise on left hull
            while (true) {
                int nextLeft = (leftIdx + 1) % leftHull.size();
                if (crossProduct(rightHull.get(rightIdx), leftHull.get(leftIdx), 
                                leftHull.get(nextLeft)) >= 0) {
                    break;
                }
                leftIdx = nextLeft;
                done = false;
            }
            
            // Move clockwise on right hull
            while (true) {
                int prevRight = (rightIdx - 1 + rightHull.size()) % rightHull.size();
                if (crossProduct(leftHull.get(leftIdx), rightHull.get(rightIdx),
                                rightHull.get(prevRight)) <= 0) {
                    break;
                }
                rightIdx = prevRight;
                done = false;
            }
        }
        
        return new int[]{leftIdx, rightIdx};
    }
    
    /**
     * Find lower tangent between two hulls
     */
    private static int[] findLowerTangent(List<Point> leftHull, List<Point> rightHull) {
        int leftIdx = getRightmost(leftHull);
        int rightIdx = getLeftmost(rightHull);
        
        boolean done = false;
        
        while (!done) {
            done = true;
            
            // Move clockwise on left hull
            while (true) {
                int prevLeft = (leftIdx - 1 + leftHull.size()) % leftHull.size();
                if (crossProduct(rightHull.get(rightIdx), leftHull.get(leftIdx),
                                leftHull.get(prevLeft)) <= 0) {
                    break;
                }
                leftIdx = prevLeft;
                done = false;
            }
            
            // Move counterclockwise on right hull
            while (true) {
                int nextRight = (rightIdx + 1) % rightHull.size();
                if (crossProduct(leftHull.get(leftIdx), rightHull.get(rightIdx),
                                rightHull.get(nextRight)) >= 0) {
                    break;
                }
                rightIdx = nextRight;
                done = false;
            }
        }
        
        return new int[]{leftIdx, rightIdx};
    }
    
    /**
     * Cross product to determine turn direction
     * Returns: > 0 if left turn, < 0 if right turn, 0 if collinear
     */
    private static double crossProduct(Point p1, Point p2, Point p3) {
        return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x);
    }
    
    /**
     * Handle base cases of 1-3 points
     */
    private static List<Point> computeSmallHull(List<Point> points, int left, int right) {
        List<Point> hull = new ArrayList<>();
        
        if (right - left + 1 == 1) {
            hull.add(points.get(left));
        } else if (right - left + 1 == 2) {
            hull.add(points.get(left));
            hull.add(points.get(right));
        } else { // 3 points
            Point p1 = points.get(left);
            Point p2 = points.get(left + 1);
            Point p3 = points.get(right);
            
            double cp = crossProduct(p1, p2, p3);
            
            if (cp > 0) { // Counterclockwise
                hull.add(p1);
                hull.add(p2);
                hull.add(p3);
            } else if (cp < 0) { // Clockwise - reverse
                hull.add(p1);
                hull.add(p3);
                hull.add(p2);
            } else { // Collinear - return two extreme points
                hull.add(p1);
                hull.add(p3);
            }
        }
        
        return hull;
    }
    
    /**
     * Find index of rightmost point in hull
     */
    private static int getRightmost(List<Point> hull) {
        int idx = 0;
        for (int i = 1; i < hull.size(); i++) {
            if (hull.get(i).x > hull.get(idx).x) {
                idx = i;
            }
        }
        return idx;
    }
    
    /**
     * Find index of leftmost point in hull
     */
    private static int getLeftmost(List<Point> hull) {
        int idx = 0;
        for (int i = 1; i < hull.size(); i++) {
            if (hull.get(i).x < hull.get(idx).x) {
                idx = i;
            }
        }
        return idx;
    }
    
    /**
     * Test driver with timing
     */
    public static void main(String[] args) {
        System.out.println("Convex Hull - Divide and Conquer Algorithm");
        System.out.println("==========================================\n");
        
        // Test with different input sizes
        int[] sizes = {10, 100, 1000, 5000, 10000, 50000, 100000};
        
        System.out.printf("%-12s %-15s %-15s %-15s%n", 
                         "Input Size", "Time (ms)", "Hull Size", "Time/n log n");
        System.out.println("--------------------------------------------------------");
        
        for (int n : sizes) {
            List<Point> points = generateRandomPoints(n);
            
            long startTime = System.nanoTime();
            List<Point> hull = convexHull(points);
            long endTime = System.nanoTime();
            
            double timeMs = (endTime - startTime) / 1_000_000.0;
            double nlogn = n * Math.log(n) / Math.log(2);
            double ratio = timeMs / nlogn;
            
            System.out.printf("%-12d %-15.3f %-15d %-15.6f%n", 
                             n, timeMs, hull.size(), ratio);
        }
        
        // Detailed example with small input
        System.out.println("\n\nDetailed Example:");
        System.out.println("=================");
        List<Point> testPoints = Arrays.asList(
            new Point(0, 0),
            new Point(1, 1),
            new Point(2, 0),
            new Point(2, 2),
            new Point(1, 0.5),
            new Point(0, 2)
        );
        
        System.out.println("Input points:");
        for (Point p : testPoints) {
            System.out.println("  " + p);
        }
        
        List<Point> hull = convexHull(testPoints);
        
        System.out.println("\nConvex hull (counterclockwise):");
        for (Point p : hull) {
            System.out.println("  " + p);
        }
    }
    
    /**
     * Generate random points for testing
     */
    private static List<Point> generateRandomPoints(int n) {
        Random rand = new Random(42); // Fixed seed for reproducibility
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            double x = rand.nextDouble() * 1000;
            double y = rand.nextDouble() * 1000;
            points.add(new Point(x, y));
        }
        
        return points;
    }
}
