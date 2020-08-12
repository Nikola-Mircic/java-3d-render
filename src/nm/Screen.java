package nm;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

public class Screen extends Canvas implements Runnable,KeyListener,MouseListener,MouseMotionListener,MouseWheelListener{
	private static final long serialVersionUID = 1L;

	private int WIDTH,HEIGHT;
	private double Xmove=0,Ymove=0,Zmove=0;
	
	private BufferedImage img;
	private int[] cubeSide;
	private int[] imgPix;
	private double[] ZBuffer;
	
	private JFrame frame;
	private boolean running = false;
	
	private int frames = 0,currFrames = 0;
	private long startTime,endTime;
	
	public Screen() {
		this.WIDTH = 800;
		this.HEIGHT = 600;
		
		this.img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		this.imgPix = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		this.ZBuffer = new double[WIDTH*HEIGHT];
		this.cubeSide = new int[200*200];
		
		//Midle of side
		for(int i=0;i<200;i++) {
			for(int j=0;j<200;j++) {
				cubeSide[i*200+j] = ((200-j)<<16)+((200-j)<<8)+(200-j);
			}
		}
		
		//Top
		for(int i=0;i<5;i++) {
			for(int j=0;j<200;j++) {
				cubeSide[j+i*200] = ((255)<<16)+((255)<<8)+(255);
			}
		}
		
		//Right
		for(int i=0;i<200;i++) {
			for(int j=195;j<200;j++) {
				cubeSide[j+i*200] = ((255)<<16)+((255)<<8)+(255);
			}
		}
		
		//Bottom
		for(int i=195;i<200;i++) {
			for(int j=0;j<200;j++) {
				cubeSide[j+i*200] = ((255)<<16)+((255)<<8)+(255);
			}
		}
		
		//Left
		for(int i=0;i<200;i++) {
			for(int j=0;j<5;j++) {
				cubeSide[j+i*200] = ((255)<<16)+((255)<<8)+(255);
			}
		}
		
		frame = new JFrame();
		frame.add(this);
		frame.setTitle("Java 3D");
		frame.setSize(WIDTH,HEIGHT);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		frame.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		
		startTime = System.currentTimeMillis();
		this.render();
	}
	
	public void render() {
		BufferStrategy bs;
		do {
			bs = this.getBufferStrategy();
			if(bs==null) {
				createBufferStrategy(3);
			}
		}while(bs==null);
		
		Graphics g = bs.getDrawGraphics();
		
		frames++;
		
		for(int i=0;i<WIDTH;++i) {
			for(int j=0;j<HEIGHT;++j) {
				imgPix[j*WIDTH+i] = getColor(255, 255, 255);
				ZBuffer[j*WIDTH+i] = -1;
			}
		}
		
		double[][] cube = getCube(-2, 0, 1);
		drawTopSide(cube);
		drawBottomSide(cube);
		drawLeftSide(cube);
		drawRightSide(cube);
		drawNearSide(cube);
		
		g.drawImage(img, 0, 0, null);
		
		cube = getCube(1, 0, 1);
		makeCube(cube,g);
		
		endTime = System.currentTimeMillis();
		if(endTime-startTime>1000) {
			currFrames = frames;
			frames=0;
			startTime=endTime;
		}
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Times New Roman",Font.BOLD,25));
		g.drawString(""+currFrames+" fps", 10, 35);
		
		g.dispose();
		bs.show();
	}
	
	private double[][] getCube(double x,double y,double z){
		double cubeSize = 200;
		x*=cubeSize;
		y*=cubeSize;
		z*=cubeSize;
		double[][] cube = {
				{x,y,z},//1
				{x+cubeSize, y, z},//2
				{x+cubeSize, y, z+cubeSize},//3
				{x, y, z+cubeSize},//4
				{x, y+cubeSize, z},//5
				{x+cubeSize, y+cubeSize, z},//6
				{x+cubeSize, y+cubeSize, z+cubeSize},//7
				{x,  y+cubeSize, z+cubeSize},//8
		};
		return cube;
	}
	
	private void drawTopSide(double[][] toDraw) {
		double Xpix=0.0,Ypix=0.0,Zpix=0.0;
		
		for(int yy=0;yy<200;yy++) {
			for(int xx=0;xx<200;xx++) {
				//translate Z
				double renderDis = HEIGHT/2;
				Zpix = (renderDis+toDraw[4][2]+yy-Zmove)/(renderDis);
				if(Zpix<=0) {
					return;
				}
				
				//Translate X & Y
				Xpix = (toDraw[4][0]+Xmove+xx)/Zpix + WIDTH/2;
				Ypix = -(toDraw[4][1]+Ymove)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200];
					ZBuffer[(int)Xpix+(int)Ypix*WIDTH]=Zpix;
				}
			}
		}
	}
	
	private void drawBottomSide(double[][] toDraw) {
		double Xpix=0.0,Ypix=0.0,Zpix=0.0;
		
		for(int yy=0;yy<200;yy++) {
			for(int xx=0;xx<200;xx++) {
				//translate Z
				double renderDis = HEIGHT/2;
				Zpix = (renderDis+toDraw[3][2]-yy-Zmove)/(renderDis);
				if(Zpix<=0) {
					return;
				}
				
				//Translate X & Y
				Xpix = (toDraw[3][0]+Xmove+xx)/Zpix + WIDTH/2;
				Ypix = -(toDraw[3][1]+Ymove)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200];
					ZBuffer[(int)Xpix+(int)Ypix*WIDTH]=Zpix;
				}
			}
		}
	}
	
	private void drawNearSide(double[][] toDraw) {
		double Xpix=0.0,Ypix=0.0,Zpix=0.0;
		
		for(int yy=0;yy<200;yy++) {
			for(int xx=0;xx<200;xx++) {
				//translate Z
				double renderDis = HEIGHT/2;
				Zpix = ((renderDis+toDraw[5][2]-Zmove)/(renderDis));
				if(Zpix<=0) {
					return;
				}
				
				//Translate X & Y
				Xpix = (toDraw[5][0]+Xmove-xx)/Zpix + WIDTH/2;
				Ypix = -(toDraw[5][1]+Ymove-yy)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[199-xx+yy*200];
					ZBuffer[(int)Xpix+(int)Ypix*WIDTH]=Zpix;
				}
			}
		}
	}
	
	private void drawLeftSide(double[][] toDraw) {
		double Xpix=0.0,Ypix=0.0,Zpix=0.0;
		
		for(int yy=0;yy<200;yy++) {
			for(int xx=0;xx<200;xx++) {
				//translate Z
				double renderDis = HEIGHT/2;
				Zpix = ((renderDis+toDraw[7][2]-Zmove-xx)/(renderDis));
				if(Zpix<=0) {
					return;
				}
				
				
				//Translate X & Y
				Xpix = (toDraw[7][0]+Xmove)/Zpix + WIDTH/2;
				Ypix = -(toDraw[7][1]+Ymove-yy)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200];
					ZBuffer[(int)Xpix+(int)Ypix*WIDTH]=Zpix;
				}
			}
		}
	}
	
	private void drawRightSide(double[][] toDraw) {
		double Xpix=0.0,Ypix=0.0,Zpix=0.0;
		
		for(int yy=0;yy<200;yy++) {
			for(int xx=0;xx<200;xx++) {
				//translate Z
				double renderDis = HEIGHT/2;
				Zpix = ((renderDis+toDraw[5][2]-Zmove+xx)/(renderDis));
				if(Zpix<=0) {
					return;
				}
				
				
				//Translate X & Y
				Xpix = (toDraw[5][0]+Xmove)/Zpix + WIDTH/2;
				Ypix = -(toDraw[5][1]+Ymove-yy)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200];
					ZBuffer[(int)Xpix+(int)Ypix*WIDTH]=Zpix;
				}
			}
		}
	}
	
	private void makeCube(double[][] toDraw,Graphics g) {
		double[][] tempCube = new double[8][3];
		
		//translate Z
		double renderDis = HEIGHT/2;
		for(int i=0;i<8;++i) {
			tempCube[i][2] = ((renderDis+toDraw[i][2]-Zmove)/(renderDis));
			if(tempCube[i][2]<=0) {
				return;
			}
		}
		
		//Translate X & Y
		for(int i=0;i<8;++i) {
			if(tempCube[i][2]==0)
				continue;
			tempCube[i][0] = (toDraw[i][0]+Xmove)/tempCube[i][2] + WIDTH/2;
			tempCube[i][1] = -(toDraw[i][1]+Ymove)/tempCube[i][2] + HEIGHT/2;
		}
		
		drawCube(tempCube, g);
	}
	
	private void drawCube(double[][] toDraw,Graphics g) {
		g.setColor(Color.BLUE);
		g.drawLine((int)toDraw[0][0], (int)toDraw[0][1], (int)toDraw[1][0], (int)toDraw[1][1]);// 1 2
		g.setColor(Color.GREEN);
		g.drawLine((int)toDraw[3][0], (int)toDraw[3][1], (int)toDraw[0][0], (int)toDraw[0][1]);// 4 1
		g.setColor(Color.RED);
		g.drawLine((int)toDraw[4][0], (int)toDraw[4][1], (int)toDraw[0][0], (int)toDraw[0][1]);// 5 1
		g.setColor(Color.BLACK);
		g.drawLine((int)toDraw[1][0], (int)toDraw[1][1], (int)toDraw[2][0], (int)toDraw[2][1]);// 2 3
		g.drawLine((int)toDraw[2][0], (int)toDraw[2][1], (int)toDraw[3][0], (int)toDraw[3][1]);// 3 4
		g.drawLine((int)toDraw[1][0], (int)toDraw[1][1], (int)toDraw[5][0], (int)toDraw[5][1]);// 2 6
		g.drawLine((int)toDraw[2][0], (int)toDraw[2][1], (int)toDraw[6][0], (int)toDraw[6][1]);// 3 7
		g.drawLine((int)toDraw[3][0], (int)toDraw[3][1], (int)toDraw[7][0], (int)toDraw[7][1]);// 4 8
		g.drawLine((int)toDraw[5][0], (int)toDraw[5][1], (int)toDraw[6][0], (int)toDraw[6][1]);// 6 7
		g.drawLine((int)toDraw[4][0], (int)toDraw[4][1], (int)toDraw[7][0], (int)toDraw[7][1]);// 5 8
		g.drawLine((int)toDraw[7][0], (int)toDraw[7][1], (int)toDraw[6][0], (int)toDraw[6][1]);// 8 7
		g.drawLine((int)toDraw[5][0], (int)toDraw[5][1], (int)toDraw[4][0], (int)toDraw[4][1]);// 6 5
	}
	
	private int getColor(int r,int g,int b) {
		return (r<<16)+(g<<8)+b;
	}
	
	public static void main(String[] arg0) {
		Screen screen = new Screen();
		
		Thread thread = new Thread(screen);
		thread.run();
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			this.render();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		this.Zmove += e.getWheelRotation()*100;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			this.Xmove += 10;
			break;
		case KeyEvent.VK_RIGHT:
			this.Xmove -= 10;
			break;
		case KeyEvent.VK_UP:
			this.Ymove -= 10;
			break;
		case KeyEvent.VK_DOWN:
			this.Ymove += 10;
			break;
		case KeyEvent.VK_R:
			Xmove=0;
			Ymove=0;
			Zmove=0;
			break;
		default:
			break;
		}
		if(e.getKeyCode() == KeyEvent.VK_LEFT)
			this.Xmove += 10;
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			this.Xmove -= 10;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			this.Ymove -= 10;
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			this.Ymove += 10;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
