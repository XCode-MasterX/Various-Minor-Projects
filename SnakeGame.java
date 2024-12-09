import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class SnakeGame extends JFrame{
	Point headPos;
	Point foodPos;
	ArrayList<Point> body;
	int vx = 0, vy = 0;
	boolean gameOver = false;
    
    final JLabel gameOverScreen;
	final int WIDTH, HEIGHT;
	final int BLOCK_SIZE = 30;

	public SnakeGame() {
		WIDTH = 900;
		HEIGHT = 600;
        gameOverScreen = new JLabel("GAME OVER");
        gameOverScreen.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        gameOverScreen.setForeground(new Color(1f, 1,1 ));

		this.setSize(WIDTH, HEIGHT);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gameOverScreen.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                gameReset();
            }
        });

        gameReset();
	}

	public static void main(String args[]) { 
		SnakeGame inst = new SnakeGame();
        inst.gameReset();
		while(true && !inst.gameOver) inst.update();
	}

    public void gameReset() {
        gameOver = false;
        body = new ArrayList<>();
        this.remove(gameOverScreen);

        this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_A && (body.size() == 1 || vx != 1)) {	vx = -1; vy = 0; }
				else if(e.getKeyCode() == KeyEvent.VK_D && (body.size() == 1 || vx != -1)) { vx = 1; vy = 0; }
				else if(e.getKeyCode() == KeyEvent.VK_W && (body.size() == 1 || vy != 1)) { vx = 0; vy = -1;}
				else if(e.getKeyCode() == KeyEvent.VK_S && (body.size() == 1 || vy != -1)) { vx = 0; vy = 1; }
			}
		});

		vx = 0;
		vy = 1;

		headPos = new Point(BLOCK_SIZE, BLOCK_SIZE);
        body.add(0, headPos);
		nextFoodPos();
    }

	public void menuDisplay() {
		menu.setEnabled(!isPlaying);
		repaint();
	}

	public void update() {
        if(gameOver) return;
		// UPDATE
        
        for(int i = body.size() - 1; i > 0; i--) {
            body.get(i).copy(body.get(i - 1));
            body.get(i).copy(body.get(i - 1));
        }
        
        body.get(0).y += vy * BLOCK_SIZE;
        body.get(0).x += vx * BLOCK_SIZE;

		// CHECK AGAINST BOUNDARIES
		if(body.get(0).x < 0) body.get(0).x = WIDTH - BLOCK_SIZE;
		else if(body.get(0).x > WIDTH - BLOCK_SIZE) body.get(0).x = BLOCK_SIZE;

		if(body.get(0).y < 0) body.get(0).y = HEIGHT - BLOCK_SIZE;
		else if(body.get(0).y > HEIGHT - BLOCK_SIZE) body.get(0).y = BLOCK_SIZE; 
		
		checkCollision();

		// DISPLAY POST UPDATE
		repaint();
		try {
			Thread.sleep(50);
		}
		catch(InterruptedException e) { e.printStackTrace(); System.exit(0); }
	}

	public void checkCollision() {
		if(checkPointCollision(foodPos, headPos)) {	
			nextFoodPos();
			Point p = body.get(body.size() - 1);
			body.add(1, new Point(p.x - vx * BLOCK_SIZE, p.y - vy * BLOCK_SIZE));
            repaint();
        }
        for(int i = 1; i < body.size(); i++) {
            if(checkPointCollision(body.get(i), headPos)) {
                System.out.println("GAME OVER");
                gameOver = true;
                repaint();
                this.add(gameOverScreen);
            }
        }
	}

	public boolean checkPointCollision(Point a, Point b) {
		int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(b.y - a.y);
        dx *= dx;
        dy *= dy;
        return (dx + dy) < (BLOCK_SIZE * BLOCK_SIZE);
	}

	public void nextFoodPos() {
		ArrayList<Point> possibilities = new ArrayList<>();

		for(int x = 1; x < WIDTH / BLOCK_SIZE  - 1; x++)
		for(int y = 1; y < HEIGHT / BLOCK_SIZE - 1; y++)
			possibilities.add(new Point(x * BLOCK_SIZE, y * BLOCK_SIZE));
		
		for(Point b : body) possibilities.remove(b);
		
		foodPos = possibilities.get((int)(Math.random() * possibilities.size()));
	}

	public void paint(Graphics g){
        if(gameOver){
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            return;
        }
        
    	// DRAW BACKGROUND
		g.setColor(Color.GREEN);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		// DRAW SNAKE
        g.setColor(Color.BLUE);
        g.fillOval(headPos.x, headPos.y, BLOCK_SIZE, BLOCK_SIZE);
		
        g.setColor(Color.WHITE);
		for(int i = 1; i < body.size(); i++) {
            Point bodyPoint = body.get(i);
			g.fillOval(bodyPoint.x, bodyPoint.y, BLOCK_SIZE, BLOCK_SIZE);
        }

		// DRAW FOOD
		g.setColor(Color.RED);
		g.fillOval(foodPos.x, foodPos.y, BLOCK_SIZE, BLOCK_SIZE);
		return; 
	}
}

class Point {
	int x;
	int y;

	public Point(int x, int y) { this.x = x; this.y = y; }

	public boolean equal(Object ob) {
		if(ob instanceof Point p)
			return x == p.x && y == p.y;
		else
			return false;
	}

	public String toString() { return String.format("{x: %d, y: %d}", x, y); }

	public boolean equals(Object o) { 
		if(o instanceof Point comp)
			return this.x == comp.x && this.y == comp.x;
		else {
			new Exception("Shouldn't have happened").printStackTrace();
			System.exit(0);
			return false;	
		}
	}

    public void copy(Point a) {
        this.x = a.x;
        this.y = a.y;
    }
}
