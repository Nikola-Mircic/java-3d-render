package nm;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

public class Screen extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;

	private int WIDTH, HEIGHT;
	public double Xmove = 0, Ymove = 0, Zmove = 0;

	public int rotationX;
	public int lastX;

	private BufferedImage img;
	private int[] imgPix;
	private double[] ZBuffer;

	private JFrame frame;
	private boolean running = false;

	private int frames = 0, currFrames = 0;
	private long startTime, endTime;

	public Screen() {
		this.WIDTH = 800;
		this.HEIGHT = 600;

		this.img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		this.imgPix = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		this.ZBuffer = new double[WIDTH * HEIGHT];

		this.rotationX = 0;
		this.lastX = -1;

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

		frames++;

		for (int i = 0; i < WIDTH; ++i) {
			for (int j = 0; j < HEIGHT; ++j) {
				imgPix[j * WIDTH + i] = getColor(255, 255, 255);
				ZBuffer[j * WIDTH + i] = -1;
			}
		}
		
		makeCube(getCube(-0.5, -0.5, 2));
		
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
		g.drawString("X: " + Xmove, 10, 55);
		g.drawString("Y: " + Ymove, 10, 75);
		g.drawString("Z: " + Zmove, 10, 95);

		g.dispose();
		bs.show();
	}

	private double[][] getCube(double x, double y, double z) {
		double cubeSize = 200;
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

	private void makeCubeFill(double[][] toDraw) {
		double[][] tempCube = new double[8][3];

		// calculate rotation
		double dist, a1, a2;
		for (int i = 0; i < 8; i++) {
			dist = Math.sqrt(
					(toDraw[i][0] - Xmove) * (toDraw[i][0] - Xmove) + (toDraw[i][2] - Zmove) * (toDraw[i][2] - Zmove));
			a1 = -Math.asin((toDraw[i][0] - Xmove) / dist) * 180 / Math.PI;
			a2 = a1 - rotationX;
			a2 = a2 / 180 * Math.PI;
			tempCube[i][0] = Xmove + Math.sin(a2) * dist + Math.cos(a2) * dist;
			tempCube[i][1] = toDraw[i][1];
			tempCube[i][2] = Zmove - Math.cos(a2) * dist;// + Math.sin(a2) * dist;
		}

		// translate Z
		double horizont = HEIGHT/2;
		double renderDistance = 2000.0;
		if (toDraw[3][2] - Zmove > renderDistance)
			return;
		for (int i = 0; i < 8; ++i) {
			tempCube[i][2] = ((horizont + tempCube[i][2] - Zmove) / (horizont));
			if (tempCube[i][2] <= 0) {
				return;
			}
		}

		// Translate X & Y
		for (int i = 0; i < 8; ++i) {
			if (tempCube[i][2] == 0)
				continue;
			tempCube[i][0] = (tempCube[i][0] - Xmove) / tempCube[i][2] + WIDTH / 2;
			tempCube[i][1] = -(tempCube[i][1] - Ymove) / tempCube[i][2] + HEIGHT / 2;
		}
		
	}

	/*private void fillCube(double[][] toDraw) {
		if (Xmove < toDraw[0][0])
			drawLeftSide(toDraw);
		if (Xmove > toDraw[6][0])
			drawRightSide(toDraw);
		if (Ymove > toDraw[5][1])
			drawTopSide(toDraw);
		if (Ymove < toDraw[0][1])
			drawBottomSide(toDraw);
		drawNearSide(toDraw);
	}*/

	private void drawTriangle(double[] p1, double[] p2, double[] p3, int color) {
		double minX = Math.min(p1[0], Math.min(p2[0], p3[0]));
		double minY = Math.min(p1[1], Math.min(p2[1], p3[1]));
		double maxX = Math.max(p1[0], Math.max(p2[0], p3[0]));
		double maxY = Math.max(p1[1], Math.max(p2[1], p3[1]));

		for (double x = minX; x <= maxX; x += 1) {
			for (double y = minY; y <= maxY; y += 1) {
				if (isInTraingle(p1[0], p1[1], p2[0], p2[1], p3[0], p3[1], x, y)) {
					if (x > 0 && x < WIDTH && y > 0 && y < HEIGHT)
						imgPix[(int) x + (int) y * WIDTH] = color;
				}
			}
		}
	}

	private boolean isInTraingle(double x1, double y1, double x2, double y2, double x3, double y3, double x, double y) {
		double A = area(x1, y1, x2, y2, x3, y3);

		double A1 = area(x, y, x2, y2, x3, y3);

		double A2 = area(x1, y1, x, y, x3, y3);

		double A3 = area(x1, y1, x2, y2, x, y);

		return ((A >= (A1 + A2 + A3 - 5)) && (A <= (A1 + A2 + A3 + 5) ));
	}

	private double area(double x1, double y1, double x2, double y2, double x3, double y3) {
		return Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
	}

	private void makeCube(double[][] toDraw) {
		double[][] tempCube = new double[8][3];
		
		double renderDistance = 250.0;
		double screenz=249.2;
		
		double angle = Math.PI/2;
		double f = 1/Math.tan(angle/2);
		double zRatio = renderDistance/(renderDistance-screenz);
		
		double dist, a1, a2;
		
		for (int i = 0; i < 8; i++) {
			dist = Math.sqrt((toDraw[i][0] - Xmove) * (toDraw[i][0] - Xmove) + (toDraw[i][2] - Zmove) * (toDraw[i][2] - Zmove));
			a1 = -Math.asin((toDraw[i][0] - Xmove) / dist) * 180 / Math.PI;
			a2 = a1 - rotationX;
			a2 = a2 / 180 * Math.PI;
			tempCube[i][0] = Xmove - Math.sin(a2) * dist;
			tempCube[i][1] = toDraw[i][1];
			tempCube[i][2] = Math.cos(a2) * dist-Zmove;
		}
		
		for (int i = 0; i < 8; ++i) {
			if(tempCube[i][2]<screenz+Zmove)
				return;
			tempCube[i][2] = (tempCube[i][2]-Zmove-screenz)/zRatio;
		}
		
		for(int i=0; i<8; ++i) {
			tempCube[i][0] = f*(tempCube[i][0]-Xmove)/tempCube[i][2]+WIDTH/2;
			tempCube[i][1] = -f*(tempCube[i][1]+Ymove)/tempCube[i][2]+HEIGHT/2;
		}

		drawCube(tempCube);
	}

	private void drawCube(double[][] toDraw) {
		drawTriangle(toDraw[6], toDraw[2], toDraw[7], Color.BLUE.getRGB());
		drawTriangle(toDraw[7], toDraw[2], toDraw[3], Color.BLUE.getRGB());
		drawTriangle(toDraw[0], toDraw[1], toDraw[5], Color.GRAY.getRGB());
		drawTriangle(toDraw[0], toDraw[5], toDraw[4], Color.GRAY.getRGB());
		drawLine(toDraw[0], toDraw[1], Color.BLUE.getRGB());// 1 2  X
		drawLine(toDraw[3], toDraw[0], Color.GREEN.getRGB());// 4 1 Z
		drawLine(toDraw[4], toDraw[0], Color.RED.getRGB());// 5 1 Y
		drawLine(toDraw[1], toDraw[2], Color.BLACK.getRGB());// 2 3
		drawLine(toDraw[2], toDraw[3], Color.BLACK.getRGB());// 3 4
		drawLine(toDraw[1], toDraw[5], Color.BLACK.getRGB());// 2 6
		drawLine(toDraw[2], toDraw[6], Color.BLACK.getRGB());// 3 7
		drawLine(toDraw[3], toDraw[7], Color.BLACK.getRGB());// 4 8
		drawLine(toDraw[5], toDraw[6], Color.BLACK.getRGB());// 6 7
		drawLine(toDraw[4], toDraw[7], Color.BLACK.getRGB());// 5 8
		drawLine(toDraw[7], toDraw[6], Color.BLACK.getRGB());// 8 7
		drawLine(toDraw[5], toDraw[4], Color.BLACK.getRGB());// 6 5
	}

	private void drawLine(double[] point1, double[] point2, int color) {
		double minZpix = Math.min(point2[2], point1[2]);
		double zPixdiff = Math.abs(point2[2] - point1[2]);
		double zPixStep;
		// x1<=x2
		if (point1[0] <= point2[0]) {
			// x1==x2
			if (point2[0] == point1[0]) {
				int i = (int) Math.min(point2[1], point1[1]);
				int imax = (int) Math.max(point2[1], point1[1]);
				zPixStep = zPixdiff / (imax - i);
				for (; i < imax; i++) {
					if ((int) point2[0] >= WIDTH || (int) point2[0] <= 0 || i >= HEIGHT || i <= 0)
						continue;
					if (ZBuffer[(int) point2[0] + i * WIDTH] >= minZpix + i * zPixStep
							|| ZBuffer[(int) point2[0] + i * WIDTH] == -1) {
						imgPix[(int) point2[0] + i * WIDTH] = color;
						ZBuffer[(int) point2[0] + i * WIDTH] = minZpix + i * zPixStep;
					}
				}
			} else {
				double k = (point2[1] - point1[1]) / (point2[0] - point1[0]);
				int n = (int) (point2[1] - k * point2[0]);
				zPixStep = zPixdiff / (point2[0] - point1[0]);
				for (int x = (int) point1[0]; x <= (int) point2[0]; x++) {
					int y = (int) (x * k + n);

					if (x >= WIDTH || x <= 0 || y >= HEIGHT || y <= 0)
						continue;

					if (ZBuffer[x + y * WIDTH] >= minZpix + x * zPixStep || ZBuffer[x + y * WIDTH] == -1) {
						imgPix[x + y * WIDTH] = color;
						ZBuffer[x + y * WIDTH] = minZpix + x * zPixStep;
					}
				}
			}
		} else {
			double k = (point1[1] - point2[1]) / (point1[0] - point2[0]);
			int n = (int) (point1[1] - k * point1[0]);

			zPixStep = zPixdiff / (point1[0] - point2[0]);
			for (int x = (int) point2[0]; x <= (int) point1[0]; x++) {
				int y = (int) (x * k + n);

				if (x >= WIDTH || x <= 0 || y >= HEIGHT || y <= 0)
					continue;

				if (ZBuffer[x + y * WIDTH] >= minZpix + x * zPixStep || ZBuffer[x + y * WIDTH] == -1) {
					imgPix[x + y * WIDTH] = color;
					ZBuffer[x + y * WIDTH] = minZpix + x * zPixStep;
				}
			}
		}
	}

	private int getColor(int r, int g, int b) {
		return (r << 16) + (g << 8) + b;
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
