package subway.section.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import subway.station.domain.Station;

public final class Sections {

    private static final int INITIALIZE_SECTION_COUNT = 1;

    private final List<Section> sections;

    private Sections(final List<Section> sections) {
        this.sections = sections;
    }

    public static Sections empty() {
        return new Sections(new ArrayList<>());
    }

    public static Sections values(final Section... sections) {
        return new Sections(Arrays.asList(sections));
    }

    public static Sections values(final List<Section> sections) {
        return new Sections(sections);
    }

    public void initializeSections(final String upStationName, final String downStationName, final int distance) {
        if (!sections.isEmpty()) {
            throw new IllegalArgumentException("이미 역들이 존재하는 노선입니다.");
        }
        final Station upStation = Station.get(upStationName);
        final Station downStation = Station.get(downStationName);
        sections.add(Section.of(upStation, downStation, distance));
    }

    public void addSection(final String upStationName, final String downStationName, final int distance) {
        if (sections.size() < INITIALIZE_SECTION_COUNT) {
            throw new IllegalArgumentException("노선에는 한 구간이라도 존재해야 역이 추가 가능합니다.");
        }
        final Station upStation = Station.get(upStationName);
        final Station downStation = Station.get(downStationName);
        validateSection(upStation, downStation);
        addSection(upStation, downStation, distance);
    }

    private void validateSection(final Station upStation, final Station downStation) {
        final boolean isUpStationExist = containsStation(upStation);
        final boolean isDownStationExist = containsStation(downStation);
        if (isUpStationExist && isDownStationExist) {
            throw new IllegalArgumentException("이미 존재하는 역들입니다.");
        }
        if (!isUpStationExist && !isDownStationExist) {
            throw new IllegalArgumentException("연결되어 있는 역이 존재하지 않습니다.");
        }
    }

    private void addSection(final Station upStation, final Station downStation, final int weight) {
        final Section newSection = Section.of(upStation, downStation, weight);

        if (isLineExtend(upStation, downStation)) {
            extendLine(upStation, downStation, newSection);
            return;
        }

        final Optional<Section> sectionStartWithSameUpStation = findSectionStartWith(upStation);
        final Optional<Section> sectionEndWithSameDownStation = findSectionEndWith(downStation);

        sectionStartWithSameUpStation.ifPresent(section -> divideSection(newSection, section));
        sectionEndWithSameDownStation.ifPresent(section -> divideSection(newSection, section));
    }

    private boolean isLineExtend(final Station upStation, final Station downStation) {
        return isEndOfLine(upStation) || isStartOfLine(downStation);
    }

    private void extendLine(final Station upStation, final Station downStation, final Section newSection) {
        if (isEndOfLine(upStation)) {
            sections.add(newSection);
            return;
        }
        if (isStartOfLine(downStation)) {
            sections.add(0, newSection);
        }
    }

    private boolean isEndOfLine(final Station upStation) {
        return sections.get(sections.size() - 1).isEndWith(upStation);
    }

    private boolean isStartOfLine(final Station downStation) {
        return sections.get(0).isStartWith(downStation);
    }


    private Optional<Section> findSectionStartWith(final Station station) {
        return sections.stream()
            .filter(section -> section.isStartWith(station))
            .findFirst();
    }

    private Optional<Section> findSectionEndWith(final Station station) {
        return sections.stream()
            .filter(section -> section.isEndWith(station))
            .findFirst();
    }

    private void divideSection(final Section newSection, final Section target) {
        if (target.isStartWith(newSection.getUp())) {
            putNewSectionUpOfOther(newSection, target);
            return;
        }
        putNewSectionDownOfOther(newSection, target);
    }

    private void putNewSectionUpOfOther(final Section newSection, final Section target) {
        final int remainDistance = target.getDistance() - newSection.getDistance();
        final int targetIndex = sections.indexOf(target);
        final Section rightSection = Section.of(newSection.getDown(), target.getDown(), remainDistance);
        sections.add(targetIndex, rightSection);
        sections.add(targetIndex, newSection);
        sections.remove(target);
    }

    private void putNewSectionDownOfOther(final Section newSection, final Section target) {
        final int remainDistance = target.getDistance() - newSection.getDistance();
        final int targetIndex = sections.indexOf(target);
        final Section leftSection = Section.of(target.getUp(), newSection.getUp(), remainDistance);
        sections.add(targetIndex, newSection);
        sections.add(targetIndex, leftSection);
        sections.remove(target);
    }

    private boolean containsStation(final Station station) {
        return sections.stream()
            .anyMatch(section -> section.isContainStation(station));
    }

    public void removeStation(final String name) {
        final Station station = Station.get(name);
        final List<Section> containsSections = sections.stream()
            .filter(section -> section.isContainStation(station))
            .collect(Collectors.toList());
        combineSection(containsSections, station);
    }

    private void combineSection(final List<Section> containsSections, final Station station) {
        if (containsSections.size() == 1) {
            sections.remove(containsSections.get(0));
            return;
        }

        final Section sectionEndWith = findSectionEndWith(containsSections, station);
        final Section sectionStartWith = findSectionStartWith(containsSections, station);

        final Section combinedSection = Section.of(
            sectionEndWith.getUp(),
            sectionStartWith.getDown(),
            sectionStartWith.getDistance() + sectionEndWith.getDistance()
        );

        sections.add(containsSections.indexOf(sectionEndWith), combinedSection);
        sections.remove(sectionStartWith);
        sections.remove(sectionEndWith);
    }

    private Section findSectionEndWith(final List<Section> sections, final Station station) {
        return sections.stream()
            .filter(section -> section.isEndWith(station))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 역이 도착지인 구간이 존재하지 않습니다."));
    }

    private Section findSectionStartWith(final List<Section> sections, final Station station) {
        return sections.stream()
            .filter(section -> section.isStartWith(station))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 역이 도착지인 구간이 존재하지 않습니다."));
    }

    public List<Section> getSections() {
        return sections;
    }
}