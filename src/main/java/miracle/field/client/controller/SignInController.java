package miracle.field.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import miracle.field.client.util.Observer;
import miracle.field.client.util.SpringStageLoader;
import miracle.field.shared.model.User;
import miracle.field.shared.packet.Packet;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@NoArgsConstructor
public class SignInController extends AbstractFxmlController {
    private static final String SIGN_UP_SCENE = "sign_up";
    private static final String MAIN_SCENE = "main";

    @FXML
    private TextField user;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginButton;

    @Override
    public void getNotify(Packet packet) {
        switch (packet.getType()) {
            case "loginSuccess":
                getContext().getBean(Observer.class).removeWaiter("loginSuccess", this);
                Platform.runLater(() -> {
                    try {
                        mainSceneLoad();
                    }
                    catch (IOException ex) {
                        //TODO
                    }
                });
                break;
            case "loginError":
//                TODO show error hint
                getContext().getBean(Observer.class).removeWaiter("loginError", this);
                break;
            default:
                return;
        }
    }

    @FXML
    public void onClickLoad() {
        User tempUser = new User();
        tempUser.setUsername(user.getText());
        tempUser.setPassword(password.getText());
        try {
            getContext().getBean(WebSocketClient.class).send(
                    getContext().getBean(ObjectMapper.class).writeValueAsBytes(
                            new Packet<>("login", "", tempUser)
                    )
            );
            getContext().getBean(Observer.class).addWaiter("loginSuccess", this);
            getContext().getBean(Observer.class).addWaiter("loginError", this);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Can not write Login packet to server");
        }
        System.out.println("Login packet sent...");
    }

    @FXML
    public void signUpLoad() throws IOException {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(
                getContext().getBean(SpringStageLoader.class).loadScene(SIGN_UP_SCENE)
        );
    }

    @FXML
    public void mainSceneLoad() throws IOException {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(
                getContext().getBean(SpringStageLoader.class).loadScene(MAIN_SCENE));
        stage.show();
    }
}