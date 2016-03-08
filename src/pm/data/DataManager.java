package pm.data;

import java.util.HashMap;
import java.util.Stack;
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import pm.gui.Workspace;
import saf.components.AppDataComponent;
import saf.AppTemplate;

/**
 * This class serves as the data management component for this application.
 *
 * @author Richard McKenna
 * @author Kenneth Chiguichon
 * @version 1.0
 */
public class DataManager implements AppDataComponent {
    // THIS IS A SHARED REFERENCE TO THE APPLICATION
    AppTemplate app;
    
    Workspace workspace;

    /**
     * THis constructor creates the data manager and sets up the
     *
     *
     * @param initApp The application within which this data manager is serving.
     */
    public DataManager(AppTemplate initApp) throws Exception {
	// KEEP THE APP FOR LATER
	app = initApp;
        
        // KEEP THE WORKSPACE FOR LATER
        workspace = ((Workspace)app.getWorkspaceComponent());
    }
    
    public Shape getSelectedShape(){
        return workspace.getSelectedShape();
    }
    
    public String getSelectedOutlineFill(){
        return workspace.getSelectedOutlineFill();
    }
    
    public Workspace getWorkspace(){
        return workspace;
    }
    
    public Stack<Shape> getShapes(){
        if(workspace != null) return workspace.shapeStack;
        else return null;
    }
    
    public Paint getBackgroundColor(){
        return workspace.getDrawPane().getBackground().getFills().get(0).getFill();
    }

    /**
     * This function clears the app draw space
     */
    @Override
    public void reset() {
        if(workspace != null){
            workspace.getDrawPane().getChildren().clear();
            workspace.shapeStack.clear();
            workspace.resetWorkspace();
            workspace.reloadWorkspace();            
        }
        else{
            workspace = ((Workspace)app.getWorkspaceComponent());
        }
    }
}
