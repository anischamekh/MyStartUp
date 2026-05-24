package tn.iteam.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.EmployeeSkill;
import tn.iteam.backend.service.EmployeeSkillService;

@RestController
@RequestMapping("/api/employee-skills")
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    public EmployeeSkillController(EmployeeSkillService employeeSkillService) {
        this.employeeSkillService = employeeSkillService;
    }

    @GetMapping
    public List<EmployeeSkill> list() {
        return employeeSkillService.findVisible();
    }

    @GetMapping("/user/{userId}")
    public List<EmployeeSkill> forUser(@PathVariable Long userId) {
        return employeeSkillService.findForUser(userId);
    }

    @PostMapping
    public EmployeeSkill upsert(@RequestBody EmployeeSkill body) {
        return employeeSkillService.upsert(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        employeeSkillService.delete(id);
        return ResponseEntity.ok().build();
    }
}
