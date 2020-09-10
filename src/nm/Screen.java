package nm;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.*;
import java.util.function.ToDoubleBiFunction;

import javax.swing.JFrame;

public class Screen extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;

	public static int WIDTH = 800, HEIGHT = 600;
	public double Xmove = 0, Ymove = 0, Zmove = 0;

	public int rotationX;
	public int lastX;

	private BufferedImage img;
	public int[] imgPix;
	private double[] ZBuffer;

	private JFrame frame;
	private boolean running = false;

	private int frames = 0, currFrames = 0;
	private long startTime, endTime;
	
	private Render render;

	public Screen() {
		this.img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		this.imgPix = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		this.ZBuffer = new double[WIDTH * HEIGHT];

		this.rotationX = 0;
		this.lastX = -1;
		
		for (int i = 0; i < Screen.WIDTH; ++i) {
			for (int j = 0; j < Screen.HEIGHT; ++j) {
				imgPix[j * Screen.WIDTH + i] = (255<<16)+(255<<8)+(255);
				ZBuffer[j * Screen.WIDTH + i] = -1;
			}
		}
		
		this.render = new Render(this);
		
		frame = new JFrame();
		frame.add(this);
		frame.setTitle("Java 3D");
		frame.setSize(WIDTH, HEIGHT);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		InputHandler ih = new InputHandler(this);
		
		frame.addKeyListener(ih);
		addMouseListener(ih);
		addMouseWheelListener(ih);
		addMouseMotionListener(ih);

		startTime = System.currentTimeMillis();
		this.render();
	}

	public void render() {
		BufferStrategy bs;
		bs = this.getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();
		
		for (int i = 0; i < Screen.WIDTH; ++i) {
			for (int j = 0; j < Screen.HEIGHT; ++j) {
				imgPix[j * Screen.WIDTH + i] = (255<<16)+(255<<8)+(255);
			}
		}

		frames++;

		
		//for(int i=0;i<5;++i) {
			double[][] cube = getCube(-1.5, 0, 3);
			render.fillShape(cube, 8);
		//}
		
		g.drawImage(img, 0, 0, null);

		endTime = System.currentTimeMillis();
		if (endTime - startTime > 1000) {
			currFrames = frames;
			frames = 0;
			startTime = endTime;
		}

		g.setColor(Color.BLACK);
		g.setFont(new Font("Times New Roman", Font.BOLD, 25));
		g.drawString("" + currFrames + " fps", 10, 35);
		g.drawString("X: " + ((double)Math.round(Xmove*100))/100, 10, 55);
		g.drawString("Y: " + ((double)Math.round(Ymove*100))/100, 10, 75);
		g.drawString("Z: " + ((double)Math.round(Zmove*100))/100, 10, 95);

		g.dispose();
		bs.show();
	}

	private double[][] getCube(double x, double y, double z) {
		double cubeSize = 1;
		x *= cubeSize;
		y *= cubeSize;
		z *= cubeSize;
		double[][] cube = { { x, y, z }, // 1
				{ x + cubeSize, y, z }, // 2
				{ x + cubeSize, y, z + cubeSize }, // 3
				{ x, y, z + cubeSize }, // 4
				{ x, y + cubeSize, z }, // 5
				{ x + cubeSize, y + cubeSize, z }, // 6
				{ x + cubeSize, y + cubeSize, z + cubeSize }, // 7
				{ x, y + cubeSize, z + cubeSize },// 8
		};
		return cube;
	}

	public static void main(String[] arg0) {
		Screen screen = new Screen();

		Thread thread = new Thread(screen);
		thread.run();
	}

	@Override
	public void run() {
		running = true;
		while (running)
			this.render();
	}
}
