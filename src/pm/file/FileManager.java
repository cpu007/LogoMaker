package pm.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import pm.data.DataManager;
import pm.gui.Workspace;
import saf.components.AppDataComponent;
import saf.components.AppFileComponent;

/**
 * This class serves as the file management component for this application,
 * providing all I/O services.
 *
 * @author Richard McKenna
 * @author Kenneth Chiguichon
 * @version 1.0
 */
public class FileManager implements AppFileComponent {

    /**
     * This method is for saving user work, which in the case of this
     * application means the data that constitutes the page DOM.
     * 
     * @param data The data management component for this application.
     * 
     * @param filePath Path (including file name/extension) to where
     * to save the data to.
     * 
     * @throws IOException Thrown should there be an error writing 
     * out data to the file.
     */
    @Override
    public void saveData(AppDataComponent data, String filePath) throws IOException {
        StringWriter sw = new StringWriter();

	// BUILD THE HTMLTags ARRAY
	DataManager dataManager = (DataManager)data;
        
        Shape selectedShape = dataManager.getSelectedShape();
        
        ObservableList<Node> shapeChildList = dataManager.getWorkspace().getDrawPane().getChildren();

	//Build Shapes Array
	JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        dataManager.getShapes().stream().map((shape) -> {
            JsonObjectBuilder shapeObjectBuilder = Json.createObjectBuilder();
            if(shape instanceof Rectangle) shapeObjectBuilder.add("Type", "Rectangle");
            else if(shape instanceof Ellipse) shapeObjectBuilder.add("Type", "Ellipse");
            JsonObjectBuilder coordinates = Json.createObjectBuilder()
                    .add("x-location", 
                            (shape instanceof Rectangle)?
                                    ((Rectangle)shape).getX():
                                    ((Ellipse)shape).getCenterX())
                    .add("y-location", 
                            (shape instanceof Rectangle)?
                                    ((Rectangle)shape).getY():
                                    ((Ellipse)shape).getCenterY()
                    );
            JsonObjectBuilder dimensions = Json.createObjectBuilder()
                    .add("width",
                            (shape instanceof Rectangle)?
                                    ((Rectangle)shape).getWidth():
                                    ((Ellipse)shape).getRadiusX()
                    )
                    .add("height",
                            (shape instanceof Rectangle)?
                                    ((Rectangle)shape).getHeight():
                                    ((Ellipse)shape).getRadiusY()
                    );
            shapeObjectBuilder
                    .add("Coordinates", coordinates)
                    .add("Dimensions", dimensions)
                    .add("fill-color", shape.getFill().toString())
                    .add("border-color", 
                        (shape != selectedShape)?
                            shape.getStroke().toString():
                            dataManager.getSelectedOutlineFill()
                    )
                    .add("border-width", shape.getStrokeWidth());
            return shapeObjectBuilder;
        }).forEach((shapeObjectBuilder) -> {
            arrayBuilder.add(shapeObjectBuilder);
        });
	
	// THEN PUT IT ALL TOGETHER IN A JsonObject
	JsonObject dataManagerJSO = Json.createObjectBuilder()
                .add("background-color", dataManager.getBackgroundColor().toString())
                .add("Shapes", arrayBuilder)
		.build();
	
	// AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        try (JsonWriter jsonWriter = writerFactory.createWriter(sw)) {
            jsonWriter.writeObject(dataManagerJSO);
        }

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(filePath);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.write(prettyPrinted);
        }
    }
      
    /**
     * This method loads data from a JSON formatted file into the data 
     * management component and then forces the updating of the workspace
     * such that the user may edit the data.
     * 
     * @param data Data management component where we'll load the file into.
     * 
     * @param filePath Path (including file name/extension) to where
     * to load the data from.
     * 
     * @throws IOException Thrown should there be an error reading
     * in data from the file.
     */
    @Override
    public void loadData(AppDataComponent data, String filePath) throws IOException {
        // CLEAR THE OLD DATA OUT
	DataManager dataManager = (DataManager)data;
	dataManager.reset();
	
	// LOAD THE JSON FILE WITH ALL THE DATA
	JsonObject json = loadJSONFile(filePath);
	
        Workspace workspace = dataManager.getWorkspace();
        
	// LOAD THE Shape Array
	JsonArray jsonShapeArray = json.getJsonArray("Shapes");
        for(int i=0; i < jsonShapeArray.size(); i++){
            Shape tempShape;
            JsonObject jsonShapeObject = jsonShapeArray.getJsonObject(i);
            if(jsonShapeObject.getString("Type").equals("Rectangle")){
                tempShape = new Rectangle(0,0);
            }
            else{
                tempShape = new Ellipse(0,0);
            }
            double xLocation, yLocation, width, height, borderWidth;
            Paint fillColor, borderColor;
            Dimension2D shapeLocation;
            JsonObject coordinates, dimensions;
            coordinates = jsonShapeObject.getJsonObject("Coordinates");
            dimensions = jsonShapeObject.getJsonObject("Dimensions");
            xLocation = coordinates.getJsonNumber("x-location").doubleValue();
            yLocation = coordinates.getJsonNumber("y-location").doubleValue();
            width = dimensions.getJsonNumber("width").doubleValue();
            height = dimensions.getJsonNumber("height").doubleValue();
            borderWidth = jsonShapeObject.getJsonNumber("border-width").doubleValue();
            fillColor = Paint.valueOf(jsonShapeObject.getString("fill-color"));
            borderColor = Paint.valueOf(jsonShapeObject.getString("border-color"));
            shapeLocation = new Dimension2D(xLocation,yLocation);
            if (tempShape instanceof Rectangle){
                ((Rectangle) tempShape).setX(xLocation);
                ((Rectangle) tempShape).setY(yLocation);
                ((Rectangle) tempShape).setWidth(width);
                ((Rectangle) tempShape).setHeight(height);
            }
            else{
                ((Ellipse)tempShape).setCenterX(xLocation);
                ((Ellipse)tempShape).setCenterY(yLocation);
                ((Ellipse)tempShape).setRadiusX(width);
                ((Ellipse)tempShape).setRadiusY(height);
            }
            tempShape.setFill(fillColor);
            tempShape.setStroke(borderColor);
            tempShape.setStrokeWidth(borderWidth);
            dataManager.getShapes().push(tempShape);
            workspace.getDrawPane().getChildren().add(tempShape);
            workspace.setShapeListeners(tempShape);
        }
	
	// AND GET THE BACKGROUND COLOR
	String backgroundColor = json.getString("background-color");
	workspace.getDrawPane().setBackground(
            new Background(new BackgroundFill(Paint.valueOf(backgroundColor),CornerRadii.EMPTY,Insets.EMPTY))
        );
    }

    // HELPER METHOD FOR LOADING DATA FROM A JSON FORMAT
    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
        JsonObject json;
        try (InputStream is = new FileInputStream(jsonFilePath); 
             JsonReader jsonReader = Json.createReader(is)) {
            json = jsonReader.readObject();
        }
	return json;
    }

    @Override
    public void exportData(AppDataComponent data, String filePath) throws IOException {}

    @Override
    public void importData(AppDataComponent data, String filePath) throws IOException {}
}
