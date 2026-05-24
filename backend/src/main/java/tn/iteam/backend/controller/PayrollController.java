package tn.iteam.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Payroll;
import tn.iteam.backend.service.PayrollService;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public List<Payroll> list() {
        return payrollService.findVisible();
    }

    @GetMapping("/user/{userId}")
    public List<Payroll> forUser(@PathVariable Long userId) {
        return payrollService.findForUser(userId);
    }

    @GetMapping("/{id}")
    public Payroll one(@PathVariable Long id) {
        return payrollService.findById(id);
    }

    @PostMapping
    public Payroll create(@RequestBody Payroll payroll) {
        return payrollService.save(payroll);
    }

    @PutMapping("/{id}")
    public Payroll update(@PathVariable Long id, @RequestBody Payroll payroll) {
        return payrollService.update(id, payroll);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        payrollService.delete(id);
        return ResponseEntity.ok().build();
    }
}
