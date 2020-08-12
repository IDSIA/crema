package ch.idsia.crema.model.io.uai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UAI {

    public enum UAITypes {

        BAYES("BAYES"),
        EVID("EVID"),
        HCREDAL("H-CREDAL"),
        VCREDAL("V-CREDAL");

        private  static List<String> labels;

        private static final Map<String, UAITypes> BY_LABEL = new HashMap<>();

        static {
            for (UAITypes t : values()) {
                BY_LABEL.put(t.label, t);
            }
        }
        public final String label;

        private UAITypes(String label) {
            this.label = label;
        }
        public String toString() {
            return this.label;
        }

        public static UAITypes valueOfLabel(String label) {
            if (!BY_LABEL.containsKey(label))
                throw new java.lang.IllegalArgumentException("No enum constant "+label);
            return BY_LABEL.get(label);
        }


    }



}
