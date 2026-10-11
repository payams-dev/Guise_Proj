package judgment;

import groove.GrooveCalculator;
import java.util.*;

public final class JudgmentEvaluator {
    public static class Desire {
        public String id,name;
        public double identityWeight,cooperationWeight,adaptationWeight,fitThreshold,performanceThreshold;
    }
    public static final class Evaluation {
        public final double identity,cooperation,adaptation,fit,performance;
        public final boolean passed;
        public final List<String> hints=new ArrayList<>();
        Evaluation(Desire d,double i,double c,double a,double performance){
            this.identity=i;this.cooperation=c;this.adaptation=a;this.performance=performance;
            fit=d.identityWeight*i+d.cooperationWeight*c+d.adaptationWeight*a;
            passed=fit>=d.fitThreshold&&performance>=d.performanceThreshold;
            if(i<.5)hints.add("IDENTITY_UNCLEAR");
            if(c<.5)hints.add("COORDINATION_MISSING");
            if(performance<d.performanceThreshold)hints.add("PERFORMANCE_DISRUPTED");
            if(hints.isEmpty()&&!passed)hints.add("INTERPRETATION_INCOMPLETE");
        }
    }
    public Evaluation evaluate(Desire desire,GrooveCalculator groove){
        return evaluate(desire,groove.brassShare(),Math.min(1,groove.completedArrangements()/2.0),Math.min(1,.5*(groove.recoveryCount()+groove.adaptationCount())),groove.score()-groove.preparation);
    }
    public Evaluation evaluate(Desire d,double i,double c,double a,double performance){
        if(d==null||!Double.isFinite(i+c+a+performance)||i<0||i>1||c<0||c>1||a<0||a>1)throw new IllegalArgumentException("Invalid Judgment evidence");
        return new Evaluation(d,i,c,a,performance);
    }
}
