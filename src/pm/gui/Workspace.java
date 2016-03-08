package pm.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Ellipse;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import properties_manager.PropertiesManager;
import saf.ui.AppGUI;
import saf.AppTemplate;
import saf.components.AppWorkspaceComponent;

/**
 * This class serves as the workspace component for this application, providing
 * the user interface controls for editing work.
 *
 * @author Richard McKenna
 * @author Kenneth Chiguichon
 * @version 1.0
 */
public class Workspace extends AppWorkspaceComponent {

    // HERE'S THE APP
    AppTemplate app;

    // IT KNOWS THE GUI IT IS PLACED INSIDE
    AppGUI gui;
    
    Pane appDrawSpace;
    SplitPane appDrawSpaceContainer;
    VBox sideToolbar;
    
    BorderPane centerWorkspace;
    
    HBox shapeSelectorControlSet;
    HBox shapeHeirarchyControlSet;
    VBox backgroundColorMenu;
    VBox fillColorMenu;
    VBox outlineColorMenu;
    VBox outlineThicknessMenu;
    VBox snapshotMenu;
    
    boolean isDrawEnabled = false;
    boolean isShapeSelected = false;
    
    
    
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.valueOf("#ffe4c4");
    private static final Color DEFAULT_FILL_COLOR = Color.valueOf("#ff6666");
    private static final Color DEFAULT_OUTLINE_COLOR = Color.valueOf("#99cc99");
    private static Slider outlineThicknessSlider;
    private MouseState currentMouseState = MouseState.SELECTOR;
    private Shape selectedShape;
    
    public ArrayList<ColorPicker> activeColors;
    public Stack<Shape> shapeStack;
    public ArrayList<Button> shapeManipulators;
    public double currentOutlineThickness = 5;
    
    
    public static enum MouseState{
        SELECTOR,
        REMOVAL,
        CREATE_RECT,
        CREATE_ELLIPSE
    };
    
    //Use these enums with the folowing syntax:
    //[ENUM].[KEY].ordinal();
    
    public static enum ShapeManipulatorIndex {
        SHAPESELECTOR,
        REMOVE,
        RECT,
        ELLIPSE,
        SENDTOFRONT,
        SENDTOBACK
    };
    
    public static enum ColorPickerIndex {
        BACKGROUNDCOLOR,
        FILLCOLOR,
        OUTLINECOLOR
    };
    

    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     *
     * @throws IOException Thrown should there be an error loading application
     * data for setting up the user interface.
     */
    public Workspace(AppTemplate initApp) throws IOException {
        // KEEP THIS FOR LATER
	app = initApp;

	// KEEP THE GUI FOR LATER
	gui = app.getGUI();

	// THIS WILL PROVIDE US WITH OUR CUSTOM UI SETTINGS AND TEXT
	PropertiesManager propsSingleton = PropertiesManager.getPropertiesManager();
        
        gui.getWindow().setResizable(false);
        
	// WE'LL ORGANIZE OUR WORKSPACE COMPONENTS USING A BORDER PANE
	workspace = new BorderPane();
        activeColors = new ArrayList();
        shapeStack = new Stack<>();
        shapeManipulators = new ArrayList();
        appDrawSpace = new Pane();
        sideToolbar = new VBox();
        
        //Set up the controls for selecting/creating/destroying shapes
        shapeSelectorControlSet = new HBox(10);
        Button shapeSelector = new Button();
        Button removeButton = new Button();
        Button createRectButton = new Button();
        Button createEllipseButton = new Button();
        
        //Setting the images for all selector/creation/removal buttons
        shapeSelector.setGraphic(new ImageView(new Image("file:images\\SelectionTool.png")));
        removeButton.setGraphic(new ImageView(new Image("file:images\\Remove.png")));
        createRectButton.setGraphic(new ImageView(new Image("file:images\\Rect.png")));
        createEllipseButton.setGraphic(new ImageView(new Image("file:images\\Ellipse.png")));
        
        //Add the buttons to the list of shape manipulators
        shapeManipulators.add(shapeSelector);
        shapeManipulators.add(removeButton);
        shapeManipulators.add(createRectButton);
        shapeManipulators.add(createEllipseButton);
        
        //Add Button Functionality Structure
        shapeSelector.setOnAction(e -> {
            currentMouseState = MouseState.SELECTOR;
            reloadWorkspace();
        });
        removeButton.setOnAction(e -> {
            currentMouseState = MouseState.REMOVAL;
            reloadWorkspace();
        });
        createRectButton.setOnAction(e -> {
            currentMouseState = MouseState.CREATE_RECT;
            reloadWorkspace();
        });
        createEllipseButton.setOnAction(e -> {
            currentMouseState = MouseState.CREATE_ELLIPSE;
            reloadWorkspace();
        });
        
        //Add the buttons to the GUI
        shapeSelectorControlSet.getChildren().add(shapeSelector);
        shapeSelectorControlSet.getChildren().add(removeButton);
        shapeSelectorControlSet.getChildren().add(createRectButton);
        shapeSelectorControlSet.getChildren().add(createEllipseButton);
        
        //Set up the shape heirarchy control set
        shapeHeirarchyControlSet = new HBox(10);
        shapeHeirarchyControlSet.alignmentProperty().setValue(Pos.CENTER);
        Button sendToBackButton = new Button();
        Button sendToFrontButton = new Button();
        
        //Setting up the images for the shape heirarchy buttons
        sendToBackButton.setGraphic(new ImageView("file:images\\MoveToBack.png"));
        sendToFrontButton.setGraphic(new ImageView("file:images\\MoveToFront.png"));
        
        sendToBackButton.setOnAction(e -> {
            if(selectedShape != null){
                selectedShape.toBack();
                shapeStack.remove(selectedShape);
                shapeStack.add(0, selectedShape);
            }
        });
        
        sendToFrontButton.setOnAction(e -> {
            if(selectedShape != null){
                selectedShape.toFront();
                shapeStack.remove(selectedShape);
                shapeStack.push(selectedShape);
            }
        });
        
        shapeManipulators.add(sendToBackButton);
        shapeManipulators.add(sendToFrontButton);
        
        //Add Buttons to GUI
        shapeHeirarchyControlSet.getChildren().add(sendToBackButton);
        shapeHeirarchyControlSet.getChildren().add(sendToFrontButton);
        
        //Setting up the background color menu
        backgroundColorMenu = new VBox(10);
        Label backgroundColorLabel = new Label("Background Color");
        backgroundColorLabel.getStyleClass().add("subheading_label");
        ColorPicker backgroundColorPicker = new ColorPicker(DEFAULT_BACKGROUND_COLOR);
        backgroundColorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
        
        //Add the controls to the control menu
        backgroundColorMenu.getChildren().add(backgroundColorLabel);
        backgroundColorMenu.getChildren().add(backgroundColorPicker);
        
        //Setting up fill color menu
        fillColorMenu = new VBox(10);
        Label fillColorLabel = new Label("Fill Color");
        fillColorLabel.getStyleClass().add("subheading_label");
        ColorPicker fillColorPicker = new ColorPicker(DEFAULT_FILL_COLOR);
        fillColorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
        
        //Add the controls to the control menu
        fillColorMenu.getChildren().add(fillColorLabel);
        fillColorMenu.getChildren().add(fillColorPicker);
        
        //Setting up outline color menu
        outlineColorMenu = new VBox(10);
        Label outlineColorLabel = new Label("Outline Color");
        outlineColorLabel.getStyleClass().add("subheading_label");
        ColorPicker outlineColorPicker = new ColorPicker(DEFAULT_OUTLINE_COLOR);
        outlineColorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
        
        //Add the controls to the control menu
        outlineColorMenu.getChildren().add(outlineColorLabel);
        outlineColorMenu.getChildren().add(outlineColorPicker);
        
        backgroundColorPicker.setOnAction(e -> { 
            appDrawSpace.setBackground(
                new Background(
                    new BackgroundFill(
                        Paint.valueOf(
                            backgroundColorPicker.getValue().toString()
                        ), 
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                    )
                )
            );
        });
        fillColorPicker.setOnAction(e -> {
            if(selectedShape != null){
                selectedShape.setFill(fillColorPicker.getValue());
            }
        });
        outlineColorPicker.setOnAction(e -> {
            if (selectedShape != null){
                selectedShape.setStroke(outlineColorPicker.getValue());
            }
        });
        
        //Add the color pickers to the list of color pickers
        activeColors.add(backgroundColorPicker);
        activeColors.add(fillColorPicker);
        activeColors.add(outlineColorPicker);
        
        //Setting up the outline thickness menu
        outlineThicknessMenu = new VBox(10);
        Label outlineThicknessLabel = new Label("Outline Thickness");
        outlineThicknessLabel.getStyleClass().add("subheading_label");
        outlineThicknessSlider = new Slider(0,20,currentOutlineThickness);
        outlineThicknessSlider.valueProperty().addListener(e ->{
            currentOutlineThickness = outlineThicknessSlider.getValue();
            if(selectedShape != null){
                selectedShape.setStrokeWidth(currentOutlineThickness);
            }
        });
        
        //Add the controls to the GUI
        outlineThicknessMenu.getChildren().add(outlineThicknessLabel);
        outlineThicknessMenu.getChildren().add(outlineThicknessSlider);  
        
        //Setting up the snapshot menu
        snapshotMenu = new VBox();
        snapshotMenu.setAlignment(Pos.CENTER);
        Label snapshotLabel = new Label("Take a snapshot!");
        snapshotLabel.getStyleClass().add("subheading_label");
        Button snapshotButton = new Button();
        snapshotButton.setGraphic(new ImageView("file:images\\Snapshot.png"));
        snapshotButton.setOnAction(e -> {
             WritableImage snapshot = appDrawSpaceContainer.snapshot(null, null);
             try{
                File imageDestination = new File("./temp/");
                if(imageDestination.exists()){
                    if(!imageDestination.isDirectory()){
                        imageDestination.mkdir();
                    }
                }
                else{
                    imageDestination.mkdir();
                }
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(imageDestination);
                fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG (*.png)", "*.png"));
                imageDestination = fileChooser.showSaveDialog(null);
                if(imageDestination != null){
                    if(!imageDestination.getName().endsWith(".png"))
                        imageDestination = new File(imageDestination.getPath() + ".png");
                    try (ImageOutputStream out = ImageIO.createImageOutputStream(imageDestination)) {
                        ImageIO.write(
                                SwingFXUtils.fromFXImage(snapshot, null), "png",
                                out
                        );
                    }
                }
             }catch(IOException ioe){}
        });
        
        //Adding the snapshot menu controls to the menu
        snapshotMenu.getChildren().add(snapshotLabel);
        snapshotMenu.getChildren().add(snapshotButton);
        
        //Add the button layout containers to the GUI
        sideToolbar.getChildren().add(shapeSelectorControlSet);
        sideToolbar.getChildren().add(shapeHeirarchyControlSet);
        sideToolbar.getChildren().add(backgroundColorMenu);
        sideToolbar.getChildren().add(fillColorMenu);
        sideToolbar.getChildren().add(outlineColorMenu);
        sideToolbar.getChildren().add(outlineThicknessMenu);
        sideToolbar.getChildren().add(snapshotMenu);
        
        ScrollPane sideScrollPane = new ScrollPane(sideToolbar); 
        sideScrollPane.getStyleClass().add("max_pane");
        appDrawSpaceContainer = new SplitPane();
        appDrawSpaceContainer.getItems().add(appDrawSpace);
        //Set up the workspace with all the components
        ((BorderPane)workspace).setLeft(sideScrollPane);
        ((BorderPane)workspace).setCenter(appDrawSpaceContainer);
        // NOTE THAT WE HAVE NOT PUT THE WORKSPACE INTO THE WINDOW,
	// THAT WILL BE DONE WHEN THE USER EITHER CREATES A NEW
	// COURSE OR LOADS AN EXISTING ONE FOR EDITING
	workspaceActivated = false;
        initStyle();
        setUpDrawPaneEventHandlers();
    }
    
    /**
     * This function specifies the CSS style classes for all the UI components
     * known at the time the workspace is initially constructed. Note that the
     * tag editor controls are added and removed dynamicaly as the application
     * runs so they will have their style setup separately.
     */
    @Override
    public void initStyle() {
	appDrawSpace.getStyleClass().add("render_canvas");
        sideToolbar.getStyleClass().add("max_pane");
        shapeSelectorControlSet.getStyleClass().add("max_pane");
        shapeSelectorControlSet.getStyleClass().add("control_set");
        shapeHeirarchyControlSet.getStyleClass().add("max_pane");
        shapeHeirarchyControlSet.getStyleClass().add("control_set");
        backgroundColorMenu.getStyleClass().add("max_pane");
        backgroundColorMenu.getStyleClass().add("control_set");
        fillColorMenu.getStyleClass().add("max_pane");
        fillColorMenu.getStyleClass().add("control_set");
        outlineColorMenu.getStyleClass().add("max_pane");
        outlineColorMenu.getStyleClass().add("control_set");
        outlineThicknessMenu.getStyleClass().add("max_pane");
        outlineThicknessMenu.getStyleClass().add("control_set");
        snapshotMenu.getStyleClass().add("max_pane");
        snapshotMenu.getStyleClass().add("control_set");
    }
    
    /**
     * This function provides the current mouse state for the application workspace.
     * @return the current mouse state of the application.
     */
    public MouseState getMouseState(){
        return currentMouseState;
    }
    
    public Shape getSelectedShape(){
        return selectedShape;
    }
    
    public String getSelectedOutlineFill(){
        return activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal()).getValue().toString();
    }
    
    public Pane getDrawPane(){
        return appDrawSpace;
    }
    
    public Stack<Shape> getShapeStack(){
        return shapeStack;
    }
    
    public void resetWorkspace(){
        try{
            selectedShape = null;
            currentOutlineThickness = 5;
            activeColors.get(ColorPickerIndex.BACKGROUNDCOLOR.ordinal()).setValue(DEFAULT_BACKGROUND_COLOR);
            activeColors.get(ColorPickerIndex.FILLCOLOR.ordinal()).setValue(DEFAULT_FILL_COLOR);
            activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal()).setValue(DEFAULT_OUTLINE_COLOR);
            currentMouseState = MouseState.SELECTOR;
            appDrawSpace.setBackground(
                new Background(
                    new BackgroundFill(
                            activeColors.get(
                                ColorPickerIndex.BACKGROUNDCOLOR.ordinal()
                            ).getValue(),
                            CornerRadii.EMPTY,
                            Insets.EMPTY
                    )
                )
            );
        }catch(NullPointerException NPE){}
    }
    
    private void setUpDrawPaneEventHandlers(){
        appDrawSpace.setOnMouseEntered(e -> {
        switch (currentMouseState) {
             case SELECTOR:
                 appDrawSpace.setCursor(Cursor.DEFAULT);
                 break;
             case CREATE_RECT:                    
             case CREATE_ELLIPSE:
                 appDrawSpace.setCursor(Cursor.CROSSHAIR);
                 break;
             default:
                 appDrawSpace.setCursor(Cursor.DEFAULT);
                 break;
        }});
        
        appDrawSpace.setOnMousePressed(e -> {
        if(selectedShape != null)
            selectedShape.setStroke(activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal()).getValue());
        switch (currentMouseState){
            case CREATE_RECT:
                selectedShape = new Rectangle(0,0);
                Rectangle tempRect = (Rectangle)selectedShape;
                tempRect.setFill(activeColors.get(ColorPickerIndex.FILLCOLOR.ordinal()).getValue());
                tempRect.setStroke(activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal()).getValue());
                tempRect.setStrokeWidth(currentOutlineThickness);
                tempRect.setX(e.getX());
                tempRect.setY(e.getY());
                appDrawSpace.getChildren().add(tempRect);
                setShapeListeners(tempRect);
                break;
            case CREATE_ELLIPSE:
                selectedShape = new Ellipse(0,0);
                Ellipse tempEllipse = (Ellipse)selectedShape;
                tempEllipse.setFill(activeColors.get(ColorPickerIndex.FILLCOLOR.ordinal()).getValue());
                tempEllipse.setStroke(activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal()).getValue());
                tempEllipse.setStrokeWidth(currentOutlineThickness);
                tempEllipse.setCenterX(e.getX());
                tempEllipse.setCenterY(e.getY());
                appDrawSpace.getChildren().add(tempEllipse);
                setShapeListeners(tempEllipse);
                break;
            default:
                if(selectedShape != null)
                    selectedShape.setStroke(activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal()).getValue());
                selectedShape = null;
                break;
        }
        updateControls();
        });
        
        appDrawSpace.setOnMouseDragged(e -> {
        switch (currentMouseState){
            case CREATE_RECT:
                Rectangle tempRect = (Rectangle)selectedShape;
                if(tempRect != null){
                    tempRect.setWidth(e.getX() - tempRect.getX());
                    tempRect.setHeight(e.getY() - tempRect.getY());
                }
                break;
            case CREATE_ELLIPSE:
                Ellipse tempEllipse = (Ellipse)selectedShape;
                if(tempEllipse != null){
                    tempEllipse.setRadiusX(e.getX() - tempEllipse.getCenterX());
                    tempEllipse.setRadiusY(e.getY() - tempEllipse.getCenterY());
                }
                break;
            default:
                selectedShape = null;
                break;
        }});
        
        appDrawSpace.setOnMouseReleased(e -> {
        switch (currentMouseState){
            case CREATE_RECT:
                Rectangle tempRect = (Rectangle)selectedShape;
                if(selectedShape != null && !shapeStack.contains(selectedShape))
                    shapeStack.push(selectedShape);
                break;
            case CREATE_ELLIPSE:
                Ellipse tempEllipse = (Ellipse)selectedShape;
                if(selectedShape != null && !shapeStack.contains(selectedShape))
                    shapeStack.push(selectedShape);
                break;
            default:
                break;
        }
        selectedShape = null;
        });
    }
    
    public void setShapeListeners(Shape tempShape){
        tempShape.setOnMouseClicked(x -> {
            switch (currentMouseState){
                case SELECTOR:
                    selectedShape = tempShape;
                    updateControls();
                    tempShape.setStroke(Paint.valueOf("yellow"));
                    break;
                case REMOVAL:
                    if(shapeStack.contains(tempShape))
                        shapeStack.remove(tempShape);
                    appDrawSpace.getChildren().remove(tempShape);
                    break;
                default:
                    break;
            }
        });
        if(tempShape instanceof Rectangle){
            tempShape.setOnMouseDragged(x -> {
                switch (currentMouseState){
                    case SELECTOR:
                        ((Rectangle)tempShape).setX(x.getX());
                        ((Rectangle)tempShape).setY(x.getY());
                        break;
                    default:
                        break;
                }
            });
        }
        else{
            tempShape.setOnMouseDragged(x -> {
                switch (currentMouseState){
                    case SELECTOR:
                        ((Ellipse)tempShape).setCenterX(x.getX());
                        ((Ellipse)tempShape).setCenterY(x.getY());
                        break;
                    default:
                        break;
                }
            });
        }
    }
    
    private void updateControls(){
        if(selectedShape != null){
            ColorPicker colorPicker = activeColors.get(ColorPickerIndex.FILLCOLOR.ordinal());
            colorPicker.setValue(Color.valueOf(selectedShape.getFill().toString()));
            colorPicker = activeColors.get(ColorPickerIndex.OUTLINECOLOR.ordinal());
            colorPicker.setValue(Color.valueOf(selectedShape.getStroke().toString()));
            outlineThicknessSlider.adjustValue(selectedShape.getStrokeWidth());
            currentOutlineThickness = outlineThicknessSlider.getValue();
        }
    }
    
    /**
     * This function reloads all the controls for editing tag attributes into
     * the workspace.
     */
    @Override
    public void reloadWorkspace() {
        
    }
};