package nm;

import java.awt.Color;
import java.util.Arrays;

public class Render {
	
	private Screen screen;
	
	private int[] imgPix;
	private double[] ZBuffer;
	
	public Render(Screen screen) {
		this.screen = screen;
		this.imgPix = screen.imgPix;
		this.ZBuffer = new double[Screen.WIDTH*Screen.HEIGHT];
		
		for (int i = 0; i < Screen.WIDTH; ++i) {
			for (int j = 0; j < Screen.HEIGHT; ++j) {
				ZBuffer[j * Screen.WIDTH + i] = -1;
			}
		}

	}
	
	public void drawShape(double[][] shape, int vnum) {
		double[][] tempCube = new double[vnum][3];
		
		double renderDistance = 250.0;
		double screenz = 1.0;

		double angle = Math.PI / 2;
		double f = 1 / Math.tan(angle / 2);
		double zRatio = renderDistance / (renderDistance - screenz);

		double dist, a1, a2;
		
		for (int i = 0; i < vnum; i++) {
			dist = Math.sqrt((shape[i][0] - screen.Xmove) * (shape[i][0] - screen.Xmove) + 
							 (shape[i][2] - screen.Zmove) * (shape[i][2] - screen.Zmove));
			a1 = -Math.asin((shape[i][0] - screen.Xmove) / dist) * 180 / Math.PI;
			a2 = a1 - screen.rotationX;
			a2 = a2 / 180 * Math.PI;
			tempCube[i][0] = screen.Xmove - Math.sin(a2) * dist;
			tempCube[i][1] = shape[i][1];
			tempCube[i][2] = Math.cos(a2) * dist - screen.Zmove;
		}

		for (int i = 0; i < 8; ++i) {
			if (tempCube[i][2] < screenz + screen.Zmove)
				return;
			tempCube[i][2] = (tempCube[i][2] - screen.Zmove - screenz) / zRatio;
		}

		for (int i = 0; i < 8; ++i) {
			tempCube[i][0] = (f * (tempCube[i][0] - screen.Xmove) / tempCube[i][2]) * (Screen.WIDTH / 2) + Screen.WIDTH / 2;
			tempCube[i][1] = (-f * (tempCube[i][1] + screen.Ymove) / tempCube[i][2]) * (Screen.WIDTH / 2) + Screen.HEIGHT / 2;
		}
		
		double p1[] = {100, 150, 2};
		double p2[] = {200, 300, 2};
		
		drawCube(tempCube);
	}
	
	private void drawCube(double[][] toDraw) {
		Triangles.drawLine(toDraw[0], toDraw[1], Color.BLUE.getRGB(), screen.imgPix, ZBuffer);// 1 2  X
		Triangles.drawLine(toDraw[3], toDraw[0], Color.GREEN.getRGB(), screen.imgPix, ZBuffer);// 4 1 Z
		Triangles.drawLine(toDraw[4], toDraw[0], Color.RED.getRGB(), screen.imgPix, ZBuffer);// 5 1 Y
		Triangles.drawLine(toDraw[1], toDraw[2], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 2 3
		Triangles.drawLine(toDraw[2], toDraw[3], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 3 4
		Triangles.drawLine(toDraw[1], toDraw[5], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 2 6
		Triangles.drawLine(toDraw[2], toDraw[6], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 3 7
		Triangles.drawLine(toDraw[3], toDraw[7], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 4 8
		Triangles.drawLine(toDraw[5], toDraw[6], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 6 7
		Triangles.drawLine(toDraw[4], toDraw[7], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 5 8
		Triangles.drawLine(toDraw[7], toDraw[6], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 8 7
		Triangles.drawLine(toDraw[5], toDraw[4], Color.BLACK.getRGB(), screen.imgPix, ZBuffer);// 6 5
	}

	public void fillShape(double[][] shape, int vnum) {
		double[][] tempCube = new double[vnum][3];
		
		double renderDistance = 250.0;
		double screenz=1.0;
		
		double angle = Math.PI/2;
		double f = 1/Math.tan(angle/2);
		double zRatio = renderDistance/(renderDistance-screenz);
		
		double dist, a1, a2;
		for (int i = 0; i < vnum; i++) {
			dist = Math.sqrt((shape[i][0] - screen.Xmove) * (shape[i][0] - screen.Xmove) + (shape[i][2] - screen.Zmove) * (shape[i][2] - screen.Zmove));
			a1 = -Math.asin((shape[i][0] - screen.Xmove) / dist) * 180 / Math.PI;
			a2 = a1 - screen.rotationX;
			a2 = a2 / 180 * Math.PI;
			tempCube[i][0] = screen.Xmove - Math.sin(a2) * dist;
			tempCube[i][1] = shape[i][1];
			tempCube[i][2] = screen.Zmove + Math.cos(a2) * dist;
		}
		
		for (int i = 0; i < vnum; ++i) {
			if(tempCube[i][2]<screenz+screen.Zmove)
				return;
			tempCube[i][2] = (tempCube[i][2]-screen.Zmove-screenz)/zRatio;
		}
		
		for(int i=0; i<vnum; ++i) {
			tempCube[i][0] = (f*(tempCube[i][0]-screen.Xmove)/tempCube[i][2])*(Screen.WIDTH/2)+Screen.WIDTH/2;
			tempCube[i][1] = (-f*(tempCube[i][1]+screen.Ymove)/tempCube[i][2])*(Screen.WIDTH/2)+Screen.HEIGHT/2;
		}
		
		fillCube(shape,tempCube, getColor(100, 100, 100));
	}

	private void fillCube(double[][] origin,double[][] toFill, int color) {
		if(-screen.Ymove>=origin[5][1]) {
			// 5 6 7 8
			Triangles.fillTriangle(toFill[4], toFill[5], toFill[6], color, imgPix, ZBuffer);
			Triangles.fillTriangle(toFill[4], toFill[6], toFill[7], color, imgPix, ZBuffer);
		}
		if(-screen.Ymove<=origin[0][1]) {
			Triangles.fillTriangle(toFill[0], toFill[1], toFill[2], color, imgPix, ZBuffer);
			Triangles.fillTriangle(toFill[0], toFill[2], toFill[3], color, imgPix, ZBuffer);
		}
		if(screen.Xmove>=origin[1][0]){
			Triangles.fillTriangle(toFill[1], toFill[2], toFill[6], color, imgPix, ZBuffer);
			Triangles.fillTriangle(toFill[1], toFill[6], toFill[5], color, imgPix, ZBuffer);
		}
		if(screen.Xmove<=origin[0][0]){
			Triangles.fillTriangle(toFill[0], toFill[4], toFill[7], color, imgPix, ZBuffer);
			Triangles.fillTriangle(toFill[0], toFill[7], toFill[3], color, imgPix, ZBuffer);
		}
		if(screen.Zmove<=origin[0][2]) {
			Triangles.fillTriangle(toFill[0], toFill[1], toFill[5], color, imgPix, ZBuffer);
			Triangles.fillTriangle(toFill[0], toFill[5], toFill[4], color, imgPix, ZBuffer);
		}
	}
	
	private int getColor(int r, int g, int b) {
		return (r << 16) + (g << 8) + b;
	}
}
