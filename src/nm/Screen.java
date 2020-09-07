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

	private int WIDTH, HEIGHT;
	public double Xmove = 0, Ymove = 0, Zmove = 0;

	public int rotationX;
	public int lastX;
	
	private double tick=0;
	private double dTick=0;

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
		for(int i=0;i<5;++i) {
			makeCubeFill(getCube(-1.5, 0, 2+i*1.5));
		}
		
		for(int i=0;i<5;++i) {
			makeCube(getCube(0.5, 0, 2+i*1.5));
		}
		
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

	private void makeCubeFill(double[][] toDraw) {
		double[][] tempCube = new double[8][3];
		
		double renderDistance = 250.0;
		double screenz=1.0;
		
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
			tempCube[i][2] = Zmove + Math.cos(a2) * dist;
		}
		
		for (int i = 0; i < 8; ++i) {
			if(tempCube[i][2]<screenz+Zmove)
				return;
			tempCube[i][2] = (tempCube[i][2]-Zmove-screenz)/zRatio;
		}
		
		for(int i=0; i<8; ++i) {
			tempCube[i][0] = (f*(tempCube[i][0]-Xmove)/tempCube[i][2])*(WIDTH/2)+WIDTH/2;
			tempCube[i][1] = (-f*(tempCube[i][1]+Ymove)/tempCube[i][2])*(WIDTH/2)+HEIGHT/2;
		}
		
		fillCube(toDraw,tempCube, getColor(100, 100, 100));
	}

	private void fillCube(double[][] origin,double[][] toFill, int color) {
		if(-Ymove>=origin[5][1]) {
			// 5 6 7 8
			fillTriangle(toFill[4], toFill[5], toFill[6], color);
			fillTriangle(toFill[4], toFill[6], toFill[7], color);
		}
		if(-Ymove<=origin[0][1]) {
			fillTriangle(toFill[0], toFill[1], toFill[2], color);
			fillTriangle(toFill[0], toFill[2], toFill[3], color);
		}
		if(Xmove>=origin[1][0]){
			fillTriangle(toFill[1], toFill[2], toFill[6], color);
			fillTriangle(toFill[1], toFill[6], toFill[5], color);
		}
		if(Xmove<=origin[0][0]){
			fillTriangle(toFill[0], toFill[4], toFill[7], color);
			fillTriangle(toFill[0], toFill[7], toFill[3], color);
		}
		if(Zmove<=origin[0][2]) {
			fillTriangle(toFill[0], toFill[1], toFill[5], color);
			fillTriangle(toFill[0], toFill[5], toFill[4], color);
		}
	}
	
	private void fillTriangle(double[] p1, double[] p2, double[] p3, int color) {
		List<double[]> triangle = new ArrayList<>();
		triangle.add(p1);
		triangle.add(p2);
		triangle.add(p3);
		
		Collections.sort(triangle, new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				return (int)(o1[1]-o2[1]);
			}
		});
		
		p1 = triangle.get(0);
		p2 = triangle.get(1);
		p3 = triangle.get(2);
		
		if((int)p2[1]==(int)p3[1]) {
			fillBottomFlatTriangle(p1, p2, p3, color);
		}else if((int)p1[1]==(int)p2[1]) {
			fillTopFlatTriangle(p1, p2, p3, color);
		}else {
			double[] tempPoint = new double[3];
			
			double scale = (p2[1]-p1[1])/(p3[1]-p1[1]);
			
			tempPoint[0] = p1[0]+(p3[0]-p1[0])*scale;
			tempPoint[1] = p2[1];
			
			fillBottomFlatTriangle(p1, tempPoint, p2, color);
			fillTopFlatTriangle(tempPoint, p2, p3, color);
		}
	}
	
	
	private void fillBottomFlatTriangle(double[] v1, double[] v2, double[] v3,int color){
		double invslope1 = (v2[0] - v1[0]) / Math.abs((v2[1]-v1[1] == 0) ? 1 : (v2[1]-v1[1] ));
		double invslope2 = (v3[0] - v1[0]) / Math.abs((v3[1]-v1[1] == 0) ? 1 : (v3[1]-v1[1] ));
	
		double curx1 = v1[0];
		double curx2 = v1[0];
		
		double minZ = v1[2];
		double zStep = (v2[2]-v1[2])/(v2[1]-v1[1]); 
	
		for (double scanlineY = v1[1]; scanlineY <= v2[1]; scanlineY++) {
			if(curx1<curx2) {
				for(int i=(int)curx1;i<curx2;i++) {
					if (i >= WIDTH || i <= 0 || scanlineY >= HEIGHT || scanlineY <= 0)
						continue;
					int greyLvl = getColor((int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)));
					if(ZBuffer[i+(int)scanlineY*WIDTH]==-1 || ZBuffer[i+(int)scanlineY*WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*WIDTH]=(color+greyLvl<0)?0:color+greyLvl;
						ZBuffer[i+(int)scanlineY*WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 += invslope1;
				curx2 += invslope2;
			}else {
				for(int i=(int)curx1;i>=curx2;i--) {
					if (i >= WIDTH || i <= 0 || scanlineY >= HEIGHT || scanlineY <= 0)
						continue;
					int greyLvl = getColor((int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)));
					if(ZBuffer[i+(int)scanlineY*WIDTH]==-1 || ZBuffer[i+(int)scanlineY*WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*WIDTH]=(color+greyLvl<0)?0:color+greyLvl;
						ZBuffer[i+(int)scanlineY*WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 += invslope1;
				curx2 += invslope2;
			}
			
		}
	}


	private void fillTopFlatTriangle(double[] v1, double[] v2, double[] v3, int color) {
		double invslope1 = (v3[0] - v1[0]) / Math.abs((v3[1]-v1[1] == 0) ? 1 : (v3[1]-v1[1] ));
		double invslope2 = (v3[0] - v2[0]) / Math.abs((v3[1]-v2[1] == 0) ? 1 : (v3[1]-v2[1] ));
	
		double curx1 = v3[0];
		double curx2 = v3[0];
		
		
		double minZ = v1[2];
		double zStep = (v3[2]-v1[2])/(v3[1]-v1[1]); 
		
		for (double scanlineY = v3[1]; scanlineY > v1[1]; scanlineY--) {
			if(curx1<curx2) {
				for(int i=(int)curx1;i<curx2;i++) {
					if (i >= WIDTH || i <= 0 || scanlineY >= HEIGHT || scanlineY <= 0)
						continue;
					int greyLvl = getColor((int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)));
					if(ZBuffer[i+(int)scanlineY*WIDTH]==-1 || ZBuffer[i+(int)scanlineY*WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*WIDTH]=(color+greyLvl<0)?0:color+greyLvl;
						ZBuffer[i+(int)scanlineY*WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 -= invslope1;
				curx2 -= invslope2;
			}else {
				for(int i=(int)curx1;i>=curx2;i--) {
					if (i >= WIDTH || i <= 0 || scanlineY >= HEIGHT || scanlineY <= 0)
						continue;
					int greyLvl = getColor((int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)), (int)(2*(minZ+zStep*scanlineY)));
					if(ZBuffer[i+(int)scanlineY*WIDTH]==-1 || ZBuffer[i+(int)scanlineY*WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*WIDTH]=(color+greyLvl<0)?0:color+greyLvl;
						ZBuffer[i+(int)scanlineY*WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 -= invslope1;
				curx2 -= invslope2;
			}
		}
	}

	private void makeCube(double[][] toDraw) {
		double[][] tempCube = new double[8][3];
		
		double renderDistance = 250.0;
		double screenz=1.0;
		
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
			tempCube[i][2] = Zmove + Math.cos(a2) * dist;
		}
		
		for (int i = 0; i < 8; ++i) {
			if(tempCube[i][2]<screenz+Zmove)
				return;
			tempCube[i][2] = (tempCube[i][2]-Zmove-screenz)/zRatio;
		}
		
		for(int i=0; i<8; ++i) {
			tempCube[i][0] = (f*(tempCube[i][0]-Xmove)/tempCube[i][2])*(WIDTH/2)+WIDTH/2;
			tempCube[i][1] = (-f*(tempCube[i][1]+Ymove)/tempCube[i][2])*(WIDTH/2)+HEIGHT/2;
		}
		
		drawCube(tempCube);
	}

	private void drawCube(double[][] toDraw) {
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
