package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.service.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        if (template.getId() == null) {
            log.debug("Adding new template {}", template);
            return repository.save(template);
        }
        log.debug("Updating template {}", template);
        return repository.update(template);

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

    public ScheduleTemplate findById(long id) throws ServiceException {

        log.debug("Fetching template ID{} from repository", id);
        Optional<ScheduleTemplate> optionalScheduleTemplate =
                repository.findById(
                id);
        if (!optionalScheduleTemplate.isPresent()) {
            log.error("Template with ID{} could not be found", id);
            throw new ServiceException(
                    "Template with ID" + id + " could not be found");
        }

        return optionalScheduleTemplate.get();
    }

}
