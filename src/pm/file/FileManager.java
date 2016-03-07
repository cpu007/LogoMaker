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
import javafx.geometry.Dimension2D;
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

	//Build Shapes Array
	JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(Shape shape : dataManager.getShapes().keySet()){
            JsonObjectBuilder shapeObjectBuilder = Json.createObjectBuilder();
            if(shape instanceof Rectangle) shapeObjectBuilder.add("Type", "Rectangle");
            else if(shape instanceof Ellipse) shapeObjectBuilder.add("Type", "Ellipse");
            JsonObjectBuilder coordinates = Json.createObjectBuilder()
                    .add("x-location", dataManager.getShapes().get(shape).getWidth())
                    .add("y-location", dataManager.getShapes().get(shape).getHeight());
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
                    .add("border-color", shape.getStroke().toString())
                    .add("border-width", shape.getStrokeWidth());
            arrayBuilder.add(shapeObjectBuilder);
        }
	
	// THEN PUT IT ALL TOGETHER IN A JsonObject
	JsonObject dataManagerJSO = Json.createObjectBuilder()
                .add("Background-Color", dataManager.getBackgroundColor().toString())
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

    }

    // HELPER METHOD FOR LOADING DATA FROM A JSON FORMAT
    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
	InputStream is = new FileInputStream(jsonFilePath);
	JsonReader jsonReader = Json.createReader(is);
	JsonObject json = jsonReader.readObject();
	jsonReader.close();
	is.close();
	return json;
    }

    @Override
    public void exportData(AppDataComponent data, String filePath) throws IOException {}

    @Override
    public void importData(AppDataComponent data, String filePath) throws IOException {}
}
