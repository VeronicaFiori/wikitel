package it.cnr.psts.wikitel.API;

import lombok.Data;

import java.util.List;
@Data
public class Timeline {

    private float horizon;

    private List<TimelineValue> value;

    public Timeline(List<TimelineValue> value) {
        this.value = value;
    }
}
