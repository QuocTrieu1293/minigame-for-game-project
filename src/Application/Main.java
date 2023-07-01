package Application;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Group root = new Group();
		Scene scene = new Scene(root,888,619);
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setResizable(false);
		game obj = new game(primaryStage);
//		obj.play();
		
	}
	
}
