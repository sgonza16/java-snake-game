import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener
	, KeyListener {
	private class Tile{
		
		int x;
		int y;
		
		Tile(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
	int boardWidth;
	int boardHeight;
	int tileSize = 43;
	int gridCols = 25;
	int gridRows = 25;
	
	
	//Snake
	Tile snakeHead;
	Image snakeEyes;
	Image snakeBackground;
	ArrayList<Tile> snakeBody;
	//Food
	Tile Food;
	Random random;
	Color foodColor;
	
	//game logic
	Timer gameLoop;
	Timer flashTimer;
	Color gameOverColor = Color.red;
	int velocityX;
	int velocityY;
	boolean gameOver=false;
	boolean showGameOver = true;
	int highScore = 0;
	
	//play again
	JButton playAgainButton;
	
	//Food color options
	private static final Color[] Colors = { Color.red, Color.blue, Color.pink, Color.orange, Color.yellow};
	
	SnakeGame(int boardWidth, int boardHeight){
		this.boardWidth = boardWidth;
		this.boardHeight = boardHeight;
		setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
		setBackground(Color.black);
		addKeyListener(this);
		setFocusable(true);
		
		//play again button
		playAgainButton = new JButton("Play Again");
		playAgainButton.setFont(new Font("Arial", Font.BOLD, 30));
		playAgainButton.setFocusable(false);
		playAgainButton.setVisible(false);
		playAgainButton.setBorderPainted(false);
		playAgainButton.setBackground(Color.BLACK);
		playAgainButton.setForeground(Color.white);
		playAgainButton.setBounds(650, 560, 200, 50); 
		playAgainButton.addActionListener(e -> resetGame());
		this.setLayout(null);
		this.add(playAgainButton);
		
		//snake
		snakeHead = new Tile(5,5);
		snakeBody= new ArrayList<Tile>();
		snakeEyes = new ImageIcon(getClass().getResource("/eyes.png")).getImage();
		snakeBackground = new ImageIcon(getClass().getResource("/pixel.png")).getImage();
		
		//food
		Food = new Tile(10,10);
		random = new Random();
		foodColor = generateColor();
		placeFood();
		
		//game timer
		gameLoop = new Timer(100, this);
		gameLoop.start();
		
		//snakes default velocity
		velocityX=0;
		velocityY=0;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}
	
	public void draw(Graphics g) {
		
//		//Grid
//		for(int i =0; i<boardWidth/tileSize; i++) {
//			g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
//			g.drawLine(0, i * tileSize, boardWidth, i*tileSize);
//		}
		//background and borders


		// Draw background tile
		
		g.setColor(Color.black);
		g.fillRect(0, 0, 600, 5);
		g.fillRect(0, 0, 5, 600);
		g.fillRect(595, 0, 595, 600);
		g.fillRect(0, 595, 600, 600);
		
		g.drawImage(snakeBackground, 0, 0, tileSize * gridCols, tileSize * gridRows, null);
		g.drawImage(snakeBackground, 1030, 0, tileSize * gridCols, tileSize * gridRows, null);
	
		//Food
		g.setColor(foodColor);
		
		g.fill3DRect(Food.x * tileSize, Food.y * tileSize, tileSize, tileSize, true);

		//Snake head
		g.setColor(Color.BLUE);
		g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);
		g.drawImage(snakeEyes, snakeHead.x * tileSize -9, snakeHead.y * tileSize - 13, 60, 60, null);

		//Snake body
		for(int i=0;i<snakeBody.size();i++) {
			Tile snakePart = snakeBody.get(i);
//			g.fillRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize);
			g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);

		}
		
		//Score
		g.setFont(new Font("Arial", Font.BOLD, 60));
		if (gameOver && showGameOver==true) {
			g.setColor(gameOverColor);
			g.drawString("Game Over: " + String.valueOf(snakeBody.size()), 553, 510);

		}
		if(gameOver==true) {
			
			if(snakeBody.size() > highScore) {
				highScore=snakeBody.size();
			}
			g.setColor(Color.red);
			g.setFont(new Font("Arial", Font.BOLD, 25));
			g.drawString("Highscore: " + String.valueOf(highScore), 675, 545);
		}

		else if (gameOver!=true){
			g.setColor(Color.black);
			g.setFont(new Font("Arial", Font.BOLD, 35));
			g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 25, tileSize - 5);
		}
	}
	public void placeFood() {
		int foodX;
		int foodY;
		boolean validPosition;
		
		do {
			validPosition=true;
			foodX=random.nextInt(boardWidth/tileSize);
			foodY=random.nextInt(boardHeight/tileSize);
		
		//checks body to see if food position is the same
		for(int i=0; i<snakeBody.size();i++) {
			if(foodX==snakeBody.get(i).x && foodY==snakeBody.get(i).y) {
				//makes valid position false
				validPosition=false;
				break;
			}
			
		}
		} while(!validPosition);
		
		//if position is valid for all snake parts, updates food x & y
		Food.x = foodX;
		Food.y = foodY;
	}
	//checks if tiles collide
	public boolean collision(Tile tile1, Tile tile2) {
		return tile1.x == tile2.x && tile1.y == tile2.y;
	}
	public void move() {		

		if(collision(snakeHead, Food)) {
			snakeBody.add(new Tile(Food.x, Food.y));
			placeFood();
			foodColor = generateColor();
			playSound();
		}
		//Updates snake body position
		for(int i=snakeBody.size()-1; i >=0; i--) {
			Tile snakePart = snakeBody.get(i);
			if(i==0) {
				snakePart.x = snakeHead.x;
				snakePart.y = snakeHead.y;
			}
			else {
				Tile prevSnakePart = snakeBody.get(i-1);
				snakePart.x = prevSnakePart.x;
				snakePart.y = prevSnakePart.y;
			}
		}
		snakeHead.x += velocityX;
		snakeHead.y += velocityY;

		//game over if head hits body
		for(int i=0; i<snakeBody.size();i++) {
			Tile snakePart=snakeBody.get(i);
			if(collision(snakeHead, snakePart)) {
				gameOver=true;
				playSound();
			}
		}
		//if out of bounds, game over
		if(snakeHead.x*tileSize<0 || snakeHead.x *tileSize > boardWidth
				|| snakeHead.y*tileSize < 0 || snakeHead.y *tileSize > boardHeight) {
			gameOver=true;
			playSound();
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		move();
		repaint();
		if(gameOver) {
			gameLoop.stop();
			playAgainButton.setVisible(true);
			//flashes game over text
			if(flashTimer==null) {
				flashTimer = new Timer(200, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showGameOver = !showGameOver;
						repaint();
					}
				});
				flashTimer.start();
			}
		}
	}
	public static Color generateColor(){
		//generates random color for fruit
		Random randcolor = new Random();
		int randIndex = randcolor.nextInt(Colors.length);
		return Colors[randIndex];
	}
	
	public void playSound() {

		try {
			if (gameOver!=true){
			//fruit eating sound
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/munch.wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.setFramePosition(clip.getFrameLength() / 2 - 2);
			FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			volume.setValue(-12.0f);
			clip.start();
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			
			if(gameOver==true) {
			//game over sound
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/gameover.wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.setFramePosition(clip.getFrameLength() /2 );
			clip.start();
			}
		}
			catch(Exception ex) {
				ex.printStackTrace();
			}
	}
	
	public void resetGame() {
		
		snakeHead = new Tile(5,5);
		snakeBody.clear();
		placeFood();
		
		velocityX=0;
		velocityY=0;
		
		gameOver=false;
		playAgainButton.setVisible(false);
		
		gameLoop.start();
		requestFocus();
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_UP && velocityY!=1 && velocityY!=-1) {
			velocityX=0;
			velocityY=-1;
			//up sound
			try {
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/snakeup.wav"));
				Clip clip = AudioSystem.getClip();
				clip.open(audioStream);
				//lower volume
				FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				volume.setValue(-8.0f);
				clip.start();
				}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN && velocityY!=-1 && velocityY!=1) {
			velocityX=0;
			velocityY=1;
			//down sound
			try {
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/snakedown.wav"));
				Clip clip = AudioSystem.getClip();
				clip.open(audioStream);
				//lower volume
				FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				volume.setValue(-8.0f);
				clip.start();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT && velocityX!=1 && velocityX!=-1) {
			velocityX=-1;
			velocityY=0;
			//left sound
			try {
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/snakeleft.wav"));
				Clip clip = AudioSystem.getClip();
				clip.open(audioStream);
				//lower volume
				FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				volume.setValue(-8.0f);
				clip.start();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX!= -1 && velocityX!=1) {
			velocityX=1;
			velocityY=0;
			//right sound
			try {
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/snakeright.wav"));
				Clip clip = AudioSystem.getClip();
				clip.open(audioStream);
				//lower volume
				FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				volume.setValue(-8.0f);
				clip.start();
			}

			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);  // immediately closes the game
		}
	}

	//do not need
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	
}
