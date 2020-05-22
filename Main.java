import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.imageio.ImageIO;

import java.awt.Component;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Main{
	static final int size = 1000;
	static final int numTiles = 200;
	static JFrame frame;

	// ripped from stackoverflow
	public static BufferedImage getScreenShot(Component component){
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
		component.paint(image.getGraphics());
		return image;
	}

	public static void main(String[] args){
		System.out.println("If you want it to save, run with args:");
		System.out.println("Arg 0: suffix of file name");
		System.out.println("Arg 1: any input will make it quit upon saving (so no visualization of the pattern)");
		frame = new JFrame("Canvas");
		frame.setSize(size, size);

		if(args.length > 1){
			frame.setState(frame.ICONIFIED);
		}

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new GridLayout(numTiles, numTiles));

		Genome test = new Genome(new HashMap<Integer, Gene>(), 2, 3);
		test.compile();
		
		for(int i = 0; i < 100; i++){

			if(Math.random() >= 0.5){
				test.generateLink();
			} else{
				test.generateNode();
			}

			test.compile();
		}

		double maxValR = Double.MIN_VALUE;
		double minValR = Double.MAX_VALUE;

		double maxValG = Double.MIN_VALUE;
		double minValG = Double.MAX_VALUE;

		double maxValB = Double.MIN_VALUE;
		double minValB = Double.MAX_VALUE;

		double[][][] vals = new double[numTiles][numTiles][3];

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				double[] state = {2.0 * (double) i / numTiles - 1, 2.0 * (double) j / numTiles - 1};
				vals[i][j] = test.predict(state);
				
				if(vals[i][j][0] > maxValR){
					maxValR = vals[i][j][0];
				} else if(vals[i][j][0] < minValR){
					minValR = vals[i][j][0];
				}

				if(vals[i][j][1] > maxValG){
					maxValG = vals[i][j][1];
				} else if(vals[i][j][1] < minValG){
					minValG = vals[i][j][1];
				}

				if(vals[i][j][2] > maxValB){
					maxValB = vals[i][j][2];
				} else if(vals[i][j][2] < minValB){
					minValB = vals[i][j][2];
				}

			}

		}

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				int r = (int) (254.0 * (vals[i][j][0] - minValR) / (maxValR - minValR));
				int g = (int) (254.0 * (vals[i][j][1] - minValG) / (maxValG - minValG));
				int b = (int) (254.0 * (vals[i][j][2] - minValB) / (maxValB - minValB));
				frame.getContentPane().add(new JCanvas(size / numTiles, r, g, b));
			}

		}

		frame.validate();

		if(args.length > 0){

			BufferedImage img = getScreenShot(frame.getContentPane());

			try{
				ImageIO.write(img, "png", new File("patterns/screenshot" + args[0] + ".png"));
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("patterns/network" + args[0] + ".gen"));
				oos.writeObject(test);
				oos.close();
			} catch(Exception e){
				e.printStackTrace();
			}

			if(args.length > 1){
				System.exit(0);
			}

		}

	}
	
}

class JCanvas extends JPanel{
	private int size;
	private int r;
	private int g;
	private int b;

	public JCanvas(int size, int r, int g, int b){
		this.size = size;
		this.r = Math.min(254, r);
		this.g = Math.min(254, g);
		this.b = Math.min(254, b);
		setPreferredSize(new Dimension(size, size));
	}

	@Override
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr;
		g2.setColor(new Color(r, g, b));
		g2.fillRect(0, 0, size, size);
	}
	
}