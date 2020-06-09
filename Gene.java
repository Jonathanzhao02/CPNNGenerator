import java.io.Serializable;
import java.util.Random;

/**
 * The object that contains link information between two Nodes
 */
public class Gene implements Serializable{
    public static final long serialVersionUID = Long.parseLong("52120201110");

    private static final Random RANDOM = new Random();
    private int input;
    private int output;
    private double weight;
    private int innovationNumber;
    public boolean enabled = true;

    /**
     * Constructor for the Gene object with a pre-defined weight
     * @param input The ID of the input Node
     * @param output The ID of the output Node
     * @param weight The weight of the connection
     * @param innovationNumber The Gene's innovation number
     */
    public Gene(int input, int output, double weight, int innovationNumber){
        this.input = input;
        this.output = output;
        this.weight = weight;
        this.innovationNumber = innovationNumber;
    }

    /**
     * Constructor for the Gene object with a random weight
     * @param input The ID of the input Node
     * @param output The ID of the output Node
     * @param innovationNumber The Gene's innovation number
     */
    public Gene(int input, int output, int innovationNumber){
        this.input = input;
        this.output = output;
        this.weight = RANDOM.nextGaussian();
        this.innovationNumber = innovationNumber;
    }

    /**
     * Multiplies input by the Gene's weight
     * @param x The input to the Gene
     * @return Returns the input multiplied by the Gene's weight
     */
    public double invoke(double x){
        return x * weight;
    }

    /**
     * Perturbs the Gene's weight by a defined amount
     * @param x The amount to perturb the Gene's weight by
     */
    public void mutateWeight(double x){
        weight += x;
    }

    /**
     * Get method for the Gene's innovation number
     * @return Returns the Gene's innovation number
     */
    public int getInnovationNumber(){return innovationNumber;}

    /**
     * Get method for the Gene's input Node ID
     * @return Returns the Gene's input Node ID
     */
    public int getInput(){return input;}

    /**
     * Get method for the Gene's output Node ID
     * @return Returns the Gene's output Node ID
     */
    public int getOutput(){return output;}

    /**
     * Get method for the Gene's weight
     * @return Returns the weight of the Gene's link
     */
    public double getWeight(){return weight;}
}