package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.ScheduleTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleTemplateService {

    private final ScheduleTemplateDao repository;

    public long count() {

        log.debug("Fetching template count from repository");
        return repository.count();
    }

    public ScheduleTemplate save(ScheduleTemplate template) {

        log.debug("Saving template {}", template);
        return repository.save(template);
    }

    public List<ScheduleTemplate> saveAll(List<ScheduleTemplate> templates) {

        if (templates.isEmpty()) {
            log.debug("Received empty list, not saving");
            return templates;
        }

        log.debug("Saving templates to repository");
        return repository.saveAll(templates);
    }

    public List<ScheduleTemplate> findAll() {

        log.debug("Fetching templates from repository");
        return repository.findAll();
    }

    public List<ScheduleTemplate> findAllForWeek(boolean weekParity) {

        log.debug("Fetching week templates for week parity {}", weekParity);
        return repository.findAllByWeek(weekParity);
    }

    public List<ScheduleTemplate> findAllForDay(boolean weekParity,
            DayOfWeek day) {

        log.debug("Fetching {} templates for week parity {}", day, weekParity);
        return repository.findAllByDay(weekParity, day);
    }

    public Optional<ScheduleTemplate> findById(long id) {

        log.debug("Fetching template ID{} from repository", id);
        return repository.findById(id);
    }

}
