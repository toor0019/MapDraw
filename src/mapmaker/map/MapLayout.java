package mapmaker.map;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import mapmaker.map.shapes.controls.ControlPoint;

public class MapLayout extends Pane {

	private ObservableList<Node> children;

	private PolyShape activeShape;
	private Path path;
	private SelectionArea selection;
	private IntegerProperty ID;
	private ObservableList<Node> selected = FXCollections.observableList(new ArrayList<Node>());
	private ObservableList<PolyShape> temp;
	private double startX, startY;
	private ToolState tool;
	private VBox vBox ;
	private ListView<String>listView ;
	private GridPane gridPane;

	private ObservableList<String> jatt;
	private Map<Integer,Integer> listInfo;

	public MapLayout() {
		super();
		
		listInfo= new HashMap<>();
		ID = new SimpleIntegerProperty(0);
		vBox = new VBox() ;
		gridPane = translateToGridPane();;
		listView = new ListView<>();
		
		children = this.getChildren();

		temp = FXCollections.observableList(new ArrayList<PolyShape>());
		for(Node n:children) {
			if(n instanceof PolyShape) {
			temp.add((PolyShape)n)	;
			}
			
		}
		addListChangeListener();
		
		
		intializeListview();
		listView.setOnMouseClicked(e-> {
			vBox.getChildren().remove(gridPane);
			
			gridPane=translateToGridPane(listView.getSelectionModel().getSelectedIndex());
			
			vBox.getChildren().add(gridPane);
		});
		vBox.getChildren().addAll(listView,gridPane);
		tool = ToolState.getToolState();
		children = this.getChildren();
		
		registerMouseEvents();
	}

	private void registerMouseEvents() {
		addEventHandler(MouseEvent.ANY, e-> {
			PolyShape  p=getFirstContain(e);
			if(p!=null) {
				ID.set(p.getID());;
		}});
		addEventHandler(MouseEvent.MOUSE_PRESSED, this::pressClick);
	
		
		addEventHandler(MouseEvent.MOUSE_RELEASED, this::releaseClick);
		addEventHandler(MouseEvent.MOUSE_DRAGGED, this::dragClick);
	}

	private void pressClick(MouseEvent e) {
		e.consume();
		startX = e.getX();
		startY = e.getY();
		switch (activeTool()) {
		case IRREGULAR:
			break;
		case Door:
			break;
		case Move:
			break;
		case Path:
			path = new Path();
			path.setStroke(null);
			path.setStrokeWidth(0);
			children.add(path);
			break;
		case Select:
			selection = SelectionArea.getSelectionArea();
			selection.start(startX, startY);
			if(selected!=null) {
				selected.clear();
			}
			children.add(selection);
			break;
		case Erase:
			break;
		case Room:
			if(tool.getOption()<2) {
				return;
			}
			activeShape = new PolyShape(tool.getOption());
			children.add(activeShape);
			break;
		default:
			throw new UnsupportedOperationException(
					"Cursor for Tool \"" + activeTool().name() + "\" is not implemneted");
		}
	}

	private void dragClick(MouseEvent e) {
		e.consume();
		switch (activeTool()) {
		case IRREGULAR:
			break;
		case Door:
			break;
		case Path:
			path.setStroke(Color.AZURE);
			path.setStrokeWidth(5);
			path.reDraw(startX, startY, e.getX(), e.getY(), true);
		
			break;
		case Erase:
			break;
		case Select:
			selection.end(e.getX(), e.getY());

			break;
		case Move:
			double deltaX=e.getX()-startX;
			double deltaY=e.getY()-startY;
			if(selected!=null&& !(e.getTarget() instanceof Shape) ) {
				selected.stream().forEach(f-> ((Movable)f).translate(deltaX, deltaY));
			}
			
			
			
	
			if ((e.getTarget() instanceof Movable)) {
				selected.clear();
				((Movable) e.getTarget()).translate(deltaX, deltaY);
			}

				
				
			
			
			startX=e.getX();
			startY=e.getY();
			break;
			
		case Room:
			// if you are not using PolyShapeSkeleton2 use line below
			// activeShape.reDraw( startX, startY, distance(startX, startY, e.getX(),
			// e.getY()));
			 activeShape.reDraw(startX, startY, e.getX(), e.getY(), true);
		
			break;
		default:
			throw new UnsupportedOperationException("Drag for Tool \"" + activeTool().name() + "\" is not implemneted");
		}
	}

	private void releaseClick(MouseEvent e) {
		e.consume();
		switch (activeTool()) {
		case IRREGULAR:
		break;
		case Door:
			break;
		case Move:
			
			break;
		case Path:
		
			 path.registerControlPoints();
			children.addAll(path.getControlPoints());
			if(e.getTarget() instanceof PolyShape) {
				path.addLock(((PolyShape) e.getTarget()));
				((PolyShape) e.getTarget()).addLock(path);
			}
			PolyShape p = getFirstContain(e);
			if(p!=null) {
				path.addLock(p);
				p.addLock(path);
			}
			
				path=null;
			break;
		case Select:
			
			children.remove(selection);
			selected.clear();
			selection.containsAny(children,i->selected.add(i));
			selected.stream().filter(g-> g instanceof ControlPoint).forEach(i->((ControlPoint)i).setFill(Color.MEDIUMSLATEBLUE));
			selection.clear();
			break;
		case Erase:
			
			if (!(e.getTarget() instanceof Shape))
				break;
			
			if ((e.getTarget() instanceof PolyShape)) {
			ObservableList<Movable> movable = ((PolyShape) e.getTarget()).getLocks();
			for(Movable m: movable) {
				((PolyShape) m).LOCKS.remove(((PolyShape) e.getTarget()));
				((PolyShape) e.getTarget()).LOCKS.remove(m);
			}
			Node[] nodes = ((PolyShape) e.getTarget()).getControlPoints();
			children.remove(e.getTarget());
			children.removeAll(nodes);}
			if ((e.getTarget() instanceof ControlPoint)) {
				PolyShape parent = (PolyShape) ((ControlPoint) e.getTarget()).getParentShape();
				reDraw(parent,parent.getSides()-1);
				
			}
			break;
		case Room:
			
			
			 activeShape.registerControlPoints();
			
			children.addAll( activeShape.getControlPoints());
			
			break;
		default:
			throw new UnsupportedOperationException(
					"Release for Tool \"" + activeTool().name() + "\" is not implemneted");
		}
		
	}

	private Tools activeTool() {
		return tool.getTool();
	}
	
	public String convertToString(){
		//for each node in children
		return children.stream()
				//filter out any node that is not PolyShape
				.filter( PolyShape.class::isInstance)
				//cast filtered nodes to PolyShapes
				.map( PolyShape.class::cast)
				//convert each shape to a string format
				.map( PolyShape::convertToString)
				//join all string formats together using new line
				.collect( Collectors.joining( System.lineSeparator()));
	}
	
	public void convertFromString( Map< Object, List< String>> map){
		//for each key inside of map
		map.keySet().stream()
		//create a new PolyShape with given list in map
		.map( k->new PolyShape( map.get( k)))
		//for each created PolyShape
		.forEach( s->{
			children.add( s);
			children.addAll( s.getControlPoints());
		});;
	}
	
	public VBox getInformationBox() {
		return vBox;
	}
	
	
	public void clearMap() {
		children.clear();
	}
	
	private PolyShape getFirstContain(MouseEvent e) {
	
		for(Node n:children) {
			if(n.contains(e.getX(), e.getY())) {
				if(n instanceof PolyShape) {
					return ((PolyShape) n);
				}
			}
		}
		return null;
	}
	
	public IntegerProperty getID() {
		return ID;
		 }

	public void addListChangeListener() {
		children.addListener(new ListChangeListener<Node>() {

			@Override
			public void onChanged(Change<? extends Node> c) {
				
				while(c.next()) {
				if(c.wasAdded()) {
					for (Node additem : c.getAddedSubList()) {
						if(additem instanceof PolyShape) {
                        temp.add((PolyShape)additem);
                        listInfo .put(temp.indexOf((PolyShape)additem), ((PolyShape)additem).getID());
						jatt.add(decideName(((PolyShape)additem)));}
                    }
				}
				if(c.wasRemoved()) {
					for (Node additem : c.getRemoved()) {
						if(additem instanceof PolyShape) {
                        temp.remove((PolyShape)additem);
                        listInfo .remove(temp.indexOf((PolyShape)additem), ((PolyShape)additem).getID());
                        jatt.remove(decideName(((PolyShape)additem)));}
                    }
				}
				
				}
			}
		});
	}
	
	public void intializeListview() {
		jatt = FXCollections.observableArrayList();
		for(PolyShape p:temp) {
			jatt.add(decideName(p));
			listInfo.put(temp.indexOf(p), p.getID());
		}
		listView.setItems(jatt);
	}
	private String decideName(PolyShape shape) {
		int sides = shape.getSides();
		if(sides==2) {
			if(shape instanceof Path) {
				return "Path";
			}else return"Line";
		}else if(sides==3) {
			return "Triangle";
		}else if(sides==4) {
			return "Rectangle";
		}else
		if(sides==5) {
			return "Pentagon";
		}else
		if(sides==6) {
			return "Hexagon";
		}
		if(sides>6) {
			return "cUSTOM";
		}
		return null;
	}

	private GridPane translateToGridPane() {
		
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(10,10,10,10));
		gp.setHgap(10);
		gp.setVgap(10);
		Label sides = new Label("Number of sides");
		gp.add(sides, 0, 0);
		Label stroke = new Label("Stroke");
		gp.add(stroke, 0, 1);
		Label fill = new Label("Fill");
		gp.add(fill, 0, 2);
		Label strokeWidth = new Label("StrokeWidth");
		gp.add(strokeWidth, 0, 3);
		Label id = new Label("ID");
		gp.add(id, 0, 4);
		return gp;
	}
	
private GridPane translateToGridPane(int e) {

		PolyShape poly = temp.get(e);
	
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(10,10,10,10));
		gp.setHgap(10);
		gp.setVgap(10);
		Label sides = new Label("Number of sides ");
		TextField sidevalue = new TextField();
		sidevalue.setText(""+poly.getSides());
		sidevalue.textProperty().addListener(
		    I->{
		           if(sidevalue.getText().trim().isEmpty()){}else {
		           if(Integer.parseInt(sidevalue.getText())>=2) {
		        	   
		        	   reDraw(poly,Integer.parseInt(sidevalue.getText()));
		           };
		    }
		    
		    });
		gp.add(sides, 0, 0);
		gp.add(sidevalue, 1, 0);
		Label stroke = new Label("Stroke");
		ColorPicker picker = new ColorPicker();
		picker.setOnAction(g->poly.setStroke(picker.getValue()));
		picker.setValue((Color) poly.getStroke());
		gp.add(picker, 1, 1);
		gp.add(stroke, 0, 1);
		Label fill = new Label("Fill");
		ColorPicker fillpicker = new ColorPicker();
		fillpicker.setValue((Color) poly.getFill());
		fillpicker.setOnAction(g->poly.setFill(fillpicker.getValue()));
		gp.add(fillpicker, 1, 2);
		gp.add(fill, 0, 2);
		Label strokeWidth = new Label("StrokeWidth");
		TextField strokevalue = new TextField();
		strokevalue.setText(""+poly.getStrokeWidth());
		strokevalue.textProperty().addListener(
			    I->{
			           if(strokevalue.getText().trim().isEmpty()){}else {
			          
			        	   
			        	 poly.setStrokeWidth(Double.parseDouble(strokevalue.getText()));
			           };
			    
			    
			    });
		gp.add(strokevalue, 1, 3);
		gp.add(strokeWidth, 0, 3);
		Label id = new Label("ID");
		TextField idvalue = new TextField();
		idvalue.setText(""+poly.getID());
		idvalue.setDisable(true);
		gp.add(idvalue, 1, 4);
		gp.add(id, 0, 4);
		return gp;
	}


public void reDraw(PolyShape parent,int sides) {
	LinkedList<Double> list = parent.getSidesList();
	Paint fill =parent.getFill();
	Paint stroke =parent.getStroke();
	double strokewidth= parent.getStrokeWidth();
	children.removeAll(parent.getControlPoints());
	children.remove(parent);
	activeShape = new PolyShape(sides);
	activeShape.setFill(fill);
	activeShape.setStroke(stroke);
	activeShape.setStrokeWidth(strokewidth);
	children.add(activeShape);
	activeShape.reDraw(list.get(0), list.get(1), list.get(2), list.get(3), false);
	activeShape.registerControlPoints();
	children.addAll(activeShape.getControlPoints());
}


	}

