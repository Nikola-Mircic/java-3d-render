package nm;

import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	private Screen screen;
	
	public InputHandler(Screen screen) {
		this.screen = screen;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		screen.Zmove += ((double)e.getWheelRotation())/3;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			screen.Xmove -= 0.05;
			break;
		case KeyEvent.VK_RIGHT:
			screen.Xmove += 0.05;
			break;
		case KeyEvent.VK_UP:
			screen.Ymove += 0.05;
			break;
		case KeyEvent.VK_DOWN:
			screen.Ymove -= 0.05;
			break;
		case KeyEvent.VK_R:
			screen.Xmove = 0;
			screen.Ymove = 0;
			screen.Zmove = 0;
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
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		screen.lastX = -1;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		if (screen.lastX == -1)
			screen.lastX = x;
		
		screen.rotationX += (x - screen.lastX) / 3;
		screen.lastX = x;
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
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}
