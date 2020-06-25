package view;

import client_server.TCPServer;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
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
    private ListView<?> activeUserListView;

    @FXML
    private ListView<?> chatRoomListView;

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
    private Label informationLabel;

    @FXML
    void SetOnActionLoginButton(ActionEvent event) {    	
    	scrollUp();
    }

    @FXML
    void SetOnActionRegisterButton(ActionEvent event) {

    }

    @FXML
    void SetOnActionSendButton(ActionEvent event) {

    }
    
    @FXML
    void SetOnActionLogoutButton(ActionEvent event) {
    	System.exit(1);
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

}
