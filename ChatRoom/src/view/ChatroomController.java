package view;

import java.io.IOException;

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
import message.ChatMessage;
import server.Server;

/**
 * TODO:
 * 1. activeUserListView wird nur bei dem 1 User angezeigt, da nur er einen Zugriff auf Server hat.
 * evtl. Message vom Server implementieren, die mit der gleich nach der Einlogge-Bestaetigung an den Client geschickt wird sowie
 * jedes mal, wenn neue Clients dazukommen.
 * 
 * 2. chatRoomListView soll bei dem Absende-Client gleich nach dem Absenden angezeigt werden, bei dem Empfaenger
 * gleich nachdem alle Chunks angekommen sind. (Alle angekommenen Chat Messages liegen hier -> client.getChatData(newValue).getChatHistory()
 * 
 * 
 * 3. Da Stop-and-Wait Logik ausgenommen wurde, bekommt GUI jetzt keine returns in Methoden,
 * aus denen die Anfrage an den Server abgeschickt wurde. Alle Server-Responses werden in der Klasse Client, Methode handleMessage()
 * bearbeitet. D.h. GUI neu verknuepfen.
 * 
 * 4. GUI Loesung fuer Chat-Anfragen implementieren. Gerade wird die Anfrage automatisch angenommen.
 *
 */


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
//	List<Integer> clientPortNummer;
//	final int MAX_CLIENT = 6;
	int clientUDPPort;
	boolean selected = false;
	
	public ChatroomController() {
//		clientPortNummer = new ArrayList<>();
//		int startValue = 1201;
//		for (int i = 0; i < MAX_CLIENT; i++) {
//			clientPortNummer.add(startValue + i);
//		}
		
	}

	public Button getStartServer() {
		return startServer;
	}

	@FXML
	void SetOnActionLoginButton(ActionEvent event) {
		System.out.println(username.getText() + password.getText());

		String name = username.getText();
		String passwort = password.getText();
		
		// Client UDP Port wird random generiert
		clientUDPPort = (int) (Math.random() * 2123) + 1201; 
		System.out.println("VC: UDP port: " + clientUDPPort);
		
		this.client = new Client(name, clientUDPPort);

		try {
			client.startTCP();
			System.out.println("Client wurde gestartet");
		} catch (IOException e) {
			informationLabel.setText("Bitte erstmal den Server starten");
			e.printStackTrace();
		}
		if (client.isLogin()) {
			scrollUp();
			client.setLogin(false);
			// Wenn man auf den Namen klickt, wird die Chat-Historie angezeigt
						activeUserListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
							
							@Override
							public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
								sendButton.setDisable(false);
								anfrageButton.setDisable(false);
								
								if (client.getChatData(newValue) != null) {
									ObservableList<String> messages = FXCollections.observableArrayList();
									client.getChatData(newValue).getMessages().stream().forEach(e -> messages.add(e.toString()));
									
									chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
									chatRoomListView.setItems(messages);
								}
								
								client.getChatData(newValue).getMessages().addListener(new ListChangeListener<ChatMessage>() {

									@Override
									public void onChanged(Change<? extends ChatMessage> arg0) {
										ObservableList<String> messages = FXCollections.observableArrayList();
										client.getChatData(newValue).getMessages().stream().forEach(e -> messages.add(e.toString()));
										
										chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
										chatRoomListView.setItems(messages);
										
									}
									
								});
								
							}
							
							
						});
						client.getActiveUser().addListener(new ListChangeListener<String>() {
							@Override
							public void onChanged(Change<? extends String> arg0) {
								System.out.println("USERSSSSS: ");
								Platform.runLater(() -> {
									activeUserListView.getItems().setAll(arg0.getList());
								});
							}
						});
		} else {
			informationLabel.setText("Bitte pruefe nochmal deine Daten.");
		}

	}

	@FXML
	void SetOnActionRegisterButton(ActionEvent event) {
		String name = username.getText();
		String passwort = password.getText();
		System.out.println("ChatRoomController: " + username.getText() + ", " + password.getText());

		// Client UDP Port wird random generiert
		clientUDPPort = (int) (Math.random() * 2123) + 1201;
		System.out.println("VC: UDP port: " + clientUDPPort);
		
		this.client = new Client(name, clientUDPPort);
		
		try {
			client.startTCP();
			System.out.println("Client wurde gestartet");
		} catch (IOException e) {
			informationLabel.setText("Bitte erstmal den Server starten");
			e.printStackTrace();
		}

		if (client.isRegister()) {
			scrollUp();
			
			sendButton.setDisable(true);
			anfrageButton.setDisable(true);
			
			// Wenn man auf den Namen klickt, wird die Chat-Historie angezeigt
			activeUserListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
				
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					sendButton.setDisable(false);
					anfrageButton.setDisable(false);
					
					if (client.getChatData(newValue) != null) {
						ObservableList<String> messages = FXCollections.observableArrayList();
						client.getChatData(newValue).getMessages().stream().forEach(e -> messages.add(e.toString()));
						
						chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
						chatRoomListView.setItems(messages);
					}
					
					client.getChatData(newValue).getMessages().addListener(new ListChangeListener<ChatMessage>() {

						@Override
						public void onChanged(Change<? extends ChatMessage> arg0) {
							ObservableList<String> messages = FXCollections.observableArrayList();
							client.getChatData(newValue).getMessages().stream().forEach(e -> messages.add(e.toString()));
							
							chatRoomListView.getItems().remove(0, chatRoomListView.getItems().size());
							chatRoomListView.setItems(messages);
							
						}
						
					});
					
				}
				
				
			});
			client.getActiveUser().addListener(new ListChangeListener<String>() {
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
		
	}

	
	@FXML
	void SetOnActionAnfrageButton(ActionEvent event) {
		String user = activeUserListView.getSelectionModel().getSelectedItem();
		client.sendChatRequest(user);
	}

	@FXML
	void SetOnActionSendButton(ActionEvent event) {
		if (typeMessageField.getText() != null) {
			String user = activeUserListView.getSelectionModel().getSelectedItem();
			client.sendTextMessage(user, typeMessageField.getText());
			typeMessageField.clear();
			
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
		if (client.isLoggedout()) {
			client.setLoggedout(false);
			scrollDown();
		}
		
		
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
