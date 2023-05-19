package subway.domain;

import java.util.Objects;

public class Line {

    private Long id;
    private String name;
    private String color;
    private Integer additionalFee;

    Line() {
    }

    public Line(Long id, String name, String color, Integer additionalFee) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.additionalFee = additionalFee;
    }

    public Line(String name, String color, Integer additionalFee) {
        this(null, name, color, additionalFee);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Integer getAdditionalFee() {
        return additionalFee;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Line line = (Line) o;
        if (Objects.isNull(id) || Objects.isNull(line.id)) {
            return false;
        }
        return Objects.equals(id, line.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
