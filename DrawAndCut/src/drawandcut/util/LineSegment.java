package drawandcut.util;

import javafx.geometry.Point3D;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * @author José Pereda
 */
public class LineSegment {
    /*
    Given one single character in terms of Path, LineSegment stores a list of points that define 
    the exterior of one of its polygons (!isHole). It can contain reference to one or several 
    holes inside this polygon.
    Or it can define the perimeter of a hole (isHole), with no more holes inside.
    */

    private boolean hole;
    private List<Point3D> points;
    private Path path;
    private Point3D origen;
    private List<LineSegment> holes = new ArrayList<>();
    private String letter;

    public LineSegment(String text) {
        letter = text;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public boolean isHole() {
        return hole;
    }

    public void setHole(boolean isHole) {
        this.hole = isHole;
    }

    public List<Point3D> getPoints() {
        return points;
    }

    public void setPoints(List<Point3D> points) {
        this.points = points;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Point3D getOrigen() {
        return origen;
    }

    public void setOrigen(Point3D origen) {
        this.origen = origen;
    }

    public List<LineSegment> getHoles() {
        return holes;
    }

    public void setHoles(List<LineSegment> holes) {
        this.holes = holes;
    }

    public void addHole(LineSegment hole) {
        holes.add(hole);
    }

    @Override
    public String toString() {
        return "Poly{" + "points=" + points + ", path=" + path + ", origen=" + origen + ", holes=" + holes + '}';
    }
}