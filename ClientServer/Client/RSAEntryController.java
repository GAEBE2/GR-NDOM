import UserGroups.ClientUser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Created by tkr6u on 20.04.2017.
 * used to get a key pair from the user
 */
public class RSAEntryController implements Initializable {

    @FXML
    public Button nextButton;

    @FXML
    public TextArea publicKeyText;

    @FXML
    public TextArea privateKeyText;

    Consumer<ClientUser> stageConsumer;

    private ClientUser clientUser = new ClientUser("test", null, null);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nextButton.setDisable(true);
        publicKeyText.textProperty().addListener((observableValue, s, s2) -> checkIfNextButtonShouldBeDisabled());
        privateKeyText.textProperty().addListener((observableValue, s, s2) -> checkIfNextButtonShouldBeDisabled());

    }

    /**
     * checks whether or not the text entered in both text Fields are valid keys and disables th next button if not
     */
    public void checkIfNextButtonShouldBeDisabled() {
        try {
            if(publicKeyText.getText().length() > 50 && privateKeyText.getText().length() > 50) {
                    PublicKey publicKey = (PublicKey) Security.convertStringToKeyWithException(publicKeyText.getText(), true);
                    PrivateKey privateKey = (PrivateKey) Security.convertStringToKeyWithException(privateKeyText.getText(), false);
                    if (publicKey != null && privateKey != null) {
                        clientUser.setPrivateKey(privateKey);
                        clientUser.setPublicKey(publicKey);
                        nextButton.setDisable(false);
                    }else {
                        resetButton();
                    }

            }else {
                resetButton();
            }
        }catch (ClassCastException | IOException | InvalidKeySpecException e){
            resetButton();
        }
    }

    public void resetButton() {
        clientUser.setPublicKey(null);
        clientUser.setPrivateKey(null);
        nextButton.setDisable(true);
    }

    @FXML
    public void next() {
        if(stageConsumer != null) {
            stageConsumer.accept(clientUser);
        }
    }

    /**
     * uses the security class to generate and set the public and private key
     */
    @FXML
    public void generateRSAKeyPair() {
        KeyPair pair;
        try {
            pair = Security.generateKeyPair();
            clientUser.setPublicKey(pair.getPublic());
            clientUser.setPrivateKey(pair.getPrivate());
            Platform.runLater(() -> {
                publicKeyText.setText(Security.convertKeyToString(pair.getPublic()));
                    privateKeyText.setText(Security.convertKeyToString(pair.getPrivate()));
                nextButton.setDisable(false);
            });
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cancel() {
        Platform.exit();
        System.exit(0);
    }
}
