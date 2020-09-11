package nm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Triangles {
	public static void fillTriangle(double[] p1, double[] p2, double[] p3, int color,int[] imgPix,double[] ZBuffer) {
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
			fillBottomFlatTriangle(p1, p2, p3, color,imgPix,ZBuffer);
		}else if((int)p1[1]==(int)p2[1]) {
			fillTopFlatTriangle(p1, p2, p3, color,imgPix,ZBuffer);
		}else {
			double[] tempPoint = new double[3];
			
			double scale = (p2[1]-p1[1])/(p3[1]-p1[1]);
			
			tempPoint[0] = p1[0]+(p3[0]-p1[0])*scale;
			tempPoint[1] = p2[1];
			
			fillBottomFlatTriangle(p1, tempPoint, p2, color,imgPix,ZBuffer);
			fillTopFlatTriangle(tempPoint, p2, p3, color,imgPix,ZBuffer);
		}
	}
	
	private static void fillBottomFlatTriangle(double[] v1, double[] v2, double[] v3,int color,int[] imgPix,double[] ZBuffer){
		double invslope1 = (v2[0] - v1[0]) / Math.abs((v2[1]-v1[1] == 0) ? 1 : (v2[1]-v1[1] ));
		double invslope2 = (v3[0] - v1[0]) / Math.abs((v3[1]-v1[1] == 0) ? 1 : (v3[1]-v1[1] ));
	
		double curx1 = v1[0];
		double curx2 = v1[0];
		
		double minZ = 0;
		double zStep = (v2[2]-v1[2])/(v2[1]-v1[1]); 
	
		for (double scanlineY = v1[1]; scanlineY <= v2[1]; scanlineY++) {
			if(curx1<curx2) {
				for(int i=(int)curx1;i<curx2;i++) {
					if (i >= Screen.WIDTH || i <= 0 || scanlineY >= Screen.HEIGHT || scanlineY <= 0)
						continue;
					
					if(ZBuffer[i+(int)scanlineY*Screen.WIDTH]==-1 || ZBuffer[i+(int)scanlineY*Screen.WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*Screen.WIDTH]=color;
						//ZBuffer[i+(int)scanlineY*Screen.WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 += invslope1;
				curx2 += invslope2;
			}else {
				for(int i=(int)curx1;i>=curx2;i--) {
					if (i >= Screen.WIDTH || i <= 0 || scanlineY >= Screen.HEIGHT || scanlineY <= 0)
						continue;
					
					if(ZBuffer[i+(int)scanlineY*Screen.WIDTH]==-1 || ZBuffer[i+(int)scanlineY*Screen.WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*Screen.WIDTH]=color;
						//ZBuffer[i+(int)scanlineY*Screen.WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 += invslope1;
				curx2 += invslope2;
			}
			
		}
	}


	private static void fillTopFlatTriangle(double[] v1, double[] v2, double[] v3, int color,int[] imgPix,double[] ZBuffer) {
		double invslope1 = (v3[0] - v1[0]) / Math.abs((v3[1]-v1[1] == 0) ? 1 : (v3[1]-v1[1] ));
		double invslope2 = (v3[0] - v2[0]) / Math.abs((v3[1]-v2[1] == 0) ? 1 : (v3[1]-v2[1] ));
	
		double curx1 = v3[0];
		double curx2 = v3[0];
		
		
		double minZ = v1[2];
		double zStep = (v3[2]-v1[2])/(v3[1]-v1[1]); 
		
		for (double scanlineY = v3[1]; scanlineY > v1[1]; scanlineY--) {
			if(curx1<curx2) {
				for(int i=(int)curx1;i<curx2;i++) {
					if (i >= Screen.WIDTH || i <= 0 || scanlineY >= Screen.HEIGHT || scanlineY <= 0)
						continue;

					if(ZBuffer[i+(int)scanlineY*Screen.WIDTH]==-1 || ZBuffer[i+(int)scanlineY*Screen.WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*Screen.WIDTH]=color;
						//ZBuffer[i+(int)scanlineY*Screen.WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 -= invslope1;
				curx2 -= invslope2;
			}else {
				for(int i=(int)curx1;i>=curx2;i--) {
					if (i >= Screen.WIDTH || i <= 0 || scanlineY >= Screen.HEIGHT || scanlineY <= 0)
						continue;

					if(ZBuffer[i+(int)scanlineY*Screen.WIDTH]==-1 || ZBuffer[i+(int)scanlineY*Screen.WIDTH]>=minZ+zStep*scanlineY) {
						imgPix[i+(int)scanlineY*Screen.WIDTH]=color;
						//ZBuffer[i+(int)scanlineY*Screen.WIDTH] = minZ+zStep*scanlineY;
					}
				}
				curx1 -= invslope1;
				curx2 -= invslope2;
			}
		}
	}
	
	public static void drawTriangle(double[] point1, double[] point2, double[] point3, int color,int[] imgPix,double[] ZBuffer) {
		drawLine(point1, point2, color, imgPix, ZBuffer);
		drawLine(point2, point3, color, imgPix, ZBuffer);
		drawLine(point1, point3, color, imgPix, ZBuffer);
	}
	
	public static void drawLine(double[] point1, double[] point2, int color,int[] imgPix,double[] ZBuffer) {
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
					if ((int) point2[0] >=Screen.WIDTH || (int) point2[0] <= 0 || i>=Screen.HEIGHT || i <= 0)
						continue;
					
					if (ZBuffer[(int) point2[0] + i *Screen.WIDTH] >= minZpix + i * zPixStep
							|| ZBuffer[(int) point2[0] + i *Screen.WIDTH] == -1) {
						imgPix[(int) point2[0] + i * Screen.WIDTH] = color;
						//ZBuffer[(int) point2[0] + i *Screen.WIDTH] = minZpix + i * zPixStep;
					}
				}
			} else {
				double k = (point2[1] - point1[1]) / (point2[0] - point1[0]);
				int n = (int) (point2[1] - k * point2[0]);
				zPixStep = zPixdiff / (point2[0] - point1[0]);
				for (int x = (int) point1[0]; x <= (int) point2[0]; x++) {
					int y = (int) (x * k + n);

					if (x >=Screen.WIDTH || x <= 0 || y >=Screen.HEIGHT || y <= 0)
						continue;
					
					if (ZBuffer[x + y *Screen.WIDTH] >= minZpix + x * zPixStep || ZBuffer[x + y * Screen.WIDTH] == -1) {
						imgPix[x + y *Screen.WIDTH] = color;
						//ZBuffer[x + y *Screen.WIDTH] = minZpix + x * zPixStep;
					}
				}
			}
		} else {
			double k = (point1[1] - point2[1]) / (point1[0] - point2[0]);
			int n = (int) (point1[1] - k * point1[0]);

			zPixStep = zPixdiff / (point1[0] - point2[0]);
			for (int x = (int) point2[0]; x <= (int) point1[0]; x++) {
				int y = (int) (x * k + n);

				if (x >=Screen.WIDTH || x <= 0 || y >=Screen.HEIGHT || y <= 0)
					continue;

				if (ZBuffer[x + y *Screen.WIDTH] >= minZpix + x * zPixStep || ZBuffer[x + y *Screen.WIDTH] == -1) {
					imgPix[x + y *Screen.WIDTH] = color;
					//ZBuffer[x + y *Screen.WIDTH] = minZpix + x * zPixStep;
				}
			}
		}
	}
	
	private static int getColor(int r, int g, int b) {
		return (r << 16) + (g << 8) + b;
	}
}
