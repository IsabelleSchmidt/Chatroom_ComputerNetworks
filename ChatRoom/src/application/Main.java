package application;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Anleitung.
 * 1. Server starten, dann die Main
 * 2. Anmelde-View: Server starten, registrieren.
 * 3. Nochmal das Programm starten. Nur registrieren (Server ist bereits an).
 * 4. Zurueck zum 1. Client gehen.
 * 5. Den neuen Client auswaehlen, Anfrage-Button klicken -> Anfrage wird vom anderem Client automatisch bestaetigt.
 * 6. Vom 1. Client die Nachrichten schreiben und abschicken.
 * 7. Um die Nachrichten anzuschauen, in der Liste von den aktiven Usern den 1. Client (sich selbst) auswaehlen und dann
 * wieder den Konversations-Partner.
 *
 */

public class Main extends Application {


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

		primaryStage.setOnCloseRequest(windowEvent -> {
			Platform.exit();
		});

	}

	public static void main(String[] args) {
		launch(args);
	}
	

}
