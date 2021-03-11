package com.shablii.timetable.service.model.generator;

import com.shablii.timetable.service.TimetableFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@ExtendWith(MockitoExtension.class)
public class UniversityModelGeneratorInputFileValidationTest {

    @Mock
    private TimetableFacade timetableFacade;

    @InjectMocks
    private UniversityModelGenerator universityModelGenerator;

    @Test
    public void shouldThrowIllegalArgumentExceptionGivenWrongFilePath() {

        String wrongFilePath = "nonexistingFile.txt";
        universityModelGenerator.setCoursesFilePath(wrongFilePath);
        universityModelGenerator.setFirstNamesFilePath(wrongFilePath);
        universityModelGenerator.setLastNamesFilePath(wrongFilePath);

        assertThatIllegalArgumentException().isThrownBy(() -> universityModelGenerator.generateAndSave())
                .withMessage("Unable to locate nonexistingFile.txt");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionGivenWrongNonTxtFile() {

        String wrongFile = "wrong_file.log";
        universityModelGenerator.setCoursesFilePath(wrongFile);
        universityModelGenerator.setFirstNamesFilePath(wrongFile);
        universityModelGenerator.setLastNamesFilePath(wrongFile);

        assertThatIllegalArgumentException().isThrownBy(() -> universityModelGenerator.generateAndSave())
                .withMessage("wrong_file.log is not a *.txt file");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionGivenEmptyFile() {

        String emptyFile = "empty.txt";
        universityModelGenerator.setCoursesFilePath(emptyFile);
        universityModelGenerator.setFirstNamesFilePath(emptyFile);
        universityModelGenerator.setLastNamesFilePath(emptyFile);

        assertThatIllegalArgumentException().isThrownBy(() -> universityModelGenerator.generateAndSave())
                .withMessage("empty.txt appears to be empty");
    }

}
