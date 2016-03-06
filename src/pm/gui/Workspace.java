package pm.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    VBox sideToolbar;
    
    BorderPane centerWorkspace;
    
    HBox shapeSelectorControlSet;
    HBox shapeHeirarchyControlSet;
    VBox backgroundColorMenu;
    VBox fillColorMenu;
    VBox outlineColorMenu;
    VBox outlineThicknessMenu;
    
    boolean isDrawEnabled = false;
    boolean isShapeSelected = false;
    
    
    
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.valueOf("#ffe4c4");
    private static final Color DEFAULT_FILL_COLOR = Color.valueOf("#ff6666");
    private static final Color DEFAULT_OUTLINE_COLOR = Color.valueOf("#99cc99");
    
    public ArrayList<ColorPicker> activeColors;
    public ArrayList<Shape> shapes;
    public ArrayList<Button> shapeManipulators;
    public double currentOutlineThickness = 5;
    
    //Use enums with the folowing syntax:
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
        
	// WE'LL ORGANIZE OUR WORKSPACE COMPONENTS USING A BORDER PANE
	workspace = new BorderPane();
        activeColors = new ArrayList();
        shapes = new ArrayList();  
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
        
        //Add the color pickers to the list of color pickers
        activeColors.add(backgroundColorPicker);
        activeColors.add(fillColorPicker);
        activeColors.add(outlineColorPicker);
        
        //Setting up the outline thickness menu
        outlineThicknessMenu = new VBox(10);
        Label outlineThicknessLabel = new Label("Outline Thickness");
        outlineThicknessLabel.getStyleClass().add("subheading_label");
        Slider outlineThicknessSlider = new Slider(0,10,currentOutlineThickness);
        outlineThicknessSlider.valueProperty().addListener(e ->{
            currentOutlineThickness = outlineThicknessSlider.getValue();
        });
        
        //Add the controls to the GUI
        outlineThicknessMenu.getChildren().add(outlineThicknessLabel);
        outlineThicknessMenu.getChildren().add(outlineThicknessSlider);        
        
        //Add the button layout containers to the GUI
        sideToolbar.getChildren().add(shapeSelectorControlSet);
        sideToolbar.getChildren().add(shapeHeirarchyControlSet);
        sideToolbar.getChildren().add(backgroundColorMenu);
        sideToolbar.getChildren().add(fillColorMenu);
        sideToolbar.getChildren().add(outlineColorMenu);
         sideToolbar.getChildren().add(outlineThicknessMenu);
        
        //Set up the workspace with all the components
        ((BorderPane)workspace).setLeft(sideToolbar);
        ((BorderPane)workspace).setCenter(appDrawSpace);
        
        // NOTE THAT WE HAVE NOT PUT THE WORKSPACE INTO THE WINDOW,
	// THAT WILL BE DONE WHEN THE USER EITHER CREATES A NEW
	// COURSE OR LOADS AN EXISTING ONE FOR EDITING
	workspaceActivated = false;
        initStyle();
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
    }

    /**
     * This function reloads all the controls for editing tag attributes into
     * the workspace.
     */
    @Override
    public void reloadWorkspace() {

    }
};