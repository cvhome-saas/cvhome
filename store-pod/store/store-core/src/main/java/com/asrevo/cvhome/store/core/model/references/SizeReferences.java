package com.asrevo.cvhome.store.core.model.references;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SizeReferences {

    private List<WeightUnit> weights;
    private List<MeasureUnit> measures;
}
