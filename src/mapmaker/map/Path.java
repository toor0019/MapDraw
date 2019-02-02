package mapmaker.map;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import mapmaker.map.shapes.controls.ControlPoint;

public class Path extends PolyShape {
	private final ObservableList<PolyShape> LOCKS;
	public Path() {
		
	super(2);
	LOCKS= FXCollections.observableArrayList(new ArrayList<PolyShape>());
	this.setStroke(Color.BLACK);
	this.setFill(Color.GREEN);
	this.setStrokeWidth(8);
	
	}
	
	public void translate(double x,double y) {
		for (Node cp :cPoints) {
			((ControlPoint)cp).translate(x, y);
		}
		
		for (Movable m:LOCKS) {
			
			PolyShape p= (PolyShape)m;
			for (Node cp :p.getControlPoints()) {
				if((cp instanceof ControlPoint)) {
				((ControlPoint)cp).translate(x, y);
				}
			}
			
			for(Movable move: p.getLocks()) {
				if((Arrays.asList(cPoints).contains(move))) {
					move.translate(x, y);
				}
			}
		}
	}
	
	
	public void addLock(PolyShape p) {
		LOCKS.add(p);
	}
	
}
