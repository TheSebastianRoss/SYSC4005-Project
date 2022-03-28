public class RandomLCMGenerator {
    private final double seed;
    private double nextOutput;
    private double multiplier;
    private double increment;
    private double modulusBase;

    public RandomLCMGenerator(double seed, double multiplier, double increment, double modulusBase) {
        this.seed = seed;
        this.multiplier = multiplier;
        this.increment = increment;
        this.modulusBase = modulusBase;
        this.nextOutput = seed;
    }

    public double generate() {
        double output = this.nextOutput;

        this.nextOutput = ((this.multiplier * output) + this.increment)%this.modulusBase;

        return output;
    }

    private double getSeed() {
        return this.seed;
    }
}
