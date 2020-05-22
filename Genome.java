import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Genome{
    private static int GLOBAL_INNOVATION_NUMBER = 0;
    private static HashSet<Gene> MUTATIONS = new HashSet<Gene>();
    private static final Random RANDOM = new Random();
    private HashMap<Integer, Node> network = new HashMap<Integer, Node>();
    private HashMap<Integer, Gene> genome = new HashMap<Integer, Gene>();

    private int inputSize;
    private int outputSize;

    public Genome(int inputSize, int outputSize){
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        generateFullyConnectedGenome();
    }

    public Genome(HashMap<Integer, Gene> genome, int inputSize, int outputSize){
        this.genome = genome;
        this.inputSize = inputSize;
        this.outputSize = outputSize;
    }

    public void setGenome(HashMap<Integer, Gene> genome){this.genome = genome;}

    public void addGene(Gene gene){
        genome.put(gene.getInnovationNumber(), gene);
    }

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

        Gene newGene = new Gene(input, output, GLOBAL_INNOVATION_NUMBER, Node.ActivationFunction.random());
        genome.put(GLOBAL_INNOVATION_NUMBER, newGene);
        GLOBAL_INNOVATION_NUMBER += 1;
        MUTATIONS.add(newGene);
        // System.out.println("Added link between " + input + " and " + output);
    }

    public void generateNode(){

        if(genome.size() > 0){
            Gene gene = genome.get(RANDOM.nextInt(genome.size()));

            int newNode = network.size();
            int innovationNumber1 = checkMutation(gene.getInput(), newNode);
            int innovationNumber2 = checkMutation(newNode, gene.getOutput());

            if(innovationNumber1 == GLOBAL_INNOVATION_NUMBER && innovationNumber2 == GLOBAL_INNOVATION_NUMBER){
                Gene newGene1 = new Gene(gene.getInput(), newNode, 1, GLOBAL_INNOVATION_NUMBER, Node.ActivationFunction.random());
                Gene newGene2 = new Gene(newNode, gene.getOutput(), gene.getWeight(), GLOBAL_INNOVATION_NUMBER + 1, gene.getActivationFunction());
                genome.put(GLOBAL_INNOVATION_NUMBER, newGene1);
                genome.put(GLOBAL_INNOVATION_NUMBER + 1, newGene2);
                GLOBAL_INNOVATION_NUMBER += 2;
                MUTATIONS.add(newGene1);
                MUTATIONS.add(newGene2);
                gene.enabled = false;
                // System.out.println("Split link between " + gene.getInput() + " and " + gene.getOutput() + " with " + newNode);
            }

        }

    }

    public int checkMutation(int input, int output){

        for(Gene gene : MUTATIONS){

            if(gene.getInput() == input && gene.getOutput() == output){
                return gene.getInnovationNumber();
            }

        }

        return GLOBAL_INNOVATION_NUMBER;
    }

    public Gene checkExistence(int input, int output){

        for(Gene gene : genome.values()){

            if(gene.getInput() == input && gene.getOutput() == output){
                return gene;
            }

        }

        return null;
    }

    public HashMap<Integer, Gene> getGenome(){return genome;}

    public void generateFullyConnectedGenome(){

        for(int i = 0; i < inputSize; i++){

            for(int j = 0; j < outputSize; j++){
                this.genome.put(i * outputSize + j, new Gene(i, j + inputSize + 1, i * outputSize + j, Node.ActivationFunction.LINEAR));
            }

        }

    }

    public void compile(){

        // input + output node generation
        for(int i = 0; i < inputSize + 1; i++){
            this.network.put(i, new Node(Node.NodeType.INPUT));
        }

        this.network.get(inputSize).setOutput(1);

        for(int i = 0; i < outputSize; i++){
            this.network.put(i + inputSize + 1, new Node(Node.NodeType.OUTPUT));
        }

        // connection + hidden node generation
        for(Gene gene : genome.values()){

            if(network.get(gene.getOutput()) == null && gene.enabled){
                network.put(gene.getOutput(), new Node(Node.NodeType.HIDDEN, gene.getActivationFunction()));
            }

        }

        for(Gene gene : genome.values()){

            if(network.get(gene.getInput()) == null && gene.enabled){
                network.put(gene.getInput(), new Node(Node.NodeType.HIDDEN));
            }

        }

        for(Gene gene : genome.values()){

            if(gene.enabled){
                network.get(gene.getOutput()).addInput(network.get(gene.getInput()), gene);
            }

        }

    }

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

}