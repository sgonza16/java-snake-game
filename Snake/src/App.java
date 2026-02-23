import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.*;

public class App {

	public static void main(String[] args) {
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int boardwidth = screenSize.width;
		int boardheight = screenSize.height;
		
		JFrame frame = new JFrame("Snake");
		frame.setUndecorated(true);  
		frame.setVisible(true);
		frame.setSize(boardwidth, boardheight);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SnakeGame snakeGame = new SnakeGame(boardwidth, boardheight);
		frame.add(snakeGame);
		frame.pack();
		snakeGame.requestFocus();
	}

}
