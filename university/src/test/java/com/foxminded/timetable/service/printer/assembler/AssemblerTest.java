package com.foxminded.timetable.service.printer.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AssemblerTest {

    private String titleOne = "Title1";
    private String titleTwo = "Title2";
    private String titleThree = "Title3";
    private String cellOne = "cell1";
    private String cellTwo = "cell2";
    private String cellThree = "cell three";
    private List<ColumnWriter> columns;
    private Assembler assembler;

    @Mock
    private ColumnWriter mockColumn;

    @BeforeEach
    private void setUp() {
        assembler = new Assembler();
        columns = new ArrayList<>();
    }

    @Test
    public void shouldAssembleSingleColumnTable() {

        List<String> items = Arrays.asList(cellOne, cellTwo, cellThree);
        columns.add(new ColumnWriter(titleOne, items));
        String expected = "+------------+\n" + "| Title1     |\n"
                + "|============|\n" + "| cell1      |\n" + "| cell2      |\n"
                + "| cell three |\n" + "+------------+\n";

        String actual = assembler.assembleTable(columns);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldAssembleTwoColumnTable() {

        List<String> itemsOne = Arrays.asList(cellOne, cellTwo);
        List<String> itemsTwo = Arrays.asList(cellOne, cellThree);
        columns.add(new ColumnWriter(titleOne, itemsOne));
        columns.add(new ColumnWriter(titleTwo, itemsTwo));
        String expected = "+--------+------------+\n"
                + "| Title1 | Title2     |\n" + "|========|============|\n"
                + "| cell1  | cell1      |\n" + "| cell2  | cell three |\n"
                + "+--------+------------+\n";

        String actual = assembler.assembleTable(columns);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldAssembleThreeColumnTable() {

        List<String> itemsOne = Arrays.asList(cellOne);
        List<String> itemsTwo = Arrays.asList(cellTwo);
        List<String> itemsThree = Arrays.asList(cellThree);
        columns.add(new ColumnWriter(titleOne, itemsOne));
        columns.add(new ColumnWriter(titleTwo, itemsTwo));
        columns.add(new ColumnWriter(titleThree, itemsThree));
        String expected = "+--------+--------+------------+\n"
                + "| Title1 | Title2 | Title3     |\n"
                + "|========|========|============|\n"
                + "| cell1  | cell2  | cell three |\n"
                + "+--------+--------+------------+\n";

        String actual = assembler.assembleTable(columns);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCallOnColumnWriterToFillTableWithData() {

        List<String> items = Arrays.asList(cellOne);
        given(mockColumn.getTitle()).willReturn(titleOne);
        given(mockColumn.getItems()).willReturn(items);
        given(mockColumn.getWidth()).willReturn(6);
        columns.add(mockColumn);
        String expected = "+--------+\n" + "| Title1 |\n" + "|========|\n"
                + "| cell1  |\n" + "+--------+\n";

        String actual = assembler.assembleTable(columns);

        assertThat(actual).isEqualTo(expected);
        then(mockColumn).should(atLeastOnce()).getTitle();
        then(mockColumn).should(atLeastOnce()).getItems();
        then(mockColumn).should(atLeastOnce()).getWidth();
    }

    @Test
    public void shouldStopWritingTableOnceShorterColumnIsExhausted() {

        List<String> itemsOne = Arrays.asList(cellOne, cellTwo, cellThree,
                cellOne, cellTwo);
        List<String> itemsTwo = Arrays.asList(cellOne, cellThree);
        columns.add(new ColumnWriter(titleOne, itemsOne));
        columns.add(new ColumnWriter(titleTwo, itemsTwo));
        String expected = "+------------+------------+\n"
                + "| Title1     | Title2     |\n"
                + "|============|============|\n"
                + "| cell1      | cell1      |\n"
                + "| cell2      | cell three |\n"
                + "+------------+------------+\n";

        String actual = assembler.assembleTable(columns);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldWriteEmptyTableGivenColumnsWithMissingStrings() {

        String empty = "";
        List<String> itemsOne = Arrays.asList(empty, empty);
        List<String> itemsTwo = Arrays.asList(empty, cellThree);
        columns.add(new ColumnWriter(empty, itemsOne));
        columns.add(new ColumnWriter(titleTwo, itemsTwo));
        String expected = "+---+------------+\n" + "|   | Title2     |\n"
                + "|===|============|\n" + "|   |            |\n"
                + "|   | cell three |\n" + "+---+------------+\n";

        String actual = assembler.assembleTable(columns);

        assertThat(actual).isEqualTo(expected);
    }
}
