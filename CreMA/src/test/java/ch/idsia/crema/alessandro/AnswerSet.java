package ch.idsia.crema.alessandro;

import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: Credo3
 * Date:    21.02.2017 16:23
 */
class AnswerSet {

    /** answers foreach student */
    private int[][] answers;
    private int[] questionToIndex = null;

    AnswerSet() { }

    AnswerSet load(InputStream is) {
        List<String> records = new ArrayList<>();

        try (Scanner s = new Scanner(is)) {
            String line = s.nextLine();
            String[] tokens = line.split(",");
            questionToIndex = new int[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                questionToIndex[i] = Integer.parseInt(tokens[i].substring(1));
            }

            while (s.hasNext()) {
                line = s.nextLine();
                records.add(line);
            }
        }

        // -1 is to avoid header
        answers = new int[records.size()][];

        for (int i = 0; i < records.size(); i++) {
            String[] tokens = records.get(i).split(",");
            int answerNumber = tokens.length;

            // -1 is to avoid id
            answers[i] = new int[answerNumber];
            for (int j = 0; j < answerNumber; j++) {
                String token = tokens[j];
                answers[i][j] = Integer.parseInt(token);
            }
        }

        return this;
    }

    int getAnswer(int student, int question) {
        int idx = ArrayUtils.indexOf(questionToIndex, question);
        return answers[student][idx];
    }
    
}
