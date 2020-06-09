import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import java.util.Arrays;

/**
 * The object that contains the neural network to be used for generating an image
 */
public class Genome implements Serializable{
    public static final long serialVersionUID = Long.parseLong("52120201109");

    public static enum InputType{
        X,
        Y,
        DIST,
        THETA,
        COUNTER;

        private static final Random RANDOM = new Random();
        private static final List<InputType> VALUES = Arrays.asList(InputType.values());
        private static final int SIZE = VALUES.size();

        public static InputType random(){
            return VALUES.get(RANDOM.nextInt(SIZE));
        }

    }

    private static int GLOBAL_INNOVATION_NUMBER = 0;
    private static HashSet<Gene> MUTATIONS = new HashSet<Gene>();
    private static final Random RANDOM = new Random();

    private static double PERTURB_CHANCE = 0.8;
    private static double PERTURB_MAGNITUDE = 0.05;
    private static double ACTIVATION_MUTATION_RATE = 2.5;

    private HashMap<Integer, Node> network = new HashMap<Integer, Node>();
    private HashMap<Integer, Gene> genome = new HashMap<Integer, Gene>();
    private HashMap<Integer, Node.ActivationFunction> nodeGenome = new HashMap<Integer, Node.ActivationFunction>();

    private InputType[] inputTypes;

    private int inputSize;
    private int outputSize;

    /**
     * The constructor of a new Genome object
     * @param inputTypes The array of InputTypes to be used for constructing inputs to this object's network
     * @param outputSize The number of outputs of the network
     * @param generateFully Whether a fully connected genome should be initially generated or not
     */
    public Genome(InputType[] inputTypes, int outputSize, boolean generateFully){
        this.inputTypes = inputTypes;
        this.inputSize = inputTypes.length;
        this.outputSize = outputSize;

        if(generateFully){
            generateFullyConnectedGenome();
        }

    }

    /**
     * The constructor of a Genome object from pre-existing data
     * @param genome The HashMap of genes to be used as a genome
     * @param nodeGenome The HashMap of ActivationFunctions to be used for each Node in the network
     * @param inputTypes The array of InputTypes to be used for constructing inputs to this object's network
     * @param outputSize The number of outputs of the network
     */
    public Genome(HashMap<Integer, Gene> genome, HashMap<Integer, Node.ActivationFunction> nodeGenome, InputType[] inputTypes, int outputSize){
        this.genome = genome;
        this.nodeGenome = nodeGenome;
        this.inputTypes = inputTypes;
        this.inputSize = inputTypes.length;
        this.outputSize = outputSize;
    }

    /**
     * Get method to return InputType array of this object
     * @return Returns InputType array of the Genome object
     */
    public InputType[] getInputs(){
        return inputTypes;
    }

    /**
     * Method to add a Gene to the Genome
     * @param gene The Gene to be added
     */
    public void addGene(Gene gene){

        if(genome.get(gene.getInnovationNumber()) != null){
            System.out.println("Gene already exists at " + gene.getInnovationNumber());
            return;
        }

        genome.put(gene.getInnovationNumber(), gene);
    }

    /**
     * Method to randomly perturb weights in the network
     */
    public void mutateWeights(){

        for(Gene gene : genome.values()){

            if(RANDOM.nextDouble() < PERTURB_CHANCE){
                gene.mutateWeight(RANDOM.nextGaussian() * PERTURB_MAGNITUDE);
            }

        }

    }

    /**
     * Gets a random Gene from the genome
     * @return Returns random Gene from the genome
     */
    public Gene randomGene(){
        return (Gene) (genome.values().toArray()[RANDOM.nextInt(genome.size())]);
    }

    /**
     * Method to randomize ActivationFunctions in the nodeGenome
     */
    public void mutateActivation(){
        if(network.size() <= inputSize + outputSize) return;

        double activationChance = ACTIVATION_MUTATION_RATE;

        while(activationChance > 0){

            if(RANDOM.nextDouble() < activationChance){
                nodeGenome.put(RANDOM.nextInt(network.size()), Node.ActivationFunction.random());
            }

            activationChance -= 1;
        }

    }

    /**
     * Generates a random link between two Nodes in the network
     */
    public void generateLink(){
        int input = 0;
        int output = 0;

        do{
            input = RANDOM.nextInt(network.size());
        } while(network.get(input).getType() == Node.NodeType.OUTPUT);

        int attempts = 0;

        do{
            output = RANDOM.nextInt(network.size());
            attempts++;

            if(attempts > 100){
                return;
            }

        } while(network.get(output).getType() == Node.NodeType.INPUT
        || checkMutation(input, output) < GLOBAL_INNOVATION_NUMBER
        || checkMutation(output, input) < GLOBAL_INNOVATION_NUMBER);

        Gene newGene = new Gene(input, output, GLOBAL_INNOVATION_NUMBER);
        genome.put(GLOBAL_INNOVATION_NUMBER, newGene);
        GLOBAL_INNOVATION_NUMBER += 1;
        MUTATIONS.add(newGene);
        // System.out.println("Added link between " + input + " and " + output);
    }

    /**
     * Generates a random Node splitting a pre-existing link between two Nodes in the network
     */
    public void generateNode(){

        if(genome.size() > 0){
            Gene gene = (Gene) genome.values().toArray()[RANDOM.nextInt(genome.size())];

            int newNode = network.size();
            int innovationNumber1 = checkMutation(gene.getInput(), newNode);
            int innovationNumber2 = checkMutation(newNode, gene.getOutput());

            if(!checkExistence(gene.getInput(), newNode) && !checkExistence(newNode, gene.getOutput())){

                if(innovationNumber1 == GLOBAL_INNOVATION_NUMBER && innovationNumber2 == GLOBAL_INNOVATION_NUMBER){
                    innovationNumber2 += 1;
                }

                Gene newGene1 = new Gene(gene.getInput(), newNode, 1, innovationNumber1);
                Gene newGene2 = new Gene(newNode, gene.getOutput(), gene.getWeight(), innovationNumber2);
                nodeGenome.put(newNode, Node.ActivationFunction.LINEAR);

                if(innovationNumber1 == GLOBAL_INNOVATION_NUMBER){
                    genome.put(innovationNumber1, newGene1);
                    GLOBAL_INNOVATION_NUMBER += 1;
                    MUTATIONS.add(newGene1);
                }

                if(innovationNumber2 == GLOBAL_INNOVATION_NUMBER){
                    genome.put(innovationNumber2, newGene2);
                    GLOBAL_INNOVATION_NUMBER += 1;
                    MUTATIONS.add(newGene2);
                }

                gene.enabled = false;
                // System.out.println("Split link between " + gene.getInput() + " and " + gene.getOutput() + " with " + newNode);
            }

        }

    }

    /**
     * Checks if a historical Gene already exists linking the input and output nodes
     * @param input The ID of the input node
     * @param output The ID of the output node
     * @return Returns the innovation number of the pre-existing Gene or the current GLOBAL_INNOVATION_NUMBER
     */
    public static int checkMutation(int input, int output){

        for(Gene gene : MUTATIONS){

            if(gene.getInput() == input && gene.getOutput() == output){
                return gene.getInnovationNumber();
            }

        }

        return GLOBAL_INNOVATION_NUMBER;
    }

    /**
     * Checks if a Gene already exists within the genome linking the input and output nodes
     * @param input The ID of the input node
     * @param output The ID of the output node
     * @return Returns whether the Gene exists or not
     */
    public boolean checkExistence(int input, int output){

        for(int i = 0; i < GLOBAL_INNOVATION_NUMBER; i++){
            Gene gene = genome.get(i);

            if(gene != null && gene.getInput() == input && gene.getOutput() == output){
                return true;
            }

        }

        return false;
    }

    /**
     * Generates a fully connected network between inputs and outputs
     */
    public void generateFullyConnectedGenome(){

        for(int i = 0; i < inputSize; i++){

            for(int j = 0; j < outputSize; j++){
                this.genome.put(i * outputSize + j, new Gene(i, j + inputSize + 1, i * outputSize + j));
            }

        }

        for(int i = 0; i < inputSize + outputSize; i++){
            nodeGenome.put(i, Node.ActivationFunction.LINEAR);
        }

    }

    /**
     * Generates the network from the genome and nodeGenome
     */
    public void compile(){

        // input + output node generation
        for(int i = 0; i < inputSize + 1; i++){
            this.network.put(i, new Node(Node.NodeType.INPUT, i));
        }

        this.network.get(inputSize).setOutput(1);

        for(int i = 0; i < outputSize; i++){
            this.network.put(i + inputSize + 1, new Node(Node.NodeType.OUTPUT, i));
        }

        // connection + hidden node generation
        for(int i = 0; i < GLOBAL_INNOVATION_NUMBER; i++){
            Gene gene = genome.get(i);

            if(gene != null && network.get(gene.getOutput()) == null && gene.enabled){

                if(nodeGenome.get(gene.getOutput()) == null){
                    nodeGenome.put(gene.getOutput(), Node.ActivationFunction.LINEAR);
                }

                network.put(gene.getOutput(), new Node(Node.NodeType.HIDDEN, gene.getOutput()));
            }

        }

        for(int i = 0; i < GLOBAL_INNOVATION_NUMBER; i++){
            Gene gene = genome.get(i);

            if(gene != null && network.get(gene.getInput()) == null && gene.enabled){

                if(nodeGenome.get(gene.getOutput()) == null){
                    nodeGenome.put(gene.getOutput(), Node.ActivationFunction.LINEAR);
                }

                network.put(gene.getInput(), new Node(Node.NodeType.HIDDEN, gene.getInput()));
            }

        }

        for(int i = 0; i < GLOBAL_INNOVATION_NUMBER; i++){
            Gene gene = genome.get(i);

            if(gene != null && gene.enabled){
                network.get(gene.getOutput()).addInput(network.get(gene.getInput()));
                network.get(gene.getInput()).addOutput(network.get(gene.getOutput()), gene);
            }

        }

        //activation function assignment
        for(int i = 0; i < network.size(); i++){
            network.get(i).setActivationFunction(nodeGenome.get(i));
        }

    }

    /**
     * Passes a state through the network to obtain outputs
     * @param state The double array of inputs
     * @return Returns the double array of outputs
     */
    public double[] predict(double[] state){
        if(state.length != inputSize) throw new Error("Unexpected input length");

        for(Node node : network.values()){
            node.reset();
        }

        for(int i = 0; i < inputSize; i++){
            network.get(i).setOutput(state[i]);
        }

        double[] outputs = new double[outputSize];

        for(int i = 0; i < outputSize; i++){
            outputs[i] = network.get(i + inputSize + 1).getOutput();
        }

        return outputs;
    }

    /**
     * Allows user to see entire genome printed out
     */
    public void printGenome(){

        for(Gene gene : genome.values()){
            System.out.println("\nBetween: " + gene.getInput() + " -> " + gene.getOutput());
            System.out.println("Activation function: " + nodeGenome.get(gene.getOutput()));
            System.out.println("Weight: " + gene.getWeight());
        }

    }

}