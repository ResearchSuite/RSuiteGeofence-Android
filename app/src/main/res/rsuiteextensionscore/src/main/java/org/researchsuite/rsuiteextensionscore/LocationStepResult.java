package org.researchsuite.rsuiteextensionscore;

/**
 * Created by Christina on 6/22/17.
 */

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;

public class LocationStepResult extends StepResult {

    private Double longitute;
    private Double latitude;
    private String userInput;

    public LocationStepResult(Step step) {
        super(step);

    }

    public void setLongLat(Double longitute,Double latitude){
        this.longitute = longitute;
        this.latitude = latitude;
    }

    public void setUserInput(String userInput){
        this.userInput = userInput;
    }



}
