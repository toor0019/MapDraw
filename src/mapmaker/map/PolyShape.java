package mapmaker.map;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import mapmaker.map.shapes.controls.ControlPoint;

/**
 * http://dimitroff.bg/generating-vertices-of-regular-n-sided-polygonspolyhedra-and-circlesspheres/
 * 
 * @author Shahriar (Shawn) Emami
 * @version Sep 27, 2018
 */
public class PolyShape extends Polygon implements Movable{
	
	private static final String POINTS_COUNT = "sides";
	private static final String FILL = "fill";
	private static final String STROKE = "stroke";
	private static final String WIDTH = "strokeWidth";
	private static final String POINTS = "points";
	private static final String POLYSHAPEID="ployshapeid";
	private static final String LOCK="locks";
	private int id;
	private final ObservableList< Double> POLY_POINTS;
	protected final ObservableList<Movable> LOCKS;
	private int sides;
	private double angle;
	private double dx, dy;
	private double x1, y1;
	private LinkedList<Double> sidesList = new LinkedList<>();
	
	protected ControlPoint[] cPoints;
	private   final Preferences  PREFS = Preferences.userRoot().node(this.getClass().getName());
	private int classID=PREFS.getInt(POLYSHAPEID,0);
	public PolyShape( int sides){
		
		super(sides);
		LOCKS= FXCollections.observableArrayList(new ArrayList<Path>());

		sidesListIntialize();
		id=++classID;
		System.out.println(id);
		PREFS.putInt(POLYSHAPEID, classID);
		POLY_POINTS = getPoints();
		this.sides=sides;
		this.setStroke(Color.BLUE);
		this.setFill(Color.RED);
		this.setStrokeWidth(5);
		
		
		//Initialize sides
	}
	
	public PolyShape( List< String> list){
		this();
		id=++classID;
		
		PREFS.putInt(POLYSHAPEID, classID);
		convertFromString( list);
		
		registerControlPoints();
	}

	public PolyShape() {
		super();
	
		LOCKS= FXCollections.observableArrayList(new ArrayList<Path>());
		sidesListIntialize();
		POLY_POINTS = getPoints();
		// TODO Auto-generated constructor stub
	}

	
	private void cacluatePoints(){
		for( int side = 0; side < sides; side++){
			POLY_POINTS.addAll( point( Math::cos, dx / 2, angle, side, sides) + x1,
					point( Math::sin, dy / 2, angle, side, sides) + y1);
		}
	}

	private double radianShift( double x1, double y1, double x2, double y2){
		return Math.atan2( y2 - y1, x2 - x1);
	}

	private double point( DoubleUnaryOperator operation, double radius, double shift, double side, final int SIDES){
		return radius * operation.applyAsDouble( shift + side * 2.0 * Math.PI / SIDES);
	}

	public void registerControlPoints(){
		cPoints = new ControlPoint[sides];
	
		for(int i=0;i<POLY_POINTS.size();i+=2) {
			
			cPoints[i/2]= new ControlPoint(POLY_POINTS.get(i),POLY_POINTS.get(i+1),this);
			final int j=i;
			cPoints[i/2].addChangeListener((value, oldv,newv)->POLY_POINTS.set(j, newv.doubleValue()),
					(value, oldv,newv)->POLY_POINTS.set(j+1, newv.doubleValue()));
			
			
			
			
		}
		
	}

	
	private double distance( double x1, double y1, double x2, double y2){
		return Math.sqrt( (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	
	
	
	public void reDraw( double x1, double y1, double x2, double y2, boolean symmetrical){
		sidesList.set(0,new Double (x1));
		sidesList.set(1,new Double (y1));
		sidesList.set(2,new Double (x2));
		sidesList.set(3,new Double (y2));
		
		angle = radianShift(x1,y1,x2,y2);
		//using radianShift to measure the drawing angle
		if(symmetrical=true) {
			dx=distance(x1,y1,x2,y2);
			dy=distance(x1,y1,x2,y2);
		}else {
			dx=x2-x1;
			dy=x2-x1;
		}
		//if shape is symmetrical measure the distance between x1,y1 and x2,y2 and assign it to dx and dy
		//if not dx is difference between x1 and x2 and dy is difference between y1 and y2
		//calculate the center of your shape:
		
		this.x1= x1+(x2-x1)/2;
		//x1 is x1 plus half the difference between x1 and x2
		
		this.y1=y1+(y2-y1)/2;
		//y1 is y1 plus half the difference between y1 and y2
		
		POLY_POINTS.clear();
		//clear points
		
		cacluatePoints();
		
		//call calculate
	}

	public ControlPoint[] getcPoints() {
		return cPoints;
	}

	public void setcPoints(ControlPoint[] cPoints) {
		this.cPoints = cPoints;
	}

	public void translate(double deltaX,double deltaY) {
		
		
		
		for(ControlPoint cp:cPoints) {
			cp.translate(deltaX, deltaY);
		}
		
		for(Movable m : LOCKS) {
			m.translate(deltaX, deltaY);
		}
	}
	
	
	public Node[] getControlPoints(){
		return cPoints;
	}
	
	public int getSides() {
		return this.sides;
	}
	
	public String convertToString(){
		String newLine = System.lineSeparator();
		StringBuilder builder = new StringBuilder();
		builder.append( POINTS_COUNT).append( " ").append( sides).append( newLine);
		builder.append( FILL).append( " ").append( colorToString( getFill())).append( newLine);
		builder.append( STROKE).append( " ").append( colorToString( getStroke())).append( newLine);
		builder.append( WIDTH).append( " ").append( getStrokeWidth()).append( newLine);
		
		//join every point in POLY_POINTS and add to builder 
		builder.append( POINTS).append( " ").append( POLY_POINTS.stream().map( e -> Double.toString( e)).collect( Collectors.joining( " "))).append(newLine);
		builder.append( LOCK).append( " ").append( LOCKS.stream().map( e ->Integer.toString(e.getID())).collect( Collectors.joining( " ")));
		return builder.toString();
	}

	/**
	 * <p>
	 * convert array of strings to a PolyShape. called from constructor.</br>
	 * each property is located in one index of the list.</br>
	 * each index starts with a name of property and its value/s in front of it all separated by space.</br>
	 * </p>
	 * @param list - a list of properties for this shape
	 */
	private void convertFromString( List< String> list){
		list.forEach( line -> {
			String[] tokens = line.split( " ");
			switch( tokens[0]){
				case POINTS_COUNT:
					sides = Integer.valueOf( tokens[1]);
					break;
				case FILL:
					setFill( stringToColor( tokens[1], tokens[2]));
					break;
				case STROKE:
					setStroke( stringToColor( tokens[1], tokens[2]));
					break;
				case WIDTH:
					setStrokeWidth( Double.valueOf( tokens[1]));
					break;
				case LOCK:
					
					
				case POINTS:
					//create a stream of line.split( " ") and skip the first element as it is the name, add the rest to POLY_POINTS
					Stream.of( tokens).skip( 1).mapToDouble( Double::valueOf).forEach( POLY_POINTS::add);
				
					break;
				default:
					throw new UnsupportedOperationException( "\"" + tokens[0] + "\" is not supported");
			}
		});
	}

	

	/**
	 * <p>
	 * convert a {@link Paint} to a string in hex format followed by a space and alpha channel.</br>
	 * this method just calls {@link PolyShapeSkeleton2#colorToString(Color)}.</br>
	 * </p>
	 * @param p - paint object to be converted
	 * @return string format of {@link Paint} in hex format plus alpha
	 */
	private String colorToString( Paint p){
		return colorToString( Color.class.cast( p));
	}

	
	private String colorToString( Color c){
		return String.format( "#%02X%02X%02X %f",
				(int) (c.getRed() * 255),
				(int) (c.getGreen() * 255),
				(int) (c.getBlue() * 255),
				c.getOpacity());
	}

	
	private Color stringToColor( String color, String alpha){
		return Color.web( color, Double.valueOf( alpha));
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return id;
	}
	
	public LinkedList<Double> getSidesList() {
		return sidesList;
	}

	public void sidesListIntialize() {
		for(int i=0;i<4;i++) {
		sidesList.add(new Double(0));	
		}
	}
	
	public ObservableList<Movable> getLocks(){
		return LOCKS;
	}
	
	public void addLock(Movable m) {
		LOCKS.add(m);
	}
	
	
	
	
}
