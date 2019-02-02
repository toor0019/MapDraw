package mapmaker;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import mapmaker.map.MapLayout;
import mapmaker.map.ToolState;
import mapmaker.map.Tools;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
public class MapMaker extends Application{

	/**
	 * <p>
	 * these two string represent how regex can allow only reading of decimal or integer numbers.</br>
	 * </p>
	 * @see <a href="https://stackoverflow.com/a/45981297/764951"> how to read only numbers in {@link TextField}</a>
	 */
	public static final String REGEX_DECIMAL = "-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?";
	public static final String REGEX_POSITIVE_INTEGER = "([1-9][0-9]*)";
	
	/**
	 * <p>
	 * this object will be used to check text against given regex.</br>
	 * </p>
	 */
	public static final Pattern P = Pattern.compile( REGEX_POSITIVE_INTEGER);
	
	public static final String MAPS_DIRECTORY = "resources/maps";
	private MapLayout mapLayout ;
	
	public static final String CSS_PATH = "resources/css/style.css";
	public static final String INFO_PATH = "resources/icons/info.txt";
	public static final String HELP_PATH = "resources/icons/help.txt";
	public static final String CREDITS_PATH = "resources/icons/credits.txt";
	private IntegerProperty ID;

	@Override
	public void init() throws Exception{
		super.init();
	}

	@Override
	public void start( Stage primaryStage) throws Exception{
		BorderPane rootPane = new BorderPane();

		MenuBar menuBar = new MenuBar(
				new Menu( "File", null,
						createMenuItem( "Save", ( e) -> {saveMap(primaryStage);}),
						createMenuItem("Load",(e)->loadMap(primaryStage)),
						createMenuItem( "New", ( e) -> {Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to save your Current Map :",ButtonType.YES,ButtonType.NO);
						alert.setTitle("Confirmation");
						alert.showAndWait();
						if(alert.getResult()==ButtonType.YES) {
							saveMap(primaryStage);
							mapLayout.clearMap();
						}else if(alert.getResult()==ButtonType.NO) {
							mapLayout.clearMap();
						}
						}),
						new SeparatorMenuItem(),
						createMenuItem( "Exit", ( e) -> {primaryStage.hide();;})),
				new Menu( "Help", null,
						createMenuItem( "Credit", ( e) -> displayFile("Credit",CREDITS_PATH)),
						createMenuItem( "Info", ( e) -> displayFile("Info",INFO_PATH)),
						new SeparatorMenuItem(),
						createMenuItem( "Help", ( e) -> displayFile("Help",HELP_PATH))));

		rootPane.setTop( menuBar);
		
		Label toolLabel = new Label("Tool:       ");
		Label options = new Label("Options:{}");
		Label space = new Label("      ");
		Label space2 = new Label("      ");
		Label mouseX = new Label("x");
		Label mouseY = new Label("y");
		Label ID_Shape = new Label("ID");
		ToolBar lowerBar = new ToolBar(toolLabel,options,space,mouseX,space2,mouseY,ID_Shape);
		lowerBar.setOrientation(Orientation.HORIZONTAL);
		
		
		rootPane.setBottom(lowerBar);

		MenuButton room = new MenuButton();
		room.setId("Room");
		
		room.getItems().addAll(createToolBarItem("Line",e-> {ToolState.getToolState().setTool(Tools.Room);
														toolLabel.setText("Tool: Room");
														options.setText("Options: {}");
													  ToolState.getToolState().setOption(2);}),
						createToolBarItem("Rectangle",e-> {ToolState.getToolState().setTool(Tools.Room);
														toolLabel.setText("Tool: Room");
														options.setText("Options: {4}");
													  ToolState.getToolState().setOption(4);}),
						createToolBarItem("Triangle",e-> {ToolState.getToolState().setTool(Tools.Room);
						                                toolLabel.setText("Tool: Room");
														options.setText("Options: {3}");
						  								ToolState.getToolState().setOption(3);}),
						createToolBarItem("Pentagon",e-> {ToolState.getToolState().setTool(Tools.Room);
														toolLabel.setText("Tool: Room");
														options.setText("Options: {5}");
						  								ToolState.getToolState().setOption(5);}),
						createToolBarItem("Hexagon",e-> {ToolState.getToolState().setTool(Tools.Room);
						                                toolLabel.setText("Tool: Room");
														options.setText("Options: {6}");
						  								ToolState.getToolState().setOption(6);}),
						createToolBarItem("Irregular",e-> {ToolState.getToolState().setTool(Tools.IRREGULAR);
						                                toolLabel.setText("Tool: iRREGULAR");
						options.setText("Options: {0}");
							}),
						createToolBarItem("CustomPolygon",e-> {ToolState.getToolState().setTool(Tools.Room);
														toolLabel.setText("Tool: Room");
														TextInputDialog alert = new TextInputDialog();
														alert.setTitle("Custom PloyGram");
														alert.setContentText("Enter the number of sides you want :");
														Optional<String> result = alert.showAndWait();
														if(result.isPresent()) {
															options.setText("Options: {"+Integer.parseInt(result.get())+"}");
															ToolState.getToolState().setOption(Integer.parseInt(result.get()));
															
														}
														}));
		
		ToolBar toolbar = new ToolBar(
				createbutton("Select", e-> {ToolState.getToolState().setTool(Tools.Select);
															toolLabel.setText("Tool: Select");
															options.setText("Options:{}");}),
				createbutton("Move", e-> {ToolState.getToolState().setTool(Tools.Move);
															toolLabel.setText("Tool: Move");
															options.setText("Options:{}");}),
				room,
				createbutton("Path", e-> {ToolState.getToolState().setTool(Tools.Path);
															toolLabel.setText("Tool: Path");
															options.setText("Options:{}");}),
				createbutton("Erase", e-> {ToolState.getToolState().setTool(Tools.Erase);
															toolLabel.setText("Tool: Erase");
															options.setText("Options:{}");}),
				createbutton("Door", e-> {ToolState.getToolState().setTool(Tools.Door);
															toolLabel.setText("Tool: Door");
														options.setText("Options:{}");})
				);
		
		toolbar.setOrientation(Orientation.VERTICAL);
		rootPane.setLeft(toolbar);
		
		 mapLayout = new MapLayout();
		 ID=mapLayout.getID();
		 rootPane.setRight(mapLayout.getInformationBox());
		 final ChangeListener changeListener = new ChangeListener() {
		      @Override
		      public void changed(ObservableValue observableValue, Object oldValue,
		          Object newValue) {
		       ID_Shape.setText("ID :"+ID.get());
		      }
		    };
		    ID.addListener(changeListener);
		rootPane.setCenter(mapLayout);
		
		Scene scene = new Scene( rootPane, 800, 600);
		
		scene.getStylesheets().add( new File( CSS_PATH).toURI().toString());
		scene.addEventFilter(MouseEvent.ANY, e->{
			mouseX.setText(""+e.getSceneX());
			mouseY.setText(""+e.getSceneY());
		});
		primaryStage.addEventHandler( KeyEvent.KEY_RELEASED,
				e -> {
					if( e.getCode() == KeyCode.ESCAPE)
						primaryStage.hide();
				});
		primaryStage.setScene( scene);
		primaryStage.setTitle( "Map Maker Skeleton");
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception{
		super.stop();
	}

	private MenuItem createMenuItem( String name, EventHandler< ActionEvent> handler){
		Label icon = new Label();
		icon.setId( name + "-icon");
		MenuItem item = new MenuItem( name, icon);
		item.setId( name);
		item.setOnAction( handler);
		return item;
	}

	private void displayAlert( String title, String header, String content){
		Alert alert = new Alert( AlertType.INFORMATION);
		alert.setTitle( title);
		alert.setHeaderText( header);
		alert.setContentText( content);
		alert.show();
	}

	private String loadFile( String path){
		String str = "";
		try{
			//str = Files.lines(Paths.get(path)).reduce(str,(String a, String b)-> {return a+b;});
			str = Files.lines( Paths.get( path)).reduce( str, ( a, b) -> a + System.lineSeparator() + b);
		}catch( IOException e){
			e.printStackTrace();
		}
		return str;
	}

	private void displayFile(String name,String path){
		displayAlert( name, "Resource "+name, loadFile( path));
	}

	public static void main( String[] args){
		launch( args);
	}
	
	public Button createbutton(String name,EventHandler< ActionEvent> handler ) {
		Label icon = new Label();
		icon.setId( name + "-icon");
		Button item = new Button( " ", icon);
		item.setId( name);
		item.setOnAction( handler);
		return item;
	}
	
	public MenuItem createToolBarItem(String name,EventHandler< ActionEvent> handler) {
		MenuItem item = new MenuItem( name);
		item.setId( name);
		item.setOnAction( handler);
		return item;
	}
	
	private String loadFile( String path, String separator){
		try{
			//for each line in given file combine lines using the separator
			return Files.lines( Paths.get( path)).reduce( "", ( a, b) -> a + separator + b);
		}catch( IOException e){
			e.printStackTrace();
			return "\"" + path + "\" was probably not found" + "\nmessage: " + e.getMessage();
		}
	}
	
	private void saveMap( Stage primary){
		//get the file object to save to
		File file = getFileChooser( primary, true);
		if (file==null)
			return;
		try{
			if( !file.exists())
				file.createNewFile();
			Files.write( file.toPath(), mapLayout.convertToString().getBytes());
		}catch( IOException e){
			e.printStackTrace();
		}
	}
	
	private void loadMap( Stage primary){
		//get the file object to load from
		File file = getFileChooser( primary, false);
		if (file==null || !file.exists())
			return;
		try{
			//no parallel (threading) here but this is safer
			AtomicInteger index = new AtomicInteger(0);  
			//index.getAndIncrement()/5 means every 5 elements increases by 1
			//allowing for every 5 element placed in the same key
			//for each line in file group every 5 and pass to map area
			mapLayout.convertFromString( Files.lines( file.toPath()).collect( Collectors.groupingBy( l->index.getAndIncrement()/5)));
		}catch( IOException e){
			e.printStackTrace();
		}
	}
	
	private File getFileChooser( Stage primary, boolean save){
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add( new ExtensionFilter( "Maps", "*.map"));
		fileChooser.setInitialDirectory( Paths.get( MAPS_DIRECTORY).toFile());
		return save?fileChooser.showSaveDialog( primary):fileChooser.showOpenDialog( primary);
	}

	private void showInputDialog( String title, String content, String match, Consumer<String> callBack){
		TextInputDialog input = new TextInputDialog();
		input.setTitle( title);
		input.setHeaderText( null);
		input.setContentText( content);
		input.getEditor().textProperty().addListener( (value, oldV, newV)->{
			//check if the inputed text matched the given regex
			if(!newV.isEmpty() && !Pattern.matches( match, newV)){
				input.getEditor().setText( oldV);
			}
		});
		//show dialog and wait for an input, if valid call callBack
		input.showAndWait().ifPresent(e->{if(e.matches( match))callBack.accept( e);});
	}

	
}
