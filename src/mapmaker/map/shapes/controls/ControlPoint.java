package mapmaker.map.shapes.controls;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import mapmaker.map.Movable;

public class ControlPoint extends Circle implements Movable {

	Movable movable;
	public ControlPoint(double x,double y,Movable movable) {
		this(x,y);
		this.movable=movable;
	}
	private ControlPoint(double x,double y) {
		super(x,y,5,Color.BLACK);
	}
	
	public void addChangeListener(ChangeListener<Number> handlerx,ChangeListener<Number> handlery) {
		centerXProperty().addListener(handlerx);
		centerYProperty().addListener( handlery);
	}
	
	public void translate( double dx, double dy){
		centerXProperty().set( centerXProperty().get() + dx);
		centerYProperty().set( centerYProperty().get() + dy);
	
	}
	@Override
	public int getID() {
		
		return movable.getID();
	}
	public Movable getParentShape() {
		return movable;
	}
}
