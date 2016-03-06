package pm.data;

import pm.gui.Workspace;
import saf.components.AppDataComponent;
import saf.AppTemplate;

/**
 * This class serves as the data management component for this application.
 *
 * @author Richard McKenna
 * @author ?
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

    /**
     * This function clears the app draw space
     */
    @Override
    public void reset() {
        if(workspace != null){
            workspace.getDrawPane().getChildren().clear();
            workspace.shapes.clear();
            workspace.reloadWorkspace();
        }
        else{
            workspace = ((Workspace)app.getWorkspaceComponent());
        }
    }
}
