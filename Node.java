import java.util.function.Function;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class Node implements Serializable{
    static final long serialVersionUID = Long.parseLong("52120201111");

    public enum ActivationFunction{
        GAUSSIAN(Node::gaussian),
        SINE(Node::sine),
        COSINE(Node::cosine),
        TANH(Node::tanh),
        SIGMOID(Node::sigmoid),
        BOUNDED_LINEAR(Node::boundedLinear),
        RELU(Node::relu),
        MODULUS(Node::modulus),
        INVERSE_MODULUS(Node::inverseModulus),
        ABSOLUTE_VALUE(Node::absoluteValue),
        INVERSE_ABSOLUTE_VALUE(Node::inverseAbsoluteValue),
        LINEAR(Node::linear),
        INVERSE(Node::inverse),
        PARABOLIC(Node::parabolic),
        CUBIC(Node::cubic),
        LOG(Node::log),
        EXPONENTIAL(Node::exponential),
        ALTERNATING_FLOOR(Node::alternatingFloor),
        INVERSE_ALTERNATING_FLOOR(Node::inverseAlternatingFloor);

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

    public Node(NodeType type, int id){
        this.type = type;
        this.id = id;

        if(type == NodeType.INPUT){
            this.fired = true;
        }

    }

    public Node(NodeType type, ActivationFunction func, int id){
        this.type = type;
        this.func = func;
        this.id = id;

        if(type == NodeType.INPUT){
            this.fired = true;
        }

    }

    public void addInput(Node node){
        inputs.add(node);
    }

    public void addOutput(Node node, Gene gene){
        outputs.put(node, gene);
    }

    public Gene getGene(Node node){
        return outputs.get(node);
    }

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

    public void setOutput(double output){

        if(type == NodeType.INPUT){
            this.output = output;
        } else{
            throw new Error("Tried to set output for non-input node");
        }

    }

    public void reset(){

        if(type != NodeType.INPUT){
            fired = false;
            output = 0;
        }

    }

    public void setActivationFunction(ActivationFunction func){this.func = func;}
    public ActivationFunction getActivationFunction(){return func;}
    public NodeType getType(){return type;}
    public int getID(){return id;}

    private static double gaussian(double x){
        return 1.0 / (0.4 * Math.sqrt(2.0 * Math.PI)) * Math.pow(Math.E, -Math.pow(x, 2.0) / (2.0 * Math.pow(0.4, 2.0)));
    }

    private static double absoluteValue(double x){
        return Math.abs(x);
    }

    private static double inverseAbsoluteValue(double x){
        return 1.0 - Math.abs(x);
    }

    private static double sine(double x){
        return Math.sin(x * 2.0 * Math.PI);
    }

    private static double cosine(double x){
        return Math.cos(x * 2.0 * Math.PI);
    }

    private static double sigmoid(double x){
        return 2.0 / (1.0 + Math.pow(Math.E, -x)) - 1.0;
    }

    private static double boundedLinear(double x){

        if(x > 1){
            return 1;
        } else if(x < -1){
            return -1;
        } else{
            return x;
        }

    }

    private static double relu(double x){
        return Math.max(0, x);
    }

    private static double modulus(double x){
        return x % 1.0;
    }

    private static double inverseModulus(double x){
        return 1.0 - x % 1.0;
    }

    private static double linear(double x){
        return x;
    }

    private static double tanh(double x){
        return Math.tanh(x * Math.PI);
    }

    private static double inverse(double x){

        if(x > 0){
            return 1.0 / (x + 1);
        } else if(x == 0){
            return 0;
        } else{
            return 1.0 / (x - 1);
        }

    }

    private static double parabolic(double x){
        return Math.pow(x, 2.0);
    }

    private static double cubic(double x){
        return Math.pow(x, 3.0);
    }

    private static double log(double x){

        if(x > 0){
            return Math.log(x + 1.0 / Math.E);
        } else if(x == 0){
            return 0;
        } else{
            return -Math.log(-x + 1.0 / Math.E);
        }

    }

    private static double exponential(double x){
        return Math.pow(2, x - 1);
    }

    private static double alternatingFloor(double x){
        return Math.floor(x) % 2.0;
    }

    private static double inverseAlternatingFloor(double x){
        return 1 - Math.floor(x) % 2.0;
    }

}