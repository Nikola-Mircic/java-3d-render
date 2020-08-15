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
				cubeSide[i*200+j] = ((200)<<16)+((200)<<8)+(200);
			}
		}
		
		//Top
		for(int i=0;i<5;i++) {
			for(int j=0;j<200;j++) {
				cubeSide[j+i*200] = ((0)<<16)+((0)<<8)+(0);
			}
		}
		
		//Right
		for(int i=0;i<200;i++) {
			for(int j=195;j<200;j++) {
				cubeSide[j+i*200] = ((0)<<16)+((0)<<8)+(0);
			}
		}
		
		//Bottom
		for(int i=195;i<200;i++) {
			for(int j=0;j<200;j++) {
				cubeSide[j+i*200] = ((0)<<16)+((0)<<8)+(0);
			}
		}
		
		//Left
		for(int i=0;i<200;i++) {
			for(int j=0;j<5;j++) {
				cubeSide[j+i*200] = ((0)<<16)+((0)<<8)+(0);
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
		
		double[][] cube;
		for(int i=0;i<4;i++) {
			cube = getCube(-2, 0, 1+i*1.5);
			if(Xmove<cube[0][0])
				drawLeftSide(cube);
			if(Xmove>cube[6][0])
				drawRightSide(cube);
			if(Ymove>cube[5][1])
				drawTopSide(cube);
			if(Ymove<cube[0][1])
				drawBottomSide(cube);
			drawNearSide(cube);
		}
		
		for(int i=0;i<4;i++) {
			cube = getCube(1, 0, 1+i*1.5);
			makeCube(cube,img.getGraphics());
		}
		
		g.drawImage(img, 0, 0, null);
		
		endTime = System.currentTimeMillis();
		if(endTime-startTime>1000) {
			currFrames = frames;
			frames=0;
			startTime=endTime;
		}
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Times New Roman",Font.BOLD,25));
		g.drawString(""+currFrames+" fps", 10, 35);
		g.drawString("X: "+Xmove, 10, 55);
		g.drawString("Y: "+Ymove, 10, 75);
		g.drawString("Z: "+Zmove, 10, 95);
		
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
				Xpix = (toDraw[4][0]-Xmove+xx)/Zpix + WIDTH/2;
				Ypix = -(toDraw[4][1]-Ymove)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					int greyLevel = Math.min((int)(((cubeSide[xx+yy*200]>>16)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 16 & 255)<<16;
					greyLevel += Math.min((int)(((cubeSide[xx+yy*200]>>8)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 8 & 255)<<8;
					greyLevel += Math.min((int)((cubeSide[xx+yy*200]&255)*(Zpix/5)),cubeSide[xx+yy*200] & 255);
					
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200]-greyLevel;
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
				Xpix = (toDraw[3][0]-Xmove+xx)/Zpix + WIDTH/2;
				Ypix = -(toDraw[3][1]-Ymove)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					int greyLevel = Math.min((int)(((cubeSide[xx+yy*200]>>16)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 16 & 255)<<16;
					greyLevel += Math.min((int)(((cubeSide[xx+yy*200]>>8)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 8 & 255)<<8;
					greyLevel += Math.min((int)((cubeSide[xx+yy*200]&255)*(Zpix/5)),cubeSide[xx+yy*200] & 255);
					
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200]-greyLevel;
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
				Xpix = (toDraw[5][0]-Xmove-xx)/Zpix + WIDTH/2;
				Ypix = -(toDraw[5][1]-Ymove-yy)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					int greyLevel = Math.min((int)(((cubeSide[xx+yy*200]>>16)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 16 & 255)<<16;
					greyLevel += Math.min((int)(((cubeSide[xx+yy*200]>>8)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 8 & 255)<<8;
					greyLevel += Math.min((int)((cubeSide[xx+yy*200]&255)*(Zpix/5)),cubeSide[xx+yy*200] & 255);
					
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[199-xx+yy*200]-greyLevel;
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
				Xpix = (toDraw[7][0]-Xmove)/Zpix + WIDTH/2;
				Ypix = -(toDraw[7][1]-Ymove-yy)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					int greyLevel = Math.min((int)(((cubeSide[xx+yy*200]>>16)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 16 & 255)<<16;
					greyLevel += Math.min((int)(((cubeSide[xx+yy*200]>>8)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 8 & 255)<<8;
					greyLevel += Math.min((int)((cubeSide[xx+yy*200]&255)*(Zpix/5)),cubeSide[xx+yy*200] & 255);
					
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200]-greyLevel;
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
				Xpix = (toDraw[5][0]-Xmove)/Zpix + WIDTH/2;
				Ypix = -(toDraw[5][1]-Ymove-yy)/Zpix + HEIGHT/2;
				
				if(Xpix>=WIDTH || Xpix<=0 || Ypix>=HEIGHT || Ypix<=0)
					continue;
				
				if(ZBuffer[(int)Xpix+(int)Ypix*WIDTH]>=Zpix || ZBuffer[(int)Xpix+(int)Ypix*WIDTH]==-1) {
					int greyLevel = Math.min((int)(((cubeSide[xx+yy*200]>>16)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 16 & 255)<<16;
					greyLevel += Math.min((int)(((cubeSide[xx+yy*200]>>8)&255)*(Zpix/5)),cubeSide[xx+yy*200] >> 8 & 255)<<8;
					greyLevel += Math.min((int)((cubeSide[xx+yy*200]&255)*(Zpix/5)),cubeSide[xx+yy*200] & 255);
					
					imgPix[(int)Xpix+(int)Ypix*WIDTH] = cubeSide[xx+yy*200]-greyLevel;
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
			tempCube[i][0] = (toDraw[i][0]-Xmove)/tempCube[i][2] + WIDTH/2;
			tempCube[i][1] = -(toDraw[i][1]-Ymove)/tempCube[i][2] + HEIGHT/2;
		}
		
		drawCube(tempCube, g);
	}
	
	private void drawCube(double[][] toDraw,Graphics g) {
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
	
	private void drawLine(double[] point1,double[] point2,int color) {
		double minZpix = Math.min(point2[2], point1[2]);
		double zPixdiff = Math.abs(point2[2]-point1[2]);
		double zPixStep;
		//x1<=x2
		if(point1[0]<=point2[0]) {
			//x1==x2
			if(point2[0]==point1[0]) {
				int i = (int) Math.min(point2[1],point1[1]);
				int imax = (int) Math.max(point2[1],point1[1]);
				zPixStep = zPixdiff/(imax-i);
				for(;i<imax;i++) {
					if((int)point2[0]>=WIDTH || (int)point2[0]<=0 || i>=HEIGHT || i<=0)
						continue;
					if(ZBuffer[(int)point2[0]+i*WIDTH]>=minZpix+i*zPixStep || ZBuffer[(int)point2[0]+i*WIDTH]==-1) {
						imgPix[(int)point2[0]+i*WIDTH] = color;
						ZBuffer[(int)point2[0]+i*WIDTH]=minZpix+i*zPixStep;
					}
				}
			}else{
				double k = (point2[1]-point1[1])/(point2[0]-point1[0]);
				int n = (int)(point2[1]-k*point2[0]);
				zPixStep = zPixdiff/(point2[0]-point1[0]);
				for(int x=(int)point1[0];x<=(int)point2[0];x++) {
					int y = (int)(x*k+n);
					
					if(x>=WIDTH || x<=0 || y>=HEIGHT || y<=0)
						continue;
					
					if(ZBuffer[x+y*WIDTH]>=minZpix+x*zPixStep || ZBuffer[x+y*WIDTH]==-1) {
						imgPix[x+y*WIDTH] = color;
						ZBuffer[x+y*WIDTH]=minZpix+x*zPixStep;
					}
				}
			}
		}else {
			double k = (point1[1]-point2[1])/(point1[0]-point2[0]);
			int n = (int)(point1[1]-k*point1[0]);
			
			zPixStep = zPixdiff/(point1[0]-point2[0]);
			for(int x=(int)point2[0];x<=(int)point1[0];x++) {
				int y = (int)(x*k+n);
				
				if(x>=WIDTH || x<=0 || y>=HEIGHT || y<=0)
					continue;
				
				if(ZBuffer[x+y*WIDTH]>=minZpix+x*zPixStep || ZBuffer[x+y*WIDTH]==-1) {
					imgPix[x+y*WIDTH] = color;
					ZBuffer[x+y*WIDTH]=minZpix+x*zPixStep;
				}
			}
		}
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
			this.Xmove -= 10;
			break;
		case KeyEvent.VK_RIGHT:
			this.Xmove += 10;
			break;
		case KeyEvent.VK_UP:
			this.Ymove += 10;
			break;
		case KeyEvent.VK_DOWN:
			this.Ymove -= 10;
			break;
		case KeyEvent.VK_R:
			Xmove=0;
			Ymove=0;
			Zmove=0;
			break;
		default:
			break;
		}
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
