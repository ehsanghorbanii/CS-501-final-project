package com.example.applearrowsimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Objects;
import java.util.Random;


public class FinalSimulation extends Application {

    // Set Screen Size
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight();
    private final double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private final double topPane_height = 0.8*screenHeight;
    private final double botPane_height = 0.2*screenHeight;

    // Constant values
    private static final double GRAVITY = 9.81;
    private static final double ARROW_LENGTH = 50;
    private static final double ARROW_HEAD_LENGTH = 5;
    private static final int MAX_TRIAL = 5;
    private final double apple_radius = 20;

    // Initial values
    private double t = 0; // time -> initial time = 0
    private int trial = 0; // Set initial trial to 0
    private Node apple;
    private double x_new_apple;
    private double y_new_apple;
    private double apple_Vy = 0; // Initial vertical velocity of apple
    private double apple_Vx = randomVelGenerator(); // This method generate a random double between 10.0 and 20.0
    private double ARROW_INITIAL_VELOCITY = 200;
    private double ARROW_INITIAL_ANGLE_DEGREE = 30;
    private double ARROW_INITIAL_ANGLE = Math.PI / 6; // In radian
    private double arrow_Vx = ARROW_INITIAL_VELOCITY *Math.cos(ARROW_INITIAL_ANGLE); // Initial arrow horizontal velocity
    private double arrow_Vy = ARROW_INITIAL_VELOCITY *Math.sin(ARROW_INITIAL_ANGLE); // Initial arrow vertical velocity
    private boolean PLAYED = false; // If simulation starts even for first time changes to true
    private boolean RUNNING = false; // Check the simulation is running or not

    // Get screen size

    private Timeline timeline; // Create timeline for animation
    private Pane topPane; // Top pane for animation screen
    private GridPane controlSection; // Pane for inputs and controlling section
    private Stage endGameStage; // PopUp notification

    // Elements inside the Scene
    private Circle arrow; // Middle point of the arrow
    private Line arrowline; // Arrow
    private Polygon arrowhead; // Arrow head
    private TextField tf_V, tf_theta; // Text-fields for arrow velocity and arrow angle inputs
    private Button pauseResumeButton, startButton, exitButton, resetButton; // Buttons for controlling the animation
    private Label loglabel, instruction_label, posLabel; // Different Labels



    @Override
    public void start(Stage primaryStage) {

        // Different scene sections
        VBox root = new VBox(); // Whole Simulation Screen

        // Top pane
        topPane = new Pane();
        topPane.setStyle("-fx-background-color: rgba(210,255,255,0.65);");
        topPane.setPrefHeight(topPane_height);

        // Bottom pane
        HBox botSection = new HBox();
        botSection.setPrefHeight(botPane_height);

        // Controlling area
        controlSection = new GridPane();


        controlSection.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && RUNNING==false) {
                startButton.fire(); // Trigger the Start button's action
            }
        });


        root.getChildren().addAll(topPane, botSection);

        Scene scene = new Scene(root);

        setupElements(topPane); // Add all elements of the animation section to topPane
        createTimeline();
        CreateControlSectionElements(); // Add all elements of the control section to controlSection

        controlSection.setStyle("-fx-border-color: black; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 5px;" +
                "-fx-background-color: rgb(252,242,242);");

        botSection.getChildren().addAll(instructionLabel(), controlSection, createLogLabels());


        // Stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.setFullScreen(true);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        // Set event for using Esc key to exit
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
            }

        });

        exitButton.setOnAction(e -> {
            primaryStage.close(); // Use the final variable in the lambda expression
        });

        primaryStage.show();

    }

    private boolean applePictureChecker(){
        String image_path = "apple.png";
        if (getClass().getResourceAsStream(image_path)!= null){ // If there is a picture in the resources file
            return true;
        } else {
            return false;
        }
    }

    // Set the picture of apple as apple
    private Node appleAsPicture(){
        Image appleImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("apple.png")));
        ImageView appleimageview = new ImageView(appleImage); // Apple image
        appleimageview.setFitWidth(apple_radius*2); // Set the width of the image (adjust as needed)
        appleimageview.setFitHeight(apple_radius*2); // Set the height of the image (adjust as needed)
        apple = appleimageview;
        return apple;
    }

    // Set the Circle as apple
    private Node appleAsCircle(){
        Circle applecircle = new Circle(apple_radius, Color.RED);
        apple = applecircle;
        return apple;
    }

    private void setupElements(Pane topPane) {
        topPane.getChildren().clear(); // Clear all the elements in top pane

        t = 0; // Set initial time to 0

        apple_Vy = 0; // Set initial Vy of apple to 0


        if (applePictureChecker()){
            apple = appleAsPicture();
            apple.setLayoutX(screenWidth - apple_radius*2); // Set initial X-position of apple
            apple.setLayoutY(0); // Set initial y_position of apple

        }else {
            apple = appleAsCircle();
            apple.setLayoutX(screenWidth - apple_radius); // Set initial X-position of apple
            apple.setLayoutY(apple_radius); // Set initial y_position of apple
        }

        arrow_Vx = ARROW_INITIAL_VELOCITY *Math.cos(ARROW_INITIAL_ANGLE);
        arrow_Vy = ARROW_INITIAL_VELOCITY *Math.sin(ARROW_INITIAL_ANGLE);

        arrow = new Circle(0, Color.BLACK); // Middle point of the arrow
        arrow.setLayoutX(0 + (ARROW_LENGTH / 2 * Math.cos(ARROW_INITIAL_ANGLE))); // Set initial X-position for middle of arrow
        arrow.setLayoutY(topPane_height - (ARROW_LENGTH / 2 * Math.sin(ARROW_INITIAL_ANGLE))); // Set initial y_position for middle of arrow

        // Draw arrow line
        arrowline = new Line(arrow.getLayoutX() - ARROW_LENGTH / 2 * Math.cos(ARROW_INITIAL_ANGLE),
                arrow.getLayoutY() + ARROW_LENGTH / 2 * Math.sin(ARROW_INITIAL_ANGLE),
                arrow.getLayoutX() + ARROW_LENGTH / 2 * Math.cos(ARROW_INITIAL_ANGLE),
                arrow.getLayoutY() - ARROW_LENGTH / 2 * Math.sin(ARROW_INITIAL_ANGLE));


        // Drawing arrow head
        double endX = arrowline.getEndX();
        double endY = arrowline.getEndY();
        double angle = Math.atan2(arrowline.getEndY() - arrowline.getStartY(), arrowline.getEndX() - arrowline.getStartX()) - Math.PI / 2;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double x1 = (-ARROW_HEAD_LENGTH * cos + ARROW_HEAD_LENGTH * sin) + endX;
        double y1 = (-ARROW_HEAD_LENGTH * sin - ARROW_HEAD_LENGTH * cos) + endY;
        double x2 = (ARROW_HEAD_LENGTH * cos + ARROW_HEAD_LENGTH * sin) + endX;
        double y2 = (ARROW_HEAD_LENGTH * sin - ARROW_HEAD_LENGTH * cos) + endY;

        arrowhead = new Polygon(endX, endY, x1, y1, x2, y2);
        arrowhead.setFill(Color.BLACK);


        // PosLabel is the label which shows the initial value of apple's and arrow's position on the top-left of the top pane
        // (0,0) is set on the bot-left
        posLabel = new Label(
                "Apple pos: (" + String.format("%.2f", screenWidth - apple_radius*2)+ " , "
                        + String.format("%.2f",topPane_height - apple_radius) +")"+"\n" +
                        "Arrow pos: " + "(" + String.format("%.2f",endX) +" , "+ String.format("%.2f",topPane_height - endY)+ ")"
        );
        posLabel.setAlignment(Pos.TOP_LEFT);
        posLabel.setStyle("-fx-padding: 15px;"+ "-fx-font-weight: Bold");

        // Creating cliff
        Rectangle cliff = new Rectangle(screenWidth-apple_radius-2.5, apple_radius*2-4,
                apple_radius, topPane_height); // (x, y, width, height)
        cliff.setFill(Color.BLACK);
        cliff.setArcHeight(10);
        cliff.setArcWidth(10);

        topPane.getChildren().addAll(apple, arrow, arrowline, arrowhead, posLabel, cliff);
    }


    private void CreateControlSectionElements(){
        createRepeatButton();
        createInputLabels();
        createInputValues();
        createStartButton();
        createPauseResumeButton();
        createExitButton();
        createResetButton();
    }

    private void showEndGamePopup() {
        endGameStage = new Stage();
        endGameStage.setTitle("BOOM!!");

        Label endGameLabel = new Label("You did Great! Thank you for playing.\n" +
                "Number of trials: "+ trial + "\n" +
                "Arrow velocity = " + ARROW_INITIAL_VELOCITY + "\n" +
                "Arrow Angle = " + ARROW_INITIAL_ANGLE_DEGREE + "\n" +
                "Apple Velocity = " + String.format("%.2f", apple_Vx));
        endGameLabel.setStyle("-fx-font-weight: bold;");
        VBox endGameLayout = new VBox(20);
        Button playAgainButton = new Button("Reset and Play Again?!");
        playAgainButton.setStyle("-fx-background-color: #f5eabc;"+ "-fx-font-weight: bold;" + "-fx-border-color: Black;" + "-fx-border-radius: 5;");

        // Effect of shadow for the PlayAgainButton
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GRAY);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        playAgainButton.setEffect(shadow);

        endGameLayout.getChildren().addAll(endGameLabel, playAgainButton);
        endGameLayout.setAlignment(Pos.CENTER);
        endGameLayout.setStyle("-fx-background-color: #cdffcd");

        Scene endGameScene = new Scene(endGameLayout, 300, 200);

        endGameStage.setScene(endGameScene);

        playAgainButton.setOnAction(event -> {
            endGameStage.close(); // Close the current pop-up window
            resetGame();
        });

        endGameStage.show();
    }


    private void createTimeline() {
        double timeStep = 0.01;

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(timeStep), e -> {
            t += timeStep;

            // Check if the trials reach Max number of trials
            if (trial>MAX_TRIAL){
                timeline.stop();
                resetGame();
            }
            else {

                ////////////// Using apple picture ///////////////////////////

                if (applePictureChecker()) {
                    // apple.getlayout will give the position of top left of apple picture
                    double appleCenterX = apple.getLayoutX() + apple_radius;
                    double appleCenterY = apple.getLayoutY() + apple_radius;

                    x_new_apple = appleCenterX - apple_Vx * timeStep;

                    apple_Vy = apple_Vy + GRAVITY * timeStep;
                    y_new_apple = appleCenterY + apple_Vy * timeStep;

                    apple.setLayoutX(x_new_apple - apple_radius);
                    apple.setLayoutY(y_new_apple - apple_radius);
                } else {
                    // Using circle as apple
                    x_new_apple = apple.getLayoutX() - apple_Vx * timeStep;

                    apple_Vy = apple_Vy + GRAVITY * timeStep;
                    y_new_apple = apple.getLayoutY() + apple_Vy *timeStep;
                    apple.setLayoutX(x_new_apple);
                    apple.setLayoutY(y_new_apple);
                }

                double X_middleArrow_prev = arrow.getLayoutX();
                double Y_middleArrow_prev = arrow.getLayoutY();

                double X_middleArrow_new = (arrow_Vx * timeStep) + X_middleArrow_prev;
                double Y_middleArrow_new = Y_middleArrow_prev - (arrow_Vy * timeStep);
                arrow_Vy = arrow_Vy - GRAVITY * timeStep;

                arrow.setLayoutX(X_middleArrow_new); // X-position of arrow head at t = 0
                arrow.setLayoutY(Y_middleArrow_new); // Y-position of arrow head at t = 0

                double changeInX = X_middleArrow_new - X_middleArrow_prev;
                double changeInY = -Y_middleArrow_new + Y_middleArrow_prev;

                // Calculate slope
                double slope = changeInY / changeInX;
                double current_theta = Math.atan(slope); // Finding angle of each iteration

                // Drawing new arrow line
                arrowline.setStartX(X_middleArrow_new - ARROW_LENGTH / 2 * Math.cos(current_theta));
                arrowline.setStartY(Y_middleArrow_new + ARROW_LENGTH / 2 * Math.sin(current_theta));
                arrowline.setEndX(X_middleArrow_new + ARROW_LENGTH / 2 * Math.cos(current_theta));
                arrowline.setEndY(Y_middleArrow_new - ARROW_LENGTH / 2 * Math.sin(current_theta));


                // Calculate and Draw new arrow head points
                double angle = Math.atan2(arrowline.getEndY() - arrowline.getStartY(), arrowline.getEndX() - arrowline.getStartX()) - Math.PI / 2;
                double sin = Math.sin(angle);
                double cos = Math.cos(angle);
                arrowhead.getPoints().set(0, arrowline.getEndX()); // Set the x1 value at index 0
                arrowhead.getPoints().set(1, arrowline.getEndY()); // Set the x1 value at index 1
                arrowhead.getPoints().set(2, (-ARROW_HEAD_LENGTH * cos + ARROW_HEAD_LENGTH * sin) + arrowline.getEndX()); // Set the x1 value at index 2
                arrowhead.getPoints().set(3, (-ARROW_HEAD_LENGTH * sin - ARROW_HEAD_LENGTH * cos) + arrowline.getEndY()); // Set the y1 value at index 3
                arrowhead.getPoints().set(4, (ARROW_HEAD_LENGTH * cos + ARROW_HEAD_LENGTH * sin) + arrowline.getEndX()); // Set the x2 value at index 4
                arrowhead.getPoints().set(5, (ARROW_HEAD_LENGTH * sin - ARROW_HEAD_LENGTH * cos) + arrowline.getEndY()); // Set the y2 value at index 5

                // Updating PosLabel which shows apple and arrow position
                updatePosLabel(x_new_apple, y_new_apple, arrowline.getEndX(), arrowline.getEndY());

                // Check if is hit or not
                if (isHit(x_new_apple, y_new_apple, arrowline.getEndX(), arrowline.getEndY())) {
                    showEndGamePopup();
                }

                // Check if apple of arrow are on the border of the screen
                if (y_new_apple >= topPane_height - apple_radius ||
                        x_new_apple + apple_radius < 0||
                        arrowline.getEndX()>= topPane.getWidth() ||
                        arrowline.getEndY() <=0 || arrowline.getEndY() >=topPane_height) {

                    RUNNING = false;
                    timeline.stop();
                }
            }
        });

        timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private boolean isHit(double x_new_apple, double y_new_apple, double endX, double endY){

        if (Math.sqrt(Math.pow(y_new_apple-endY, 2) + Math.pow(x_new_apple-endX,2)) <= apple_radius) {
            timeline.stop();
            return true;
        } else {
            return false;
        }
    }

    private void createRepeatButton() {
        Button repeatButton = new Button("Repeat Animation");
        repeatButton.setPrefWidth(screenWidth *0.08); // Set the preferred width
        repeatButton.setPrefHeight(12); // Set the preferred height
        repeatButton.setStyle("-fx-font-family: ''; -fx-font-size: 10px; -fx-font-weight: bold;");
        controlSection.add(repeatButton, 2, 2);
        GridPane.setMargin(repeatButton, new Insets(10));


        repeatButton.setOnAction(e -> {
            setupElements(topPane);
            if (PLAYED) {
                pauseResumeButton.setText("Pause");
                RUNNING = true;
                timeline.play();
            }
        });

    }

    private void createInputLabels() {

        Label V_Arrow_label = new Label("Initial Arrow Velocity:");
        Label theta_Arrow_label = new Label("Initial Arrow Angle (degrees):");

        V_Arrow_label.setStyle("-fx-font-family: ''; -fx-font-size: 12px; -fx-font-weight: bold;");
        theta_Arrow_label.setStyle("-fx-font-family: ''; -fx-font-size: 12px; -fx-font-weight: bold;");

        controlSection.add(V_Arrow_label, 0, 0);
        controlSection.add(theta_Arrow_label, 0, 1);

        GridPane.setMargin(V_Arrow_label, new Insets(10));
        GridPane.setMargin(theta_Arrow_label, new Insets(10));
    }


    private Label instructionLabel(){
        instruction_label = new Label("Instructions:\n" +
                "- Enter the initial V and theta, then select 'Start'.\n" +
                "- Repeating won't affect the trial count.\n" +
                "- You have a maximum of 5 trials, so choose wisely!\n" +
                "- To exit, press the 'Exit' button or use the Esc key.\n" +
                "- Good Luck!");
        instruction_label.setPrefWidth(screenWidth*0.3);
        instruction_label.setPrefHeight(screenHeight*0.2);
        instruction_label.setStyle("-fx-border-color: black; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 5px; "+
                "-fx-padding: 5px;"+
                "-fx-background-color: rgb(252,242,242);");
        Font customFont = new Font("", screenWidth*0.01);

        instruction_label.setFont(customFont);
        instruction_label.setAlignment(Pos.TOP_LEFT);
        return instruction_label;

    }

    private Label createLogLabels(){

        loglabel = new Label("Log: ");
        loglabel.setPrefWidth(screenWidth*0.4);
        loglabel.setPrefHeight(screenHeight*0.2);
        loglabel.setStyle("-fx-border-color: black; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 5px; "+
                "-fx-padding: 5px;"+
                "-fx-background-color: rgb(252,242,242);"+
                "-fx-font-weight: BOLD");
        Font customFont = new Font("", screenWidth*0.01); // Set your desired font family and size

        loglabel.setFont(customFont);
        loglabel.setAlignment(Pos.TOP_LEFT);
        return loglabel;

    }

    private void createInputValues() {
        tf_V = new TextField();
        tf_theta = new TextField();

        tf_theta.setPromptText(String.valueOf(ARROW_INITIAL_ANGLE_DEGREE));
        tf_theta.setStyle("-fx-font-family: ''; -fx-font-size: 12px; -fx-font-weight: bold");

        tf_V.setPromptText(String.valueOf(ARROW_INITIAL_VELOCITY));
        tf_V.setStyle("-fx-font-family: ''; -fx-font-size: 12px; -fx-font-weight: bold");

        controlSection.add(tf_V, 1, 0);
        controlSection.add(tf_theta, 1, 1);

        tf_V.requestFocus();

        GridPane.setMargin(tf_V, new Insets(10));
        GridPane.setMargin(tf_theta, new Insets(10));

        // Handler for tf_V (for ARROW_VELOCITY)
        tf_V.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double newVelocity = Double.parseDouble(newValue);
                setArrowVelocity(newVelocity);
            } catch (NumberFormatException e) {
            }
        });

        // Handler for tf_theta (for ARROW_ANGLE)
        tf_theta.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double newAngle = Double.parseDouble(newValue);
                setArrowAngle(newAngle);
            } catch (NumberFormatException e) {

            }
        });

    }

    private void createStartButton() {
        startButton = new Button("Start");
        startButton.setPrefWidth(screenWidth *0.08); // Set the preferred width
        startButton.setPrefHeight(12); // Set the preferred height
        startButton.setStyle("-fx-font-family: ''; -fx-font-size: 10px; -fx-font-weight: bold;");

        startButton.setOnAction(e -> {
            trial += 1;
            if (trial==5){
                startButton.setText("Reset");
            }
            if (trial<5){
                startButton.setText("Start");
            }
            PLAYED = true;
            RUNNING = true;
            setupElements(topPane);
            tf_theta.setPromptText(String.valueOf(ARROW_INITIAL_ANGLE_DEGREE)); // set hint for text field
            tf_V.setPromptText(String.valueOf(ARROW_INITIAL_VELOCITY)); // set hint for text field
            pauseResumeButton.setText("Pause");
            updateLogLabel();
            timeline.play();

        });

        controlSection.add(startButton, 2, 0);
        GridPane.setMargin(startButton, new Insets(10));
    }


    private void updatePosLabel(double x_new_apple, double y_new_apple, double endX, double endY) {
        String updatedText = ("Apple pos: (" +
                String.format("%.2f", x_new_apple)+ " , "
                + String.format("%.2f",topPane_height-y_new_apple) +")"+"\n" +
                "Arrow pos: " + "(" +
                String.format("%.2f",endX) +" , "+
                String.format("%.2f",topPane_height- endY)+ ")");
        posLabel.setText(updatedText);
    }

    private void updateLogLabel(){
        String existingText = loglabel.getText(); // Get existing text
        // Concatenate new text
        String updatedText = existingText + "\n" +
                "Trial #" + trial +
                " | Apple velocity = " + String.format("%.2f", apple_Vx) +
                " | Arrow velocity = " + ARROW_INITIAL_VELOCITY + " | Arrow angle = " + ARROW_INITIAL_ANGLE_DEGREE
                ;
        loglabel.setText(updatedText); // Set updated text to the label
    }

    private void createExitButton() {
        exitButton = new Button("Exit");
        exitButton.setPrefWidth(screenWidth * 0.08);
        exitButton.setPrefHeight(12);
        exitButton.setStyle("-fx-font-family: ''; -fx-font-size: 10px; -fx-font-weight: bold;");

        controlSection.add(exitButton, 0, 2);
        GridPane.setMargin(exitButton, new Insets(10));
    }

    private void createPauseResumeButton() {
        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setPrefWidth(screenWidth *0.08);
        pauseResumeButton.setPrefHeight(12);
        GridPane.setMargin(pauseResumeButton, new Insets(10));
        pauseResumeButton.setStyle("-fx-font-family: ''; -fx-font-size: 10px; -fx-font-weight: bold;");
        pauseResumeButton.setOnAction(e -> {
            if (PLAYED) {
                if (pauseResumeButton.getText() == "Pause") {
                    timeline.stop();
                    pauseResumeButton.setText("Resume");
                    PLAYED = true;
                    RUNNING = false;
                } else if (pauseResumeButton.getText() == "Resume") {
                    timeline.play();
                    pauseResumeButton.setText("Pause");
                    PLAYED = true;
                    RUNNING = true;
                }
            }

        });

        controlSection.add(pauseResumeButton, 2, 1);
        GridPane.setMargin(pauseResumeButton, new Insets(10));
    }

    private void createResetButton() {
        resetButton = new Button("Reset the Game!");
        resetButton.setPrefWidth(screenWidth *0.08);
        resetButton.setPrefHeight(12);
        GridPane.setMargin(resetButton, new Insets(10));
        resetButton.setStyle("-fx-font-family: ''; -fx-font-size: 10px; -fx-font-weight: bold;");
        resetButton.setOnAction(e -> {
            resetGame();

        });

        controlSection.add(resetButton, 1, 2);
        GridPane.setMargin(resetButton, new Insets(10));
    }

    private void resetGame(){
        PLAYED = false;
        RUNNING = false;
        trial = 0;
        apple_Vx = randomVelGenerator();
        setupElements(topPane);
        tf_V.clear();
        tf_theta.clear();
        timeline.stop();
        loglabel.setText("Log: ");
        startButton.setText("Start");

    }

    // Setter function for ARROW_VELOCITY
    private void setArrowVelocity(double velocity) {
        ARROW_INITIAL_VELOCITY = velocity;
        if (!RUNNING) {
            setupElements(topPane);
        }
    }

    // Setter function for ARROW_ANGLE
    private void setArrowAngle(double angle) {
        ARROW_INITIAL_ANGLE_DEGREE = angle;
        ARROW_INITIAL_ANGLE = angle * Math.PI / 180; // Make sure ARROW_ANGLE is not final

        if (!RUNNING) {
            setupElements(topPane);
        }
    }

    private double randomVelGenerator(){
        return (10+ new Random().nextDouble() * (20 - 10));
//        return (35+ new Random().nextDouble() * (105 - 35));
    }


    public static void main(String[] args) {
        launch(args);
    }
}