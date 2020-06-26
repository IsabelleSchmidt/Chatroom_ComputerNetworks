package application;

import java.io.IOException;
import java.net.BindException;

import client_server.Client;
import client_server.TCPServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	TCPServer server = new TCPServer();


	@Override
	public void start(Stage primaryStage) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/chatroom.fxml"));
		Parent root;
		try {
			root = (Parent) fxmlLoader.load();
			Scene scene = new Scene(root);
			primaryStage.initStyle(StageStyle.DECORATED);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}


		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.start();
				} catch (Exception e) {
				}
			}

		});
		thread.start();

		primaryStage.setOnCloseRequest(windowEvent -> {
			Platform.exit();
		});

	}

	public static void main(String[] args) {
		launch(args);
	}
	

}
