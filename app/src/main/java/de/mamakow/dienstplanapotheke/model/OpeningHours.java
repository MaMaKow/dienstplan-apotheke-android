package de.mamakow.dienstplanapotheke.model;

import com.google.gson.annotations.SerializedName;

public class OpeningHours {
    @SerializedName("day_opening_start")
    private String start;

    @SerializedName("day_opening_end")
    private String end;

    public OpeningHours() {
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
