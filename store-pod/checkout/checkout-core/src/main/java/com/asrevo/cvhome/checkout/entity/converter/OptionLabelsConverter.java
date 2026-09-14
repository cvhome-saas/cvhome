package com.asrevo.cvhome.checkout.entity.converter;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.checkout.entity.OptionLabel;

/**
 * A cart line's option labels in one text column: labels separated by the ASCII record separator, an option from its
 * value by the unit separator. Neither character can appear in a name a merchant types, so no escaping is needed and
 * no JSON library either. An empty list is null.
 */
@Converter
public class OptionLabelsConverter implements AttributeConverter<List<OptionLabel>, String> {

    static final char RECORD = '\u001E';

    static final char UNIT = '\u001F';

    @Override
    public String convertToDatabaseColumn(List<OptionLabel> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (OptionLabel label : labels) {
            if (!text.isEmpty()) {
                text.append(RECORD);
            }
            text.append(label.option() == null ? "" : label.option()).append(UNIT)
                    .append(label.value() == null ? "" : label.value());
        }
        return text.toString();
    }

    @Override
    public List<OptionLabel> convertToEntityAttribute(String text) {
        List<OptionLabel> labels = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return labels;
        }
        for (String record : text.split(String.valueOf(RECORD), -1)) {
            int at = record.indexOf(UNIT);
            labels.add(at < 0 ? new OptionLabel(record, "") : new OptionLabel(record.substring(0, at),
                    record.substring(at + 1)));
        }
        return labels;
    }
}
