import java.util.function.Function;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Object that contains information of a single Node in the neural network
 */
public class Node implements Serializable{
    public static final long serialVersionUID = Long.parseLong("52120201111");

    public static enum ActivationFunction{
        GAUSSIAN(Node::gaussian),
        SINE(Node::sine),
        COSINE(Node::cosine),
        TANH(Node::tanh),
        SIGMOID(Node::sigmoid),
        BOUNDED_LINEAR(Node::boundedLinear),
        RELU(Node::relu),
        MODULUS(Node::modulus),
        ABSOLUTE_VALUE(Node::absoluteValue),
        INVERSE_ABSOLUTE_VALUE(Node::inverseAbsoluteValue),
        LINEAR(Node::linear),
        INVERSE(Node::inverse),
        PARABOLIC(Node::parabolic),
        CUBIC(Node::cubic),
        LOG(Node::log),
        EXPONENTIAL(Node::exponential),
        ALTERNATING_FLOOR(Node::alternatingFloor);

        private static final Random RANDOM = new Random();
        private static final List<ActivationFunction> VALUES = Arrays.asList(ActivationFunction.values());
        private static final int SIZE = VALUES.size();

        private Function<Double, Double> func;

        ActivationFunction(Function<Double, Double> func){
            this.func = func;
        }

        public double invoke(double x){
            return func.apply(x);
        }

        public static ActivationFunction random(){
            return VALUES.get(RANDOM.nextInt(SIZE));
        }

    }

    public enum NodeType{
        HIDDEN,
        INPUT,
        OUTPUT
    }

    private NodeType type;
    private ActivationFunction func = ActivationFunction.LINEAR;
    private LinkedHashMap<Node, Gene> outputs = new LinkedHashMap<Node, Gene>();
    private ArrayList<Node> inputs = new ArrayList<Node>();
    private boolean fired = false;
    private double output = 0;
    private int id = -1;

    /**
     * Constructor for the Node object with no default activation function
     * @param type The NodeType of the Node
     * @param id The ID of this Node
     */
    public Node(NodeType type, int id){
        this.type = type;
        this.id = id;

        if(type == NodeType.INPUT){
            this.fired = true;
        }

    }

    /**
     * Constructor for the Node object with a activation function
     * @param type The NodeType of the Node
     * @param func The ActivationFunction of the Node
     * @param id The ID of this Node
     */
    public Node(NodeType type, ActivationFunction func, int id){
        this.type = type;
        this.func = func;
        this.id = id;

        if(type == NodeType.INPUT){
            this.fired = true;
        }

    }

    /**
     * Adds an input to the Node's list of input nodes
     * @param node The Node to be added
     */
    public void addInput(Node node){
        inputs.add(node);
    }

    /**
     * Adds an output to the Node's list of output nodes and its corresponding Gene
     * @param node The Node to be added
     * @param gene The Gene containing the information of the link between this Node and that Node
     */
    public void addOutput(Node node, Gene gene){
        outputs.put(node, gene);
    }

    /**
     * Returns the associated Gene of a Node
     * @param node The Node of the corresponding Gene in question
     * @return Returns the associated Gene of the Node
     */
    public Gene getGene(Node node){
        return outputs.get(node);
    }

    /**
     * Recursive method that gets all the outputs from its inputs before returning an output
     * @return Returns the output of the Node
     */
    public double getOutput(){
        if(fired) return output;
        double rawOutput = 0;
        fired = true;

        for(Node node : inputs){
            rawOutput += node.getGene(this).invoke(node.getOutput());
        }

        output = func.invoke(rawOutput);
        return output;
    }

    /**
     * Sets the output of the Node
     * @param output The value to be outputted by this Node
     */
    public void setOutput(double output){

        if(type == NodeType.INPUT){
            this.output = output;
        } else{
            throw new Error("Tried to set output for non-input node");
        }

    }

    /**
     * Resets Node to default state
     */
    public void reset(){

        if(type != NodeType.INPUT){
            fired = false;
            output = 0;
        }

    }

    /**
     * Set method to set Node's ActivationFunction
     * @param func The ActivationFunction to be invoked by this Node
     */
    public void setActivationFunction(ActivationFunction func){this.func = func;}

    /**
     * Get method to get the Node's NodeType
     * @return Returns the NodeType specifying this Node's purpose
     */
    public NodeType getType(){return type;}

    /**
     * Get method to get the Node's ID
     * @return Returns the Node's ID
     */
    public int getID(){return id;}

    /**
     * Normal distribution function (0, 0.9974]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double gaussian(double x){
        return 1.0 / (0.4 * Math.sqrt(2.0 * Math.PI)) * Math.pow(Math.E, -Math.pow(x, 2.0) / (2.0 * Math.pow(0.4, 2.0)));
    }

    /**
     * Absolute value function [0, INF)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double absoluteValue(double x){
        return Math.abs(x);
    }

    /**
     * Inverted absolute value function (-INF, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double inverseAbsoluteValue(double x){
        return 1.0 - Math.abs(x);
    }

    /**
     * Sine function [-1, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double sine(double x){
        return Math.sin(x * 2.0 * Math.PI);
    }

    /**
     * Cosine function [-1, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double cosine(double x){
        return Math.cos(x * 2.0 * Math.PI);
    }

    /**
     * Sigmoid function [-1, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double sigmoid(double x){
        return 2.0 / (1.0 + Math.pow(Math.E, -x)) - 1.0;
    }

    /**
     * Bounded linear function [-1, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double boundedLinear(double x){

        if(x > 1){
            return 1;
        } else if(x < -1){
            return -1;
        } else{
            return x;
        }

    }

    /**
     * ReLU function [0, INF)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double relu(double x){
        return Math.max(0, x);
    }

    /**
     * Modulus function [0, 1)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double modulus(double x){
        return x % 1.0;
    }

    /**
     * Linear function (-INF, INF)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double linear(double x){
        return x;
    }

    /**
     * Hyperbolic tangent function (-1, 1)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double tanh(double x){
        return Math.tanh(x * Math.PI);
    }

    /**
     * Modified inverse function [-1, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double inverse(double x){

        if(x > 0){
            return 1.0 / (x + 1);
        } else if(x == 0){
            return 0;
        } else{
            return 1.0 / (x - 1);
        }

    }

    /**
     * Modified parabolic function [0, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double parabolic(double x){
        return Math.pow(Math.abs(x) % 1.0, 2);
    }

    /**
     * Modified cubic function [-1, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double cubic(double x){
        return Math.pow((x + 1) % 2.0 - 1, 3);
    }

    /**
     * Modified log function [-1, INF)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double log(double x){

        if(x > 0){
            return Math.log(x + 1.0 / Math.E);
        } else if(x == 0){
            return 0;
        } else{
            return -Math.log(-x + 1.0 / Math.E);
        }

    }

    /**
     * Modified exponential function [1/4, 1)
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double exponential(double x){
        return Math.pow(2, (x + 1) % 2.0 - 2);
    }

    /**
     * Floor and modulus function [0, 1]
     * @param x The input to the function
     * @return Returns the output of the function
     */
    private static double alternatingFloor(double x){
        return Math.floor(x) % 2.0;
    }

}