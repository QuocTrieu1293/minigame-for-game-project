package Application;

import java.io.File;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXButton.ButtonType;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class game {
	Stage stage = null;
	Scene scene = null;
	final Double INIT_ROAD_TIME = 4.0; // seconds
	SecureRandom rand = new SecureRandom();
	Long time = 0l;
	Long score = 0l;
	AnimationTimer gameloop = null;
	ArrayList<Animation> animationList = new ArrayList<>();
	Pane root = new Pane();
	Pane gamePane = new Pane();
	Canvas c = new Canvas();
	GraphicsContext gc = c.getGraphicsContext2D();

	// road
	final String BACKGROUND_IMAGE_PATH = "/application/minigameBKG.png";
	Image roadImg = new Image(getClass().getResource(BACKGROUND_IMAGE_PATH).toExternalForm());
	ImageView road1 = new ImageView(roadImg);
	ImageView road2 = new ImageView(roadImg);
	Timeline roadAnimation = null;

	// car
	ImageView car = new ImageView();
	Image carImg1 = new Image(getClass().getResource("/application/policecar1.png").toExternalForm());
	Image carImg2 = new Image(getClass().getResource("/application/policecar2.png").toExternalForm());
	Image carImg3 = new Image(getClass().getResource("/application/policecar3.png").toExternalForm());
	final double INIT_CAR_SPEED = 7;
	final double CAR_HEIGHT = carImg1.getHeight();
	final double CAR_WIDTH = carImg1.getWidth();
	final double MIN_XCAR = 163.0;
	final double MAX_XCAR = 888 - CAR_WIDTH - 163;
	final double MIN_YCAR = 10;
	final double MAX_YCAR = 610 - CAR_HEIGHT;
	Double carSpeed = INIT_CAR_SPEED; 

	// car controller
	BooleanProperty wPressed = new SimpleBooleanProperty();
	BooleanProperty sPressed = new SimpleBooleanProperty();
	BooleanProperty aPressed = new SimpleBooleanProperty();
	BooleanProperty dPressed = new SimpleBooleanProperty();
	BooleanBinding keyPressed = wPressed.or(aPressed).or(sPressed).or(dPressed);
	AnimationTimer carController = new AnimationTimer() {

		@Override
		public void handle(long arg0) {
			if (wPressed.get()) {
				if (car.getLayoutY() + car.getY() - carSpeed < MIN_YCAR) {
//					car.setLayoutY(MIN_YCAR);
//					car.setY(0);
					car.setY(MIN_YCAR-car.getLayoutY());
				} else
					car.setY(car.getY() - carSpeed);
			}

			if (sPressed.get()) {
				if (car.getLayoutY() + car.getY() + carSpeed >= MAX_YCAR) {
//					car.setLayoutY(MAX_YCAR);
//					car.setY(0);
					car.setY(MAX_YCAR-car.getLayoutY());
				} else
					car.setY(car.getY() + carSpeed);
			}

			if (aPressed.get()) {
				if (car.getLayoutX() + car.getX() - carSpeed <= MIN_XCAR) {
//					car.setLayoutX(MIN_XCAR);
//					car.setX(0);
					car.setX(MIN_XCAR-car.getLayoutX());
				} else
					car.setX(car.getX() - carSpeed);
			} else {
//				intend to make turn left animation
			}

			if (dPressed.get()) {
				if (car.getLayoutX() + car.getX() + carSpeed >= MAX_XCAR) {
//					car.setLayoutX(MAX_XCAR);
//					car.setX(0);
					car.setX(MAX_XCAR-car.getLayoutX());
				} else
					car.setX(car.getX() + carSpeed);
			} else {
//				intend to make turn right animation
			}
		}
	};
	ChangeListener<Boolean> keyPressChangeListener = (observableVal, oldVal, newVal) -> {
		if (newVal)
			carController.start();
		else
			carController.stop();
	};

	// obstacle
	final String OBSTACLE_IMAGE_PATH = "/application/minigameObstacle.png";
	Image obstacleImg = new Image(getClass().getResource(OBSTACLE_IMAGE_PATH).toExternalForm());
	Pane obstaclePane = new Pane();
	final double OBS_HEIGHT = obstacleImg.getHeight();
	final double OBS_WIDTH = obstacleImg.getWidth();
	ArrayList<Obstacle> obstacleList = new ArrayList<>();
	class Obstacle {
		public ImageView imgV;
		public Rectangle collisionRec;
		public Obstacle() {
			imgV = new ImageView(obstacleImg);
			collisionRec = new Rectangle(OBS_WIDTH-1, 8, Paint.valueOf("transparent"));
			collisionRec.layoutXProperty().bind(imgV.layoutXProperty().add(0.5));
			collisionRec.layoutYProperty().bind(imgV.layoutYProperty().add(OBS_HEIGHT-9));
			collisionRec.xProperty().bind(imgV.xProperty());
			collisionRec.yProperty().bind(imgV.yProperty());
			collisionRec.translateYProperty().bind(imgV.translateYProperty());
			
			double randomX = rand.nextDouble() * (MAX_XCAR - MIN_XCAR) + MIN_XCAR;
			imgV.setLayoutX(randomX);
			imgV.setLayoutY(-OBS_HEIGHT);
			obstaclePane.getChildren().addAll(imgV,collisionRec);
		
			// Animation
			TranslateTransition moveObs = new TranslateTransition();
			animationList.add(moveObs);
			moveObs.setNode(imgV);
			moveObs.setFromY(0);
			moveObs.setToY(619 + OBS_HEIGHT);
			moveObs.setDuration(Duration.seconds(INIT_ROAD_TIME));
			moveObs.rateProperty().bind(roadAnimation.rateProperty());
			moveObs.play();
			moveObs.setOnFinished((event) -> {
				obstaclePane.getChildren().removeAll(imgV,collisionRec);
				animationList.remove(moveObs);
				obstacleList.remove(this);
			});
			obstacleList.add(this);
		}
	}
	// COIN
	final String COIN_IMAGE_PATH = "/application/minigameCoin.png";
	Image coinImg = new Image(getClass().getResource(COIN_IMAGE_PATH).toExternalForm());
	final double COIN_HEIGHT = 35.0;
	final double COIN_WIDTH = 35.0;
	Pane coinPane = new Pane();
	ObservableList<Node> coinList = coinPane.getChildren();
	void createCoin() {
		ImageView coin = new ImageView(coinImg);
		coinList.add(coin);
		Rectangle2D vp = new Rectangle2D(0, 0, COIN_WIDTH, COIN_HEIGHT);
		coin.setViewport(vp);
		
		double randomX = rand.nextDouble() * (MAX_XCAR - MIN_XCAR) + MIN_XCAR;
		coin.setLayoutX(randomX);
		coin.setLayoutY(-COIN_HEIGHT);
		
//		spinning coin Animation
		KeyFrame coinKF1 = new KeyFrame(Duration.ZERO, 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(0, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF2 = new KeyFrame(Duration.millis(100), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF3 = new KeyFrame(Duration.millis(200), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*2, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF4 = new KeyFrame(Duration.millis(300), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*3, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF5 = new KeyFrame(Duration.millis(400), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*4, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF6 = new KeyFrame(Duration.millis(500), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*5, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF7 = new KeyFrame(Duration.millis(600), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*6, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF8 = new KeyFrame(Duration.millis(700), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*7, 0, COIN_WIDTH, COIN_HEIGHT)));
		KeyFrame coinKF9 = new KeyFrame(Duration.millis(800), 
				new KeyValue(coin.viewportProperty(),new Rectangle2D(COIN_WIDTH*8, 0, COIN_WIDTH, COIN_HEIGHT)));
		
		Timeline coinAnimation = new Timeline(coinKF1,coinKF2,coinKF3,coinKF4,coinKF5,coinKF6,coinKF7,coinKF8,coinKF9);
		coinAnimation.setCycleCount(Animation.INDEFINITE);
		coinAnimation.rateProperty().bind(roadAnimation.rateProperty());
		coinAnimation.play();
		
//		transition coin animation
		TranslateTransition moveCoin = new TranslateTransition();
		animationList.add(moveCoin);
		moveCoin.setNode(coin);
		moveCoin.setFromY(0);
		moveCoin.setToY(619 + COIN_HEIGHT);
		moveCoin.setDuration(Duration.seconds(INIT_ROAD_TIME));
		moveCoin.rateProperty().bind(roadAnimation.rateProperty());
		moveCoin.play();
		moveCoin.setOnFinished((event) -> {
			animationList.remove(moveCoin);
			coinList.remove(coin);
		});
	}
	
//	polygon for car collision
//	Polygon rightTriangle = new Polygon();
//	Polygon leftTriangle = new Polygon();
	
	//Audio
	AudioClip crashSound = new AudioClip(Paths.get("src/Application/minigameCrashSound.wav").toUri().toString());
	AudioClip levelupSound = new AudioClip(Paths.get("src/Application/minigameLevelupSound.mp3").toUri().toString());
	MediaPlayer policeCarSound = new MediaPlayer(
		new Media(Paths.get("src/Application/policeSound.mp3").toUri().toString()));
	MediaPlayer backgroundMusic = new MediaPlayer(
		new Media(Paths.get("src/Application/backgroundSound.mp3").toUri().toString()));
	AudioClip gameStartSound = new AudioClip(Paths.get("src/Application/gameStartSound.mp3").toUri().toString());
	
	public game(Stage stage) {
		this.stage = stage;
		root.setPrefWidth(888);
		root.setPrefWidth(619);
		car.setLayoutY(MAX_YCAR);
		car.setLayoutX(444 - CAR_WIDTH / 2);
		gamePane.getChildren().addAll(road1,road2,car,coinPane,obstaclePane);
		root.getChildren().addAll(gamePane,c);
		obstaclePane.setLayoutX(0);
		obstaclePane.setLayoutY(0);
		obstaclePane.setPrefWidth(888);
		obstaclePane.setPrefHeight(619);
		scene = new Scene(root, 888, 619);
		stage.setScene(scene);

		KeyValue startRoad1KV = new KeyValue(road1.yProperty(), 0);
		KeyValue startRoad2KV = new KeyValue(road2.yProperty(), -roadImg.getHeight());
		KeyValue endRoad1KV = new KeyValue(road1.yProperty(), 619);
		KeyValue endRoad2KV = new KeyValue(road2.yProperty(), 619);
		KeyValue road1KV = new KeyValue(road1.yProperty(), -roadImg.getHeight());

		KeyFrame startRoad1KF = new KeyFrame(Duration.ZERO, startRoad1KV);
		KeyFrame startRoad2KF = new KeyFrame(Duration.ZERO, startRoad2KV);
		KeyFrame endRoad1KF = new KeyFrame(Duration.seconds(INIT_ROAD_TIME), endRoad1KV);
		KeyFrame endRoad2KF = new KeyFrame(Duration.seconds(INIT_ROAD_TIME * 2), endRoad2KV);
		KeyFrame road1KF = new KeyFrame(Duration.seconds(INIT_ROAD_TIME), road1KV);
		KeyFrame road1KF_ = new KeyFrame(Duration.seconds(INIT_ROAD_TIME * 2), startRoad1KV);

		roadAnimation = new Timeline(startRoad1KF, startRoad2KF, endRoad1KF, endRoad2KF, road1KF, road1KF_);
		animationList.add(roadAnimation);
		roadAnimation.setCycleCount(Animation.INDEFINITE);
		roadAnimation.play();
		roadAnimation.setRate(1);

		KeyFrame carKV1 = new KeyFrame(Duration.ZERO, new KeyValue(car.imageProperty(), carImg1));
		KeyFrame carKV2 = new KeyFrame(Duration.seconds(0.5), new KeyValue(car.imageProperty(), carImg2));
		KeyFrame carKV3 = new KeyFrame(Duration.seconds(0.25), new KeyValue(car.imageProperty(), carImg3));
		Timeline policeAnimation = new Timeline(carKV1, carKV2, carKV3);
		policeAnimation.setCycleCount(Animation.INDEFINITE);
		policeAnimation.play();
		policeAnimation.rateProperty().bind(roadAnimation.rateProperty());
		
		// car controller
		keyPressed.addListener(keyPressChangeListener);
		root.setOnKeyPressed((event) -> {
			KeyCode key = event.getCode();
			if (key == KeyCode.W || key == KeyCode.UP)
				wPressed.set(true);
			if (key == KeyCode.S || key == KeyCode.DOWN)
				sPressed.set(true);
			if (key == KeyCode.A || key == KeyCode.LEFT)
				aPressed.set(true);
			if (key == KeyCode.D || key == KeyCode.RIGHT)
				dPressed.set(true);
		});
		root.setOnKeyReleased((event) -> {
			KeyCode key = event.getCode();
			if (key == KeyCode.W || key == KeyCode.UP)
				wPressed.set(false);
			if (key == KeyCode.S || key == KeyCode.DOWN)
				sPressed.set(false);
			if (key == KeyCode.A || key == KeyCode.LEFT)
				aPressed.set(false);
			if (key == KeyCode.D || key == KeyCode.RIGHT)
				dPressed.set(false);
		});
		
		//canvas
		c.setLayoutX(0); c.setLayoutY(0);
		c.setWidth(888); c.setHeight(619);
		gc.setFont(Font.font("Arial",FontWeight.EXTRA_BOLD,34));
		gc.setFill(Paint.valueOf("#ff1a5f"));
		gc.setStroke(Paint.valueOf("red"));
		gc.setLineWidth(1);
		gc.fillText("SCORE", 10, 35);
		gc.strokeText("SCORE", 10, 35);
		gc.setFill(Paint.valueOf("#FFFFFF"));
		drawScore();
		
//		polygon for car collision
//		leftTriangle.getPoints().addAll(new Double[] {
//				car.getLayoutX(),car.getLayoutY(),
//				car.getLayoutX()+carImg1.getWidth()/2-15,car.getLayoutY(),
//				car.getLayoutX(),car.getLayoutY()+25
//		});
//		
//		rightTriangle.getPoints().addAll(new Double[] {
//				car.getLayoutX()+carImg1.getWidth()/2+15,car.getLayoutY(),
//				car.getLayoutX()+carImg1.getWidth(),car.getLayoutY(),
//				car.getLayoutX()+carImg1.getWidth(),car.getLayoutY()+25
//		});
//		
//		rightTriangle.setFill(Paint.valueOf("transparent")); 
//		rightTriangle.setStrokeWidth(0.5); rightTriangle.setStroke(Paint.valueOf("red"));
//		leftTriangle.setFill(Paint.valueOf("transparent"));
//		leftTriangle.setStrokeWidth(0.5); leftTriangle.setStroke(Paint.valueOf("red"));
//		
//		rightTriangle.translateXProperty().bind(car.xProperty());
//		rightTriangle.translateYProperty().bind(car.yProperty());
//		
//		leftTriangle.translateXProperty().bind(car.xProperty());
//		leftTriangle.translateYProperty().bind(car.yProperty());

		//Audio
		backgroundMusic.setCycleCount(Integer.MAX_VALUE);
		policeCarSound.setCycleCount(Integer.MAX_VALUE);
		policeCarSound.setVolume(0.4);
		
//		game start here
		gameloop = new AnimationTimer() {

			@Override
			public void handle(long arg0) {
				update();
			}
		};
		
		root.requestFocus();
		gameStartSound.play();
		backgroundMusic.play();
		policeCarSound.play();
		new Obstacle();
		createCoin();
		gameloop.start();
	}

	void update() {
		time++;

		// born obstacle
		if (time % 50 == 0 && time % 100 != 0) {
			int r = rand.nextInt(4);
			if (r == 0 || r == 1) {
				randomObstacle();
			}
		}
		if (time % 100 == 0) {
			randomObstacle();
		}
		
		//born coin
		if(time % 10==0) {
			if(rand.nextInt(7) == 1) 
				randomCoin();
		}

		// speed up
		if (time % 300 == 0) {
			levelupSound.play();
			roadAnimation.setRate(roadAnimation.getRate() + 0.25);
			carSpeed = (roadAnimation.getRate()+3)*0.25*INIT_CAR_SPEED;
			System.out.println("rate: " + roadAnimation.getRate());
//			System.out.println("carSpeed: " + carSpeed);
		}
		
		// check collision with coins
		ArrayList<Node> removedCoinList = new ArrayList<>();
		for(Node node : coinList) {
			ImageView coin = (ImageView)node;
			if(car.getBoundsInParent().intersects(coin.getBoundsInParent())) {
				AudioClip coinSound = new AudioClip(Paths.get("src/Application/minigameCoinSound.wav").toUri().toString());
				coinSound.play();
				score += Math.round(roadAnimation.getRate()*10);
//				System.out.println("score: " + score);
				drawScore();
				removedCoinList.add(coin);
			}
		}
		coinList.removeAll(removedCoinList);
		
		// check collision with obstacles
		for (Obstacle obs : obstacleList) {
			if (car.getBoundsInParent().intersects(obs.collisionRec.getBoundsInParent())) 
			{
				double minY = car.getLayoutY() + car.getY();
				double maxY = minY + 25;
				double obsMaxY = obs.collisionRec.getTranslateY();
				if(minY <= obsMaxY && obsMaxY <= maxY) {
					double minX_left = car.getLayoutX() + car.getX();
					double maxX_left = minX_left + ((maxY-obsMaxY)/25)*(carImg1.getWidth()/2-15);
					
					double maxX_right = car.getLayoutX() + car.getX() + carImg1.getWidth();
					double minX_right = maxX_right - ((maxY-obsMaxY)/25)*(carImg1.getWidth()/2-15);
					
					double obsMinX = obs.collisionRec.getLayoutX() + obs.collisionRec.getX();
					double obsMaxX = obsMinX + obs.collisionRec.getWidth();
					
					if(!(minX_left <= obsMaxX && obsMaxX <= maxX_left) && !(minX_right <= obsMinX && obsMinX <= maxX_right)) {
						gameOver();
					}
				}else {
					gameOver();
				}
			}
		}
	}

	void randomObstacle() {
		int num = rand.nextInt(4) + 1;
		score += Math.round(num*2*roadAnimation.getRate());
//		System.out.println("score: " + score);
		drawScore();
		while (num-- != 0) {
			new Obstacle();
		}
	}
	
	void drawScore() {
		gc.clearRect(0, 40, 175, 70);
//		gc.fillRect(0, 40, 175, 70);
		Text text = new Text(score.toString());
		text.setFont(Font.font("Arial",FontWeight.EXTRA_BOLD,34));
		gc.setFill(Paint.valueOf("#30A2FF")); gc.setStroke(Paint.valueOf("#205E61"));
		gc.setLineWidth(1.5);
		gc.fillText(score.toString(),70-text.getBoundsInParent().getWidth()/2,75);
		gc.strokeText(score.toString(),70-text.getBoundsInParent().getWidth()/2,75);
	}
	
	void randomCoin() {
		int num = rand.nextInt(7) + 1;
		
		while (num-- != 0) {
			createCoin();
		}
	}
		
	void gameOver() {
		crashSound.play();
		policeCarSound.stop();
		backgroundMusic.stop();
//		System.out.println("GAME OVER");
		gameloop.stop();
		carController.stop();
		
		AudioClip buttonSound = new AudioClip(Paths.get("src/Application/buttonSound.mp3").toUri().toString());		
		
		for (Animation ani : animationList)
			ani.stop();
		keyPressed.removeListener(keyPressChangeListener);
		gc.clearRect(0, 0, 888, 619);
		GaussianBlur blur = new GaussianBlur(7);
		gamePane.setEffect(blur);
		gc.setFont(Font.loadFont(getClass().getResource("/Application/Kaph-Regular.ttf").toExternalForm(), 70));
		gc.setFill(Paint.valueOf("red"));
		gc.fillText("GAME OVER", 190, 200);
		gc.setStroke(Paint.valueOf("yellow"));
		gc.setLineWidth(3);
		gc.strokeText("GAME OVER", 190, 200);
		Font badComic = Font.loadFont(getClass().getResource("/Application/BadComic-Regular.ttf").toExternalForm(), 50);
		gc.setFont(badComic);
		gc.setFill(Paint.valueOf("#F9F5F6"));
		Text text = new Text("Score: " + score.toString());
		text.setFont(badComic);
		gc.fillText("Score: " + score.toString(), 444-text.getBoundsInParent().getWidth()/2, 267);	
		gc.setStroke(Paint.valueOf("black")); 
		gc.strokeText("Score: " + score.toString(), 444-text.getBoundsInParent().getWidth()/2, 267);	
		
		Image coin1 = new Image(getClass().getResource("/Application/minigameCoin1.png").toExternalForm());
		Image coin2 = new Image(getClass().getResource("/Application/minigameCoin2.png").toExternalForm());
		Image coin3 = new Image(getClass().getResource("/Application/minigameCoin3.png").toExternalForm());
		Image coin4 = new Image(getClass().getResource("/Application/minigameCoin4.png").toExternalForm());
		Image coin5 = new Image(getClass().getResource("/Application/minigameCoin5.png").toExternalForm());
		Image coin6 = new Image(getClass().getResource("/Application/minigameCoin6.png").toExternalForm());
		
		ImageView coin = new ImageView();
		coin.setFitHeight(150); coin.setFitWidth(150);
		coin.setLayoutX(444-coin.getFitWidth()/2); coin.setLayoutY(280);
		root.getChildren().add(coin);
		KeyFrame coinKF1 = new KeyFrame(Duration.ZERO,
				new KeyValue(coin.imageProperty(), coin1));
		KeyFrame coinKF2 = new KeyFrame(Duration.millis(100),
				new KeyValue(coin.imageProperty(), coin2));
		KeyFrame coinKF3 = new KeyFrame(Duration.millis(200),
				new KeyValue(coin.imageProperty(), coin3));
		KeyFrame coinKF4 = new KeyFrame(Duration.millis(300),
				new KeyValue(coin.imageProperty(), coin4));
		KeyFrame coinKF5 = new KeyFrame(Duration.millis(400),
				new KeyValue(coin.imageProperty(), coin5));
		KeyFrame coinKF6 = new KeyFrame(Duration.millis(500),
				new KeyValue(coin.imageProperty(), coin6));
		KeyFrame coinKF7 = new KeyFrame(Duration.millis(600),
				new KeyValue(coin.imageProperty(), coin1));
		Timeline coinSpinningAnimation = new Timeline(coinKF1,coinKF2,coinKF3,coinKF4,
														coinKF5,coinKF6,coinKF7);
		coinSpinningAnimation.setCycleCount(Animation.INDEFINITE);
		coinSpinningAnimation.play();
		
		gc.setFont(Font.loadFont(getClass().getResource("/Application/BadComic-Regular.ttf").toExternalForm(), 36)); 
		gc.setFill(Paint.valueOf("#F7FD04"));
		text.setText("+"+Math.round(score/10.0));
		gc.fillText(text.getText(), 444-text.getBoundsInParent().getWidth()/2, 455);
		gc.setLineWidth(1.5); gc.setStroke(Paint.valueOf("black"));
		gc.strokeText(text.getText(), 444-text.getBoundsInParent().getWidth()/2, 455);
		
		text.setText("You have earned " + Math.round(score/10.0) + " gold");
		text.setFont(gc.getFont());
		gc.setFill(Paint.valueOf("#F49D1A"));
		gc.fillText(text.getText(), 444-text.getBoundsInParent().getWidth()/2, 500);
		gc.setStroke(Paint.valueOf("#F0FF42")); gc.setLineWidth(1);
		gc.strokeText(text.getText(), 444-text.getBoundsInParent().getWidth()/2, 500);

		JFXButton backButton = new JFXButton();
		JFXButton playButton = new JFXButton();
		
		Image backButtonImg = new Image(getClass().
				getResource("/Application/minigameBackButton.png").toExternalForm());
		Image backButtonColoredImg = new Image(getClass().
				getResource("/Application/minigameColoredBackButton.png").toExternalForm());
		Image playButtonImg = new Image(getClass().
				getResource("/Application/minigamePlayButton.png").toExternalForm());
		Image playButtonColoredImg = new Image(getClass().
				getResource("/Application/minigameColoredPlayButton.png").toExternalForm());
		
		ImageView backButtonImgV = new ImageView(backButtonImg);
		ImageView playButtonImgV = new ImageView(playButtonImg);
		backButtonImgV.setPreserveRatio(true); playButtonImgV.setPreserveRatio(true);
		backButtonImgV.setFitWidth(267); playButtonImgV.setFitWidth(267);
		
		backButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
		playButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		backButton.setGraphic(backButtonImgV); 
		playButton.setGraphic(playButtonImgV);
		
		backButton.setLayoutX(25); backButton.setLayoutY(619/2-backButtonImgV.getFitHeight()/2);
		playButton.setLayoutX(888-25-playButtonImgV.getFitWidth()); playButton.setLayoutY(backButton.getLayoutY());
		
		backButton.setCursor(Cursor.HAND);
		playButton.setCursor(Cursor.HAND);

		backButton.focusedProperty().addListener((observableVal,oldVal,newVal)->{
			if(newVal) {
				buttonSound.play();
				backButtonImgV.setImage(backButtonColoredImg);
			}else {
				backButtonImgV.setImage(backButtonImg);
			}
		});
		playButton.focusedProperty().addListener((observableVal,oldVal,newVal)->{
			if(newVal) {
				buttonSound.play();
				playButtonImgV.setImage(playButtonColoredImg);
			}else {
				playButtonImgV.setImage(playButtonImg);
			}
		});
		
		playButton.setOnAction((event)->{
			new game(this.stage);
		});
		
		root.setOnKeyPressed((event)->{
			KeyCode key = event.getCode();
			if(key == KeyCode.LEFT)
				backButton.requestFocus();
			else if(key == KeyCode.RIGHT)
				playButton.requestFocus();
		});
		
		root.getChildren().addAll(backButton,playButton);
	}

}
