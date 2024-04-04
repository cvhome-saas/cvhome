package com.asrevo.cvhome.store.core.model.references;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SizeReferences {

    private List<WeightUnit> weights;
    private List<MeasureUnit> measures;

}
