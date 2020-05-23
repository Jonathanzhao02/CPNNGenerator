import java.io.Serializable;
import java.util.Random;

public class Gene implements Serializable{
    static final long serialVersionUID = Long.parseLong("52120201110");

    private static final Random RANDOM = new Random();
    private int input;
    private int output;
    private double weight;
    private int innovationNumber;
    private Node.ActivationFunction func;
    public boolean enabled = true;

    public Gene(int input, int output, double weight, int innovationNumber, Node.ActivationFunction func){
        this.input = input;
        this.output = output;
        this.weight = weight;
        this.innovationNumber = innovationNumber;
        this.func = func;
    }

    public Gene(int input, int output, int innovationNumber, Node.ActivationFunction func){
        this.input = input;
        this.output = output;
        this.weight = RANDOM.nextGaussian();
        this.innovationNumber = innovationNumber;
        this.func = func;
    }

    public double invoke(double x){
        return x * weight;
    }

    public void mutateWeight(double x){
        weight += x;
    }

    public void mutateActivation(){
        func = Node.ActivationFunction.random();
    }

    public void setActivationFunction(Node.ActivationFunction func){this.func = func;}
    public Node.ActivationFunction getActivationFunction(){return func;}
    public int getInnovationNumber(){return innovationNumber;}
    public int getInput(){return input;}
    public int getOutput(){return output;}
    public double getWeight(){return weight;}
}