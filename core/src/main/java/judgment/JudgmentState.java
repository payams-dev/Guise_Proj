package judgment;

import java.util.*;

/** In-memory attempts only. Reload/anti-reload policy intentionally not invented. */
public final class JudgmentState {
    public final String playthroughId=UUID.randomUUID().toString();
    public final String desireId;
    private final Map<String,JudgmentEvaluator.Evaluation> submissions=new LinkedHashMap<>();
    public boolean success,gameOver;
    public JudgmentState(String desireId){this.desireId=desireId;}
    public int attemptsUsed(){return submissions.size();}
    public JudgmentEvaluator.Evaluation commit(String id,JudgmentEvaluator.Evaluation evaluation){
        if(id==null||id.isBlank()||evaluation==null)throw new IllegalArgumentException("Invalid submission");
        if(submissions.containsKey(id))return submissions.get(id);
        if(success||gameOver||attemptsUsed()>=3)throw new IllegalStateException("Judgment sequence is terminal");
        submissions.put(id,evaluation);success=evaluation.passed;gameOver=!success&&attemptsUsed()==3;return evaluation;
    }
    public String nextName(){return new String[]{"The Audition","The Rebuttal","The Final Performance"}[Math.min(2,attemptsUsed())];}
    public String cutsceneHook(){return gameOver?"judgment_third_failure":success?"judgment_test_success":null;}
}
