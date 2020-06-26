package view;

import java.io.IOException;

import client_server.Client;
import client_server.TCPServer;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class chatroomController {

	@FXML
	private AnchorPane chatPane;

	@FXML
	private ListView<String> activeUserListView;

	@FXML
	private ListView<String> chatRoomListView;

	@FXML
	private TextArea typeMessageField;

	@FXML
	private Button sendButton;

	@FXML
	private Label chattingWithLabel;

	@FXML
	private AnchorPane startPane;

	@FXML
	private PasswordField password;

	@FXML
	private TextField username;

	@FXML
	private Button registerButton;

	@FXML
	private Button loginButton;

	@FXML
	private Button loggout;

	@FXML
	private Button startServer;

	public Button getStartServer() {
		return startServer;
	}

	@FXML
	private Label informationLabel;

	private Client client;
	private TCPServer server;
	boolean isStarted = false;

	@FXML
	void SetOnActionLoginButton(ActionEvent event) {
		System.out.println(username.getText() + password.getText());

		String name = username.getText();
		String passwort = password.getText();
		this.client = new Client();

		try {
			client.startClient();
			System.out.println("Client wurde gestartet");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (client.login(name, passwort)) {
				scrollUp();
			} else {
				System.out.println("else login");
				informationLabel.setText("Da ist etwas schief gelaufen");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	void SetOnActionRegisterButton(ActionEvent event) {
		String name = username.getText();
		String passwort = password.getText();
		System.out.println(username.getText() + password.getText());

		this.client = new Client();
		try {
			client.startClient();
			System.out.println("Client wurde gestartet");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (client.registrier(name, passwort)) {
				scrollUp();

				server.activeUser.addListener(new ListChangeListener<String>() {
					@Override
					public void onChanged(Change<? extends String> arg0) {
						System.out.println("USERSSSSS: ");
						Platform.runLater(() -> {
							
							activeUserListView.getItems().setAll(arg0.getList());
						});
					}

				});
			} else {
				informationLabel.setText("Der Benutzername ist leider schon vergeben");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	void SetOnActionSendButton(ActionEvent event) {

	}

	@FXML
	void SetStartServerOnAction(ActionEvent event) {

		server = new TCPServer();
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

	}

	@FXML
	void SetOnActionLogoutButton(ActionEvent event) {
		String name = username.getText();

		// client.logout(name);
		scrollDown();
	}

	void scrollUp() {
		TranslateTransition tr1 = new TranslateTransition();
		tr1.setDuration(Duration.millis(600));
		tr1.setToX(0);
		tr1.setToY(-1000);
		tr1.setNode(startPane);
		TranslateTransition tr2 = new TranslateTransition();
		tr2.setDuration(Duration.millis(600));
		tr2.setFromX(0);
		tr2.setFromY(1000);
		tr2.setToX(0);
		tr2.setToY(0);
		tr2.setNode(chatPane);
		ParallelTransition pt = new ParallelTransition(tr1, tr2);
		pt.play();
	}

	void scrollDown() {
		TranslateTransition tr1 = new TranslateTransition();
		tr1.setDuration(Duration.millis(600));
		tr1.setToX(0);
		tr1.setToY(1000);
		tr1.setNode(chatPane);
		TranslateTransition tr2 = new TranslateTransition();
		tr2.setDuration(Duration.millis(600));
		tr2.setFromX(0);
		tr2.setFromY(-1000);
		tr2.setToX(0);
		tr2.setToY(0);
		tr2.setNode(startPane);
		ParallelTransition pt = new ParallelTransition(tr1, tr2);
		pt.play();

	}

}
