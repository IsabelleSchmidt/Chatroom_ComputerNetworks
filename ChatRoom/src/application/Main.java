package application;

import client_server.Server;
import client_server.TCPServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.chatroomController;

public class Main extends Application {

//	public void init() {
//		Server s = new Server(8888);
//		s.start();
//	}
	@Override
	public void start(Stage primaryStage) throws Exception {

		FXMLLoader fxmlLoader =  new FXMLLoader(getClass().getResource("/view/chatroom.fxml"));
		Parent root = (Parent)fxmlLoader.load();
		Scene scene = new Scene(root);
		primaryStage.initStyle(StageStyle.DECORATED);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
		
		new Thread(()-> {
			try {
				Server s = new Server(8888);
				s.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}).start();

	}

	public static void main(String[] args) {
		launch(args);
	}

}
