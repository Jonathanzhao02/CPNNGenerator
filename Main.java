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
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The main class of the application
 */
public class Main{
	static final Random RANDOM = new Random();
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

	// credit to stackoverflow for providing code
	/**
	 * Returns a BufferedImage screenshot of a specific component
	 * @param component The component to take a picture of
	 * @return Returns a BufferedImage of the component
	 */
	public static BufferedImage getScreenShot(Component component){
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
		component.paint(image.getGraphics());
		return image;
	}

	/**
	 * The main function of the application
	 * @param rArgs The arguments used for the application
	 */
	public static void main(String[] rArgs){
		// extracts arguments from user's input
		printOptions();
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(rArgs));
		args.forEach(obj -> {
			parseString((String) obj);
		});

		// sets frame properties
		frame.setSize(resolution, resolution);

		if(minimized){
			frame.setState(JFrame.ICONIFIED);
		}

		if(animate){
			frame.setVisible(true);
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Genome genome = createGenome();

		// gets all outputs from genome network and finds min/max values
		double maxValR = -Double.MAX_VALUE;
		double minValR = Double.MAX_VALUE;

		double maxValG = -Double.MAX_VALUE;
		double minValG = Double.MAX_VALUE;

		double maxValB = -Double.MAX_VALUE;
		double minValB = Double.MAX_VALUE;

		double[][][] vals = new double[numTiles][numTiles][3];
		double rawCounter = 0;

		Genome.InputType[] inputTypes = genome.getInputs();

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				double x = 2.0 * (double) i / (numTiles - 1) - 1;
				double y = 2.0 * (double) j / (numTiles - 1) - 1;
				double dist = Math.sqrt(x * x + y * y);
				double theta = Math.asin(y / dist) / Math.PI * 2;
				double counter = 2.0 * rawCounter / numTiles / numTiles - 1;
				double[] state = order(x, y, dist, theta, counter, inputTypes);
				vals[i][j] = genome.predict(state);
				
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

		// pushes raw values to range [0-254]
		int[][][] colorVals = new int[numTiles][numTiles][3];

		for(int i = 0; i < numTiles; i++){

			for(int j = 0; j < numTiles; j++){
				colorVals[i][j][0] = minMax(0, 254, (int) (254.0 * (vals[i][j][0] - minValR) / (maxValR - minValR)));
				colorVals[i][j][1] = minMax(0, 254, (int) (254.0 * (vals[i][j][1] - minValG) / (maxValG - minValG)));
				colorVals[i][j][2] = minMax(0, 254, (int) (254.0 * (vals[i][j][2] - minValB) / (maxValB - minValB)));
			}

		}

		// graphically shows neural network outputs
		frame.add(new JCanvas(resolution, numTiles, colorVals));
		frame.validate();

		if(!animate){
			frame.setVisible(true);
		}

		// saves genome and image if user requested to do so
		if(save || !fileName.equals("pattern")){
			BufferedImage img = getScreenShot(frame.getContentPane());

			try{
				ImageIO.write(img, "png", new File("patterns/" + fileName + ".png"));

				if(loadFile == null){
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("patterns/" + fileName + ".gen"));
					oos.writeObject(genome);
					oos.close();
				}

			} catch(Exception e){
				e.printStackTrace();
			}

		}

		// exits if user does not want window to pop up at any time
		if(minimized){
			System.exit(0);
		}

	}

	/**
	 * Prints all user arguments
	 */
	private static void printOptions(){
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
	}

	/**
	 * Creates the genome to be used to generate a pattern, either loading it or creating one
	 * @return Returns a Genome
	 */
	private static Genome createGenome(){
		Genome genome = null;

		// attempts to load Genome from user file
		if(loadFile != null){

			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("patterns/" + loadFile + ".gen"));
				genome = (Genome) ois.readObject();
				System.out.println("Loaded genome");
				ois.close();
			} catch(Exception e){
				e.printStackTrace();
				System.out.println("\nExiting to avoid possible overwrite...");
				System.exit(0);
			}

		}

		// creates new Genome and randomly mutates it according to user's complexity argument
		if(genome == null){
			genome = new Genome(randomInputs(), 3, false);
			genome.compile();
			
			for(int i = 0; i < complexity; i++){

				if(Math.random() >= 0.5){
					genome.generateLink();
				} else{
					genome.generateNode();
				}

				genome.mutateWeights();
				genome.mutateActivation();

				genome.compile();
			}

		}

		return genome;
	}

	/**
	 * Pushes value to within a range
	 * @param min Minimum value of range
	 * @param max Maximum value of range
	 * @param val Actual value
	 * @return Returns an integer between min and max
	 */
	private static int minMax(int min, int max, int val){

		if(val < min){
			return min;
		} else if(val > max){
			return max;
		} else{
			return val;
		}

	}

	/**
	 * Creates random list of inputs for Genome object
	 * @return Returns array of InputType enumerations
	 */
	private static Genome.InputType[] randomInputs(){
		int length = RANDOM.nextInt(5) + 1;
		HashSet<Genome.InputType> set = new HashSet<Genome.InputType>();
		Genome.InputType[] inputTypes = {};

		while(set.size() < length){
			set.add(Genome.InputType.random());
		}

		return set.toArray(inputTypes);
	}

	/**
	 * Creates a state according to the InputType array of the Genome
	 * @param x The x coordinate of the square between -1 and 1
	 * @param y The y coordinate of the square between -1 and 1
	 * @param dist The distance of the coordinate to the origin (0, 0)
	 * @param theta The angle of the coordinate relative to the origin (0, 0), divided by PI such that the value is between -1 and 1
	 * @param counter A counter that goes from -1 to 1 over the course of iterating over the entire grid
	 * @param inputs The InputType array of the current Genome
	 * @return Returns an array of doubles containing all the inputs to the Genome network
	 */
	private static double[] order(double x, double y, double dist, double theta, double counter, Genome.InputType[] inputs){
		double[] state = new double[inputs.length];

		for(int i = 0; i < inputs.length; i++){
			
			switch(inputs[i]){
				case X:
					state[i] = x;
					break;
				case Y:
					state[i] = y;
					break;
				case DIST:
					state[i] = dist;
					break;
				case THETA:
					state[i] = theta;
					break;
				case COUNTER:
					state[i] = counter;
					break;
			}

		}

		return state;
	}

	/**
	 * Extracts arguments from a single String
	 * @param str The String that contains the argument
	 */
	private static void parseString(String str){

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

	}
	
}

/**
 * JPanel object extended to take a matrix of RGB values to paint onto the component
 */
class JCanvas extends JPanel{
	public static final long serialVersionUID = 10483782;
	private int resolution;
	private int numTiles;
	private int[][][] colorVals;

	/**
	 * Constructor of the JCanvas object
	 * @param resolution How many pixels per side of the component
	 * @param numTiles How many tiles to render per side of the component
	 * @param colorVals The RGB integer matrix to render
	 */
	public JCanvas(int resolution, int numTiles, int[][][] colorVals){
		this.resolution = resolution;
		this.numTiles = numTiles;
		this.colorVals = colorVals;
		setPreferredSize(new Dimension(resolution, resolution));
	}

	/**
	 * Overridden paint method to allow RGB matrix to be rendered instead
	 * @param gr The Graphics object of the JCanvas
	 */
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