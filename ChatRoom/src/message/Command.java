package message;

public enum Command {
	
	//SEND_REQUEST -> Anfrage von einem Client an den anderen
	
	REGISTER, 
	LOGIN, 
	SEND_REQUEST,
	SEND_MESSAGE,			//Nachricht von mir zu anderem Client
	SEND_RESPONSE			//Nachricht von anderem Client zu mir
}
