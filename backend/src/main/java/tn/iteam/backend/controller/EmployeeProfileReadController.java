package tn.iteam.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.service.impl.CurrentUserProvider;

@RestController
@RequestMapping("/api/employee-profiles")
public class EmployeeProfileReadController {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeProfileReadController(
            EmployeeProfileRepository employeeProfileRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.employeeProfileRepository = employeeProfileRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /** HR / ADMIN: merge with {@code GET /api/users} when user JSON omits nested profile (cycle-safe). */
    @GetMapping
    public List<EmployeeProfile> all() {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.ADMIN) {
            throw new BusinessException("Not allowed to list employee profiles");
        }
        return employeeProfileRepository.findAll();
    }
}
