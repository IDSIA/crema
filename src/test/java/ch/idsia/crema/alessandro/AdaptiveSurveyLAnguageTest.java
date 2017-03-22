package ch.idsia.crema.alessandro;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: Credo3
 * Date:    21.02.2017 16:22
 */
public class AdaptiveSurveyLAnguageTest {

    // debug and thread configuration ----------------------------------------------------------------------------------
    private static final boolean DEBUG = false;
    private static final int THREAD_POOL_SIZE = 8;

    // adaptive configuration data -------------------------------------------------------------------------------------

    private static final String credalFileName = "src/test/resources/adaptive/cnParameters.txt";
    private static final String[] csvs = {
            "/adaptive/Hören2015-16.csv",
            "/adaptive/Kommunikation2015-16.csv",
            "/adaptive/Lesen2015-16.csv",
            "/adaptive/Wortschatz und Strukturen2015-16.csv"
    };

    private static final int skillNumber = 4;
    private static final int levelNumber = 4;
    private static final int states = levelNumber;

    /** Fixed andom seed. */
    private static final long seed = 42;

    /** First id, inclusive. */
    private static final int minStudent = 0;
    /** Last id, exclusive. */
    private static final int maxStudent = 1;

    /** Minimum value of entropy to stop the survey. */
    private static final double STOP_THRESHOLD = 0.25;

    // object variables ------------------------------------------------------------------------------------------------
    private int student;
    private int i = 0;
    private int questionAnswered = 0;

    private double[][] askedQuestion = new double[skillNumber][levelNumber]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
    private double[][] rightQuestion = new double[skillNumber][levelNumber]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
    private double[][][] results = new double[skillNumber][][];

    private AnswerSet[] qs = new AnswerSet[skillNumber];

    private AdaptiveTests at;
    private AbellanEntropy ae;
    private QuestionSet q;

    private final Random random;


    public static void main(String[] args) throws IOException {
        ExecutorService es = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        final Path path = Paths.get(
                System.getProperty("user.home") + "/output_" +
                        new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss")
                                .format(new Date()) + " .txt");

        // for each student
        for (int student = minStudent; student < maxStudent; student++) {
            final int studentId = student;
            es.submit(() -> {
                System.out.println("Start for student " + studentId);

                AdaptiveSurveyLAnguageTest aslat = new AdaptiveSurveyLAnguageTest(studentId);
                aslat.test();

                saveToFile(aslat, path);
            });
        }

        es.shutdown();
    }

    private static synchronized void saveToFile(AdaptiveSurveyLAnguageTest aslat, Path path) {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            StringBuilder out = new StringBuilder();
            out.append(String.format("%3d %2d ", aslat.student, aslat.questionAnswered));

            double[][][] results = aslat.getResults();
            for (int s = 0; s < skillNumber; s++) {
                // interval dominance
                int[] dominating = intervalDominance(results[s][0], results[s][1]);

                out.append(s).append(": [ ");
                for (int d : dominating) {
                    out.append(d).append(" ");
                }
                out.append("]\n");

//                out
//                        .append(s).append(": ")
//                        .append(Arrays.toString(results[s][0])).append("\n")    // lower
//                        .append(Arrays.toString(results[s][1])).append("\n");   // upper
            }
            out.append("\n");

            bw.write(out.toString());
        } catch (IOException ignored) { }
    }

    private static int[] intervalDominance(double[] lowers, double[] uppers) {
        int n = lowers.length;

        // ordered from min to max
        int[] lOrdered = IntStream.range(0, lowers.length)
                .boxed().sorted(Comparator.comparingDouble(a -> lowers[a]))
                .mapToInt(e -> e).toArray();
        int[] uOrdered = IntStream.range(0, uppers.length)
                .boxed().sorted(Comparator.comparingDouble(a -> uppers[a]))
                .mapToInt(e -> e).toArray();

        List<Integer> dominating = new ArrayList<>();

        // remember min/max for up/low
        double maxU = 0.0;
        double minL = Double.MAX_VALUE;

        for (int i = n-1; i > 0; i--) {
            dominating.add(lOrdered[i]);
            if (maxU < uppers[lOrdered[i]]) {
                maxU = uppers[lOrdered[i]];
            }
            if (minL > lowers[lOrdered[i]]) {
                minL = lowers[lOrdered[i]];
            }

            if (minL > uppers[uOrdered[i-1]]) {
                break;
            }
        }

        int[] dominatingInts = new int[dominating.size()];
        for (int i = 0; i < dominating.size(); i++) {
            dominatingInts[i] = dominating.get(i);
        }

        return dominatingInts;
    }

    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     * @param student refernece id of the students
     */
    private AdaptiveSurveyLAnguageTest(int student) {
        this.student = student;

        random = new Random(seed + student);
        at = new AdaptiveTests();
        ae = new AbellanEntropy();
        q = new QuestionSet();
        q.loadKeyList();

        for (int i = 0; i < qs.length; i++) {
            qs[i] = new AnswerSet().load(AdaptiveSurveyLAnguageTest.class.getResourceAsStream(csvs[i]));
        }
    }

    /**
     * Perform the adaptive test with the initialized data.
     */
    private void test() {
        boolean stop;
        do {
            // search for next question using
            double maxIG = 0.0;
            int nextS = -1;
            int nextL = -1;

            for (int s = 0; s < skillNumber; s++) {
                results[s] = at.germanTest(credalFileName, s, askedQuestion, rightQuestion);

                // entropy of the skill
                double[] distribution = ae.getMaxEntro(results[s][0], results[s][1]);
                double HS = H(distribution, states);

                for (int l = 0; l < levelNumber; l++) {
                    List<Integer> availableQuestions = q.getQuestions(s, l);

                    // compute entropy only if we have questions available
                    if (availableQuestions.size() == 0) {
                        System.out.println("No more question for skill " + s + " level " + l);
                        continue;
                    }

                    double[] HResults = new double[2];
                    for (int r = 0; r < 2; r++) {
                        askedQuestion[s][l] += 1;
                        rightQuestion[s][l] += r;

                        results[s] = at.germanTest(credalFileName, s, askedQuestion, rightQuestion);

                        if (DEBUG) System.out.println(Arrays.toString(results[0]));    // lower
                        if (DEBUG) System.out.println(Arrays.toString(results[1]));    // upper

                        computeEntropy(results[s], HResults, s, l, r);

                        // clear
                        askedQuestion[s][l] -= 1;
                        rightQuestion[s][l] -= r;
                    }

                    // ma between right and wrong
                    double H = Math.max(HResults[0], HResults[1]);
                    if (DEBUG) System.out.println("H = " + H);

                    double ig = HS - H; // infogain

                    // minimize
                    if (ig < 0) {
                        System.err.println("Negative information gain: " + s + " (" + HS + ") " + l + " (" + H + "): " + ig);
                    }
                    if (ig > maxIG) {
                        maxIG = H;
                        nextS = s;
                        nextL = l;
                    }
                }
            }

            if (maxIG == Double.MAX_VALUE) {
                System.err.println("No min entropy found! (maxIG = " + maxIG);
                break;
            }

            // get available questions
            List<Integer> availableQuestions = q.getQuestions(nextS, nextL);

            int indexQ = random.nextInt(availableQuestions.size());
            int nextQ = availableQuestions.get(indexQ);
            int answer = qs[nextS].getAnswer(student, nextQ);

            System.out.println(i + " next: " + nextS + " " + nextL + " (H=" + maxIG + "), Q=" + indexQ + " , answer: " + answer);

            questionAnswered++;
            availableQuestions.remove(indexQ);

            askedQuestion[nextS][nextL] += 1;
            rightQuestion[nextS][nextL] += answer;

            // stop criteria
            stop = true;
            for (int s = 0; s < skillNumber; s++) {
                results[s] = at.germanTest(credalFileName, s, askedQuestion, rightQuestion);

                // entropy of the skill
                double[] distribution = ae.getMaxEntro(results[s][0], results[s][1]);
                double HS = H(distribution, states);

                if (HS > STOP_THRESHOLD) {
                    System.out.println("HS(" + s + ") = " + HS + ", continue");
                    stop = false;
                    break;
                }
            }

            System.out.println("Asked question " + ArrayUtils.toString(askedQuestion));
            System.out.println("Right question " + ArrayUtils.toString(rightQuestion));

            if (q.isEmpty()) {
                System.out.println("All questions done!");
                break;
            }

            i++;
        } while (!stop);
        System.out.println("Done!");
    }

    private void computeEntropy(double[][] results, double[] HResults, int s, int l, int r) {
        if (DoubleStream.of(results[0]).sum() > 1 - 10E-15) {
            // precise model
            if (DEBUG) System.out.println("Precise model");
            HResults[r] = H(results[0], states);
        } else {
            // imprecise model
            double[] maxLocalEntropy = ae.getMaxEntro(results[0], results[1]);
            if (DEBUG) System.out.println(s + " " + l + " " + r + " " + Arrays.toString(maxLocalEntropy));
            HResults[r] = H(maxLocalEntropy, states);
        }
    }

    private double H(double[] d, int x) {
        double h = 0.0;

        for (double v : d) {
            double logXv = Math.log(v) / Math.log(x);
            h += v * logXv;
        }

        return -h;
    }

    private double[][][] getResults() {
        double[][][] res = new double[skillNumber][][];
        for (int s = 0; s < skillNumber; s++) {
            res[s] = at.germanTest(credalFileName, s, askedQuestion, rightQuestion);
        }

        return res;
    }
}
