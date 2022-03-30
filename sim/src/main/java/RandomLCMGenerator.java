public class RandomLCMGenerator {
    private final long seed;
    private long lastOutput;
    private static final long MULTIPLIER = 420666997;
    private static final long INCREMENT = 1000001;
    private static final long MODULUS_BASE = 281474976710656L;

    public RandomLCMGenerator(long seed) {
        this.seed = seed;
        this.lastOutput = seed;
    }

    public double nextDouble() {
        this.lastOutput = ((MULTIPLIER * this.lastOutput) + INCREMENT)% MODULUS_BASE;

        return this.lastOutput/(double) MODULUS_BASE;
    }

    private long getSeed() {
        return this.seed;
    }

    public static void main(String[] args) {
        RandomLCMGenerator random = new RandomLCMGenerator(17);
        for(int i = 0; i < 50; i++) {
            System.out.println(random.nextDouble());
        }
    }
}
