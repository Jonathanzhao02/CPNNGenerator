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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Main{
	static final int DEF_SIZE = 1000;
	static final int DEF_TILES = 200;
	static final int DEF_COMPLEXITY = 100;
	static JFrame frame;

	static int size = DEF_SIZE;
	static int numTiles = DEF_TILES;
	static int complexity = DEF_COMPLEXITY;
	static boolean animate = false;
	static boolean minimized = false;
	static String fileName = "pattern";
	static String loadFile = null;

	// ripped from stackoverflow
	public static BufferedImage getScreenShot(Component component){
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
		component.paint(image.getGraphics());
		return image;
	}

	public static void main(String[] rArgs){
		System.out.println("Please type in arguments as (ARG NAME)=(ARG VALUE)");
		System.out.println("Args descriptions:");
		System.out.println("RES: Total resolution (int)");
		System.out.println("TILES: How many tiles to render (int)");
		System.out.println("CMPLX: Network complexity (int)");
		System.out.println("ANIM: If rendering should be animated (true/false)");
		System.out.println("MIN: If window should never pop up (true/false)");
		System.out.println("FILE: Screenshot and network file names (string)");
		System.out.println("LOAD: Load network file name without extension (string)");
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(rArgs));
		args.ensureCapacity(6);
		args.forEach(obj -> {
			String str = (String) obj;

			if(str == null){
				return;
			} else if(str.contains("RES=")){
				
				try{
					size = Integer.parseInt(str.substring(4, str.length()));
				} catch(Exception e){
					System.out.println("Could not read RES");
					size = DEF_SIZE;
				}

			} else if(str.contains("TILES=")){

				try{
					numTiles = Integer.parseInt(str.substring(6, str.length()));
				} catch(Exception e){
					System.out.println("Could not read TILES");
					numTiles = DEF_TILES;
				}

			} else if(str.contains("CMPLX=")){

				try{
					complexity = Integer.parseInt(str.substring(6, str.length()));
				} catch(Exception e){
					System.out.println("Could not read CMPLX");
					complexity = DEF_COMPLEXITY;
				}

			} else if(str.contains("ANIM=")){
				
				try{
					animate = Boolean.parseBoolean(str.substring(5, str.length()));
				} catch(Exception e){
					System.out.println("Could not read ANIM");
					animate = false;
				}

			} else if(str.contains("MIN=")){

				try{
					minimized = Boolean.parseBoolean(str.substring(4, str.length()));
				} catch(Exception e){
					System.out.println("Could not read MIN");
					minimized = false;
				}

			} else if(str.contains("FILE=")){

				try{
					fileName = str.substring(5, str.length());
				} catch(Exception e){
					System.out.println("Could not read FILE");
					fileName = "pattern";
				}

			} else if(str.contains("LOAD=")){

				try{
					loadFile = str.substring(5, str.length());
				} catch(Exception e){
					System.out.println("Could not read LOAD");
					loadFile = null;
				}

			}

		});

		frame = new JFrame("Canvas");
		frame.setSize(size, size);

		if(minimized){
			frame.setState(frame.ICONIFIED);
		}

		if(animate){
			frame.setVisible(true);
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(numTiles, numTiles));

		Genome test = null;

		if(loadFile != null){

			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("patterns/" + loadFile + ".gen"));
				test = (Genome) ois.readObject();
				System.out.println("Loaded genome");
				ois.close();
			} catch(Exception e){
				e.printStackTrace();
				test = null;
			}

		}

		if(test == null){
			test = new Genome(new HashMap<Integer, Gene>(), 2, 3);
			test.compile();
			
			for(int i = 0; i < DEF_COMPLEXITY; i++){

				if(Math.random() >= 0.5){
					test.generateLink();
				} else{
					test.generateNode();
				}

				test.compile();
			}

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

		if(!animate){
			frame.setVisible(true);
		}

		BufferedImage img = getScreenShot(frame.getContentPane());

		try{
			ImageIO.write(img, "png", new File("patterns/" + fileName + ".png"));
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("patterns/" + fileName + ".gen"));
			oos.writeObject(test);
			oos.close();
		} catch(Exception e){
			e.printStackTrace();
		}

		if(minimized){
			System.exit(0);
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
		this.r = Math.max(0, this.r);
		this.g = Math.min(254, g);
		this.g = Math.max(0, this.g);
		this.b = Math.min(254, b);
		this.b = Math.max(0, this.b);
		setPreferredSize(new Dimension(size, size));
	}

	@Override
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr;
		g2.setColor(new Color(r, g, b));
		g2.fillRect(0, 0, size, size);
	}
	
}