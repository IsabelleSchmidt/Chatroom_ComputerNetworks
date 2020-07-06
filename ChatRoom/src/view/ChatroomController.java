package view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import client.Client;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import server.Server;

public class ChatroomController {

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
	private Button anfrageButton;

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

	@FXML
	private Label informationLabel;
	
	private Client client;
	private Server server;
	boolean isStarted = false;
	List<Integer> clientPortNummer;
	final int MAX_CLIENT = 6;
	
	public ChatroomController() {
		clientPortNummer = new ArrayList<>();
		int startValue = 1201;
		for (int i = 0; i < MAX_CLIENT; i++) {
			clientPortNummer.add(startValue + i);
		}
	}

	public Button getStartServer() {
		return startServer;
	}

	@FXML
	void SetOnActionLoginButton(ActionEvent event) {
		System.out.println(username.getText() + password.getText());

		String name = username.getText();
		String passwort = password.getText();
		
		if (clientPortNummer.size() == 0) {
			informationLabel.setText("Maximale Anzahl von Clients erreicht.");
		} else if (name != "" && passwort != "") {
		
			int clientPort = clientPortNummer.get(0);
			clientPortNummer.remove(0);
			this.client = new Client(name, clientPort);
	
			try {
				client.startTCP();
				System.out.println("Client wurde gestartet");
			} catch (IOException e) {
				informationLabel.setText("Bitte erstmal den Server starten");
				e.printStackTrace();
			}
			try {
				if (client.login(name, passwort)) {
					scrollUp();
				} else {
					informationLabel.setText("Bitte prüfe nochmal deine Daten.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
	}

	@FXML
	void SetOnActionRegisterButton(ActionEvent event) {
		String name = username.getText();
		String passwort = password.getText();
		System.out.println("ChatRoomController: " + username.getText() + ", " + password.getText());

		if (clientPortNummer.size() == 0) {
			informationLabel.setText("Maximale Anzahl von Clients erreicht.");
		} else if (name != "" && passwort != "") { //TODO: anpassen, funktioniert nicht
			
			int clientPort = clientPortNummer.get(0);
			clientPortNummer.remove(0);
			this.client = new Client(name, clientPort);
			
			this.client = new Client(name, clientPort);
			try {
				client.startTCP();
				System.out.println("Client wurde gestartet");
			} catch (IOException e) {
				informationLabel.setText("Bitte erstmal den Server starten");
				e.printStackTrace();
			}
	
			try {
				if (client.registrier(name, passwort)) {
					scrollUp();
				} else {
					informationLabel.setText("Der Benutzername ist leider schon vergeben");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	@FXML
	void SetOnActionAnfrageButton(ActionEvent event) {
		String user = activeUserListView.getSelectionModel().getSelectedItem();
		client.sendChatRequest(user);
	}

	@FXML
	void SetOnActionSendButton(ActionEvent event) {
		if (typeMessageField.getText().equals("accept")) {
			String user = activeUserListView.getSelectionModel().getSelectedItem();
			client.acceptRequest(user);
			typeMessageField.clear();
		} else {
			if (typeMessageField.getText() != null) {
				String user = activeUserListView.getSelectionModel().getSelectedItem();
				client.sendTextMessage(user, typeMessageField.getText());
				typeMessageField.clear();
			}
		}
		
	}

	@FXML
	void SetStartServerOnAction(ActionEvent event) {

		server = new Server();
		server.start();
		
		server.getServerData().activeUser.addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> arg0) {
				System.out.println("USERSSSSS: ");
				Platform.runLater(() -> {
					activeUserListView.getItems().setAll(arg0.getList());
				});
			}

		});

	}

	@FXML
	void SetOnActionLogoutButton(ActionEvent event) {
		String name = username.getText();
		client.logout(name);
		
		
		scrollDown();
	}

	void scrollUp() {
		sendButton.setDisable(true);
		anfrageButton.setDisable(true);
		
		activeUserListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				sendButton.setDisable(false);
				anfrageButton.setDisable(false);
				
				if (client.getChatData(newValue) != null) {
					ObservableList<String> messages = FXCollections.observableArrayList();
					client.getChatData(newValue).getChatHistory().stream().forEach(e -> messages.add(e.toString()));
					
					chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
					chatRoomListView.setItems(messages);
				}
			}
		});
		
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
