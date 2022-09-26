import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Jordle Class - Java Application.
 * Jordle written in JavaFX
 * @author gcanales6
 * @version 1.1
 */
public class Jordle extends Application {
    private BorderPane root;
    private static String wordToGuess;
    private static String userGuess;
    private ArrayList<ArrayList> gridArray;
    private static GridPane gridPane;
    private Label letter;
    private static Label instructions;
    private static Stage howToPlay;
    private static Label gameStatusMessage;
    private static int column;
    private static int rows;

    @Override
    public void start(Stage primaryStage) {
        // Choosing word to be guessed
        chooseWordToGuess();

        // Label title containing the name of the game
        Label mainLabel = new Label("JORDLE");
        mainLabel.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 28));
        mainLabel.setTextFill(Color.WHITE);
        mainLabel.setPadding(new Insets(50, 50, 50, 50));

        // Creating the grid
        gridPane = createLetterGrid();

        // Handling user input from keyboard
        gridPane.setOnKeyReleased(new KeyEventsHandler());

        // Creating nodes to be placed on the bottom of the border pane (root)
        gameStatusMessage = new Label("Beep! Boop! Try guessing a word!");
        gameStatusMessage.setFont(Font.font("Verdana", FontPosture.ITALIC, 16));
        gameStatusMessage.setStyle("-fx-text-fill: white");
        Button restartBtn = new Button("New Word");
        restartBtn.setFont(Font.font("Verdana", 16));
        Button instructionsBtn = new Button("How To Play");
        instructionsBtn.setFont(Font.font("Verdana", 16));

        // Restarting the game by clearing the grid and choosing a new random word
        restartBtn.setOnAction(e -> newWord());

        // Showing instructions windown when "How To Play" button is clicked
        instructionsBtn.setOnAction(new EventHandler<ActionEvent>() { // anonymous inner class
            @Override
            public void handle(ActionEvent e) {
                howToPlay.show();
                gridPane.requestFocus();
            }
        });

        // HBox containing the nodes that go on the bottom of the border pane
        HBox bottomPane = new HBox(20);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 35, 50, 35));
        bottomPane.getChildren().addAll(gameStatusMessage, restartBtn, instructionsBtn);

        // Root - Border pane to hold all the nodes created above
        root = new BorderPane();
        root.setStyle("-fx-background-color: #101010");
        root.setTop(new StackPane(mainLabel));
        root.setCenter(gridPane);
        root.setBottom(bottomPane);

        // Stage to show the game itself
        primaryStage.setResizable(false);
        primaryStage.setTitle("Jordle");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();

        // Focuses on gridPane to register user input
        gridPane.requestFocus();

        // Confirm exit "Jordle"
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Exit Jordle");
                alert.setHeaderText("Confirm Exit");
                alert.setContentText("Are you sure you want to exit Jordle?\nYour progress will NOT be saved.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Platform.exit();
                } else {
                    event.consume();
                }
            }
        });

        // Stage to show the instructions at the beginning of the game and when the instructions button is clicked
        howToPlay = new Stage();
        howToPlay.setTitle("How To Play");
        howToPlay.setScene(new Scene(createInstructions(), 500, 350));
        howToPlay.setResizable(false);
        howToPlay.show();
    }

    /**
     * Inner class to handle key events
     */
    class KeyEventsHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent ke) {
            if (ke.getCode().isLetterKey()) {
                if (rows < 5 && column < 5) {
                    updateLetter(gridArray, ke.getText().toUpperCase(), rows, column);
                    column++;
                }
            } else if (ke.getCode() == KeyCode.BACK_SPACE) {
                if (column != 0 && column < 6) {
                    column--;
                    updateLetter(gridArray, "", rows, column);
                }
            } else if (ke.getCode() == KeyCode.ENTER) {
                if (column == 5) {
                    int numOfRightLetters = 0;
                    for (Object o : gridArray.get(rows)) {
                        userGuess = ((Label) o).getText();
                        if (wordToGuess.indexOf(userGuess) != -1) {
                            if (gridArray.get(rows).indexOf(o) == wordToGuess.indexOf(userGuess)) {
                                updateLetterColor((Label) o, new String("green"));
                                numOfRightLetters++;
                            } else {
                                updateLetterColor((Label) o, new String("yellow"));
                            }
                        } else {
                            updateLetterColor((Label) o, new String("gray"));
                        }
                    }
                    if (numOfRightLetters == 5) {
                        gameStatusMessage.setText("Beep! Boop! Congratulations! You've guessed the word!");
                        rows = 6;
                        column = 6;
                    } else if (numOfRightLetters != 5 && rows == 4) {
                        gameStatusMessage.setText("Beep! Boop! Game over! The word was " + wordToGuess.toLowerCase());
                    } else {
                        rows++;
                        column = 0;
                    }
                } else if (rows < 5 && column < 5) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error 1331:\n5-letter word not found.");
                    alert.setContentText("Please enter a 5-letter word to test your guess!");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        alert.close();
                    } else {
                        ke.consume();
                    }
                    gridPane.requestFocus();
                }
            }
        }
    }

    /**
     * Helper method that randomly chooses a word to be guessed
     */
    private void chooseWordToGuess() {
        wordToGuess = new String();
        wordToGuess = Words.list.get((int) (Math.random() * Words.list.size())).toUpperCase();
    }

    /**
     * Creates the GridPane which holds the labels where the input letter will be displayed.
     * @return GridPane 6x5 grid of labels
     */
    private GridPane createLetterGrid() {
        // Grid to hold the labels representing each letter in the word
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(0, 35, 50, 35));
        gridPane.setHgap(5.5);
        gridPane.setVgap(5.5);

        // Creating labels inside a 2D ArrayList and adding them to the grid pane
        gridArray = new ArrayList<>();
        ArrayList<Label> row0 = new ArrayList<>();
        ArrayList<Label> row1 = new ArrayList<>();
        ArrayList<Label> row2 = new ArrayList<>();
        ArrayList<Label> row3 = new ArrayList<>();
        ArrayList<Label> row4 = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            row0.add(createBlankLetter());
            row1.add(createBlankLetter());
            row2.add(createBlankLetter());
            row3.add(createBlankLetter());
            row4.add(createBlankLetter());
        }
        gridArray.add(row0);
        gridArray.add(row1);
        gridArray.add(row2);
        gridArray.add(row3);
        gridArray.add(row4);

        int col = 0;
        for (int row = 0; row < 5; row++) {
            gridPane.add((Label) gridArray.get(row).get(col), col, row);
            col++;
            gridPane.add((Label) gridArray.get(row).get(col), col, row);
            col++;
            gridPane.add((Label) gridArray.get(row).get(col), col, row);
            col++;
            gridPane.add((Label) gridArray.get(row).get(col), col, row);
            col++;
            gridPane.add((Label) gridArray.get(row).get(col), col, row);
            col = 0;
        }
        return gridPane;
    }

    /**
     * Creates and styles a label with no test, used to be shown when the user hasn't input a letter.
     * @return Label with the defined style and dimensions
     */
    private Label createBlankLetter() {
        this.letter = new Label();
        this.letter.setMinSize(70, 70);
        this.letter.setStyle("-fx-border-color: #404040; -fx-border-radius: 15 15 15 15; -fx-border-width: 3;"
            + "-fx-background-radius: 15 15 15 15; -fx-background-color: #101010");
        this.letter.setFocusTraversable(true);
        return this.letter;
    }

    /**
     * Updates the label where the user input was entered into, displaying the input and appropriate style.
     * @param gridArray 2D arraylist holding an arraylist of labels
     * @param guess String containg the text to be set to the label
     * @param row int representing the Y-axis position of the label in the grid
     * @param col int representing the X-axis position of the label in the grid
     * @return Label with updated text and style
     */
    private Label updateLetter(ArrayList<ArrayList> gridArr, final String guess, int row, int col) {
        this.letter = (Label) gridArr.get(row).get(col);
        this.letter.setText(guess);
        this.letter.setAlignment(Pos.CENTER);
        this.letter.setStyle("-fx-border-color: #404040; -fx-border-radius: 15 15 15 15; -fx-border-width: 3;"
            + "-fx-background-radius: 15 15 15 15; -fx-background-color: #101010; -fx-text-fill: white");
        this.letter.setFont(Font.font("Verdana", FontWeight.BOLD, 44));
        return this.letter;
    }

    /**
     * Updates the color of the label according to the guessed letter when compared to the word to be guessed.
     * @param blank Label representing the label that holds the letter being compared
     * @param color String representing the color to be applied to the label
     * @return Label with the corresponding color to the guessed letter when compared to the word to be guessed
     */
    private Label updateLetterColor(Label blank, String color) {
        this.letter = blank;
        this.letter.setAlignment(Pos.CENTER);
        this.letter.setFont(Font.font("Verdana", FontWeight.BOLD, 44));
        switch (color) {
        case "green":
            this.letter.setStyle("-fx-border-color: #5F973E; -fx-border-radius: 15 15 15 15;"
                + "-fx-background-radius: 15 15 15 15; -fx-background-color: #5F973E; -fx-text-fill: white");
            break;
        case "yellow":
            this.letter.setStyle("-fx-border-color: #D7C800; -fx-border-radius: 15 15 15 15;"
                + "-fx-background-radius: 15 15 15 15; -fx-background-color: #D7C800; -fx-text-fill: white");
            break;
        default:
            this.letter.setStyle("-fx-border-color: #404040; -fx-border-radius: 15 15 15 15;"
                + "-fx-background-radius: 15 15 15 15; -fx-background-color: #404040; -fx-text-fill: white");
            break;
        }
        return this.letter;
    }

    /**
     * Helper method to restart the game; generates a new grid, a new random word, and resets the game status message.
     */
    private void newWord() {
        column = 0;
        rows = 0;
        chooseWordToGuess();
        gameStatusMessage.setText("Beep! Boop! Try guessing a word!");
        gridPane.getChildren().clear();
        gridPane = createLetterGrid();
        root.setCenter(gridPane);
        gridPane.setOnKeyReleased(new KeyEventsHandler());
        gridPane.requestFocus();
    }

    /**
     * Creates the label whose text contains the instructions to Jordle that are to be shown to the user.
     * @return Label containg the instructions as text
     */
    private Label createInstructions() {
        instructions = new Label();
        instructions.setPadding(new Insets(15));
        instructions.setText("Welcome to Jordle!\n\nTry guessing the randomly-chosen 5-letter word in 6 tries!\n"
            + "Please enter exactly 5 letters per guess, not more, not less.\nTo try your guess, press Enter.\n\n"
            + "Gray letter: that letter is not in the word.\nYellow letter: that letter is in the word, but not in "
            + "that position.\nGreen letter: that letter is in the word, and in that position.\n\n"
            + "Also, please enter letters ONLY, specifically, from the English language.\n\nIf you want to play again "
            + "or restart your game with a new word, simply click on the \"New Word\" button anytime.\n\n"
            + "We hope you have fun, and good luck!");
        instructions.setWrapText(true);
        instructions.setAlignment(Pos.TOP_CENTER);
        instructions.setTextAlignment(TextAlignment.JUSTIFY);
        instructions.setStyle("-fx-background-color: #202020; -fx-text-fill: white");
        instructions.setFont(Font.font("Verdana", 14));
        return instructions;
    }

    /**
     * Main method.
     * @param args String[] containing the application launch from IDE
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
