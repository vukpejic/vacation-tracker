package com.rbt.vacationtracker.service;

import com.rbt.vacationtracker.entity.EmployeeEntity;
import com.rbt.vacationtracker.entity.EmployeeVacationDaysSpent;
import com.rbt.vacationtracker.entity.VacationEntity;
import com.rbt.vacationtracker.model.Employee;
import com.rbt.vacationtracker.model.EmployeeVacation;
import com.rbt.vacationtracker.repository.EmployeeRepository;
import com.rbt.vacationtracker.repository.VacationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {
   private final EmployeeRepository employeeRepository;
   private final VacationRepository vacationRepository;

   public void createEmployee(Employee employee) {
        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .email(employee.getEmail())
                .password(employee.getPassword())
                .build();

        employeeRepository.save(employeeEntity);
        log.info("Employee: {} saved in database", employee.getEmail());
    }
    public void addVacationDays(Integer year, EmployeeVacation employeeVacation) {
       EmployeeEntity employeeEntity = employeeRepository.findByEmail(employeeVacation.getEmail())
               .orElseThrow(() -> new NoSuchElementException());

        VacationEntity vacationEntity = VacationEntity.builder()
                        .days(employeeVacation.getDays())
                        .usedDays(0)
                        .freeDays(employeeVacation.getDays())
                        .year(year)
                        .employee(employeeEntity)
                        .build();
        vacationRepository.save(vacationEntity);
        employeeEntity.getVacations().add(vacationEntity);
        log.info("Added {} days for user {}", employeeVacation.getDays(), employeeVacation.getEmail());
    }

    public void usedVacationDaysManagement(EmployeeVacationDaysSpent employeeVacationDaysSpent) {
        EmployeeEntity employeeEntity = employeeRepository.findByEmail(employeeVacationDaysSpent.getEmail())
                .orElseThrow(() -> new NoSuchElementException());

        List<VacationEntity> vacations = employeeEntity.getVacations();
        VacationEntity vacationEntity = getVacationWithGivenYear(employeeVacationDaysSpent.getYear(), vacations);

        Long diffInMillies = Math.abs(employeeVacationDaysSpent.getEndDate().getTime() - employeeVacationDaysSpent.getStartDate().getTime());
        Long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        assert vacationEntity != null;
        vacationEntity.setFreeDays(vacationEntity.getDays() - diff.intValue());
        vacationEntity.setUsedDays(vacationEntity.getUsedDays() + diff.intValue());
        vacationRepository.save(vacationEntity);
    }

    private VacationEntity getVacationWithGivenYear(Integer year, List<VacationEntity> vacationEntities) {
       for (VacationEntity v: vacationEntities) {
           if (Objects.equals(v.getYear(), year)) {
               return v;
           }
       }
       return null;
    }

}
