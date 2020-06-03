import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	static final int DEF_RESOLUTION = 800;
	static final int DEF_TILES = 200;
	static final int DEF_COMPLEXITY = 100;
	static final JFrame frame = new JFrame("Canvas");

	static int resolution = DEF_RESOLUTION;
	static int numTiles = DEF_TILES;
	static int complexity = DEF_COMPLEXITY;
	static boolean animate = false;
	static boolean minimized = false;
	static boolean save = false;
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
		System.out.println("RES: How much pixel resolution per side (int)");
		System.out.println("TILES: How many tiles per side (int)");
		System.out.println("CMPLX: Network complexity (int)");
		System.out.println("ANIM: If rendering should be animated (true/false)");
		System.out.println("MIN: If window should never pop up (true/false)");
		System.out.println("SAVE: If the pattern should be saved to file (true/false)");
		System.out.println("FILE: Screenshot and network file names (string)");
		System.out.println("LOAD: Load network file name without extension (string)");
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(rArgs));
		args.forEach(obj -> {
			String str = (String) obj;

			if(str == null){
				return;
			} else if(str.contains("RES=")){
				
				try{
					resolution = Integer.parseInt(str.substring(4, str.length()));
				} catch(Exception e){
					System.out.println("Could not read RES");
					resolution = DEF_RESOLUTION;
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

			} else if(str.contains("SAVE=")){

				try{
					save = Boolean.parseBoolean(str.substring(5, str.length()));
				} catch(Exception e){
					System.out.println("Could not read SAVE");
					save = false;
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

		frame.setSize(resolution, resolution);

		if(minimized){
			frame.setState(JFrame.ICONIFIED);
		}

		if(animate){
			frame.setVisible(true);
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Genome test = null;

		if(loadFile != null){

			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("patterns/" + loadFile + ".gen"));
				test = (Genome) ois.readObject();
				System.out.println("Loaded genome");
				ois.close();
			} catch(Exception e){
				e.printStackTrace();
				System.out.println("\nExiting to avoid possible overwrite...");
				System.exit(0);
			}

		}

		if(test == null){
			test = new Genome(new HashMap<Integer, Gene>(), 5, 3);
			test.compile();
			
			for(int i = 0; i < DEF_COMPLEXITY; i++){

				if(Math.random() >= 0.5){
					test.generateLink();
				} else{
					test.generateNode();
				}

				test.mutateWeights();
				test.mutateActivation();

				test.compile();
			}

		}

		double maxValR = -Double.MAX_VALUE;
		double minValR = Double.MAX_VALUE;

		double maxValG = -Double.MAX_VALUE;
		double minValG = Double.MAX_VALUE;

		double maxValB = -Double.MAX_VALUE;
		double minValB = Double.MAX_VALUE;

		double[][][] vals = new double[numTiles][numTiles][3];
		double rawCounter = 0;

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				double x = 2.0 * (double) i / numTiles - 1;
				double y = 2.0 * (double) j / numTiles - 1;
				double dist = Math.sqrt(x * x + y * y);
				double theta = Math.asin(y / dist) / Math.PI * 2;
				double counter = 2.0 * rawCounter / numTiles / numTiles - 1;
				double[] state = {x, y, dist, theta, counter};
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

				rawCounter++;
			}

		}

		int[][][] colorVals = new int[numTiles][numTiles][3];

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				colorVals[i][j][0] = (int) (254.0 * (vals[i][j][0] - minValR) / (maxValR - minValR));
				colorVals[i][j][1] = (int) (254.0 * (vals[i][j][1] - minValG) / (maxValG - minValG));
				colorVals[i][j][2] = (int) (254.0 * (vals[i][j][2] - minValB) / (maxValB - minValB));
			}

		}

		frame.add(new JCanvas(resolution, numTiles, colorVals));
		frame.validate();

		if(!animate){
			frame.setVisible(true);
		}

		if(save || !fileName.equals("pattern")){
			BufferedImage img = getScreenShot(frame.getContentPane());

			try{
				ImageIO.write(img, "png", new File("patterns/" + fileName + ".png"));
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("patterns/" + fileName + ".gen"));
				oos.writeObject(test);
				oos.close();
			} catch(Exception e){
				e.printStackTrace();
			}

		}

		if(minimized){
			System.exit(0);
		}

	}
	
}

class JCanvas extends JPanel{
	public static final long serialVersionUID = 10483782;
	private int resolution;
	private int numTiles;
	private int[][][] colorVals;

	public JCanvas(int resolution, int numTiles, int[][][] colorVals){
		this.resolution = resolution;
		this.numTiles = numTiles;
		this.colorVals = colorVals;
		setPreferredSize(new Dimension(resolution, resolution));
	}

	@Override
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr;
		int size = resolution / numTiles;

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				g2.setColor(new Color(colorVals[i][j][0], colorVals[i][j][1], colorVals[i][j][2]));
				g2.fillRect(i * size, j * size, size, size);
			}

		}

	}
	
}