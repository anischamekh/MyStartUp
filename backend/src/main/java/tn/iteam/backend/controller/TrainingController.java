package tn.iteam.backend.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Training;
import tn.iteam.backend.entity.TrainingAttendance;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.service.TrainingService;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public List<Training> all() {
        return trainingService.findAll();
    }

    @GetMapping("/{id}")
    public Training one(@PathVariable Long id) {
        return trainingService.findById(id);
    }

    @PostMapping
    public Training create(@RequestBody Training training) {
        return trainingService.save(training);
    }

    @PutMapping("/{id}")
    public Training update(@PathVariable Long id, @RequestBody Training training) {
        return trainingService.update(id, training);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        trainingService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/attendance")
    public List<TrainingAttendance> attendance(@PathVariable Long id) {
        return trainingService.listAttendance(id);
    }

    @PostMapping("/{id}/attendance")
    public TrainingAttendance addAttendance(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body == null ? null : body.get("userId");
        if (userId == null) {
            throw new BusinessException("userId is required");
        }
        return trainingService.addAttendance(id, userId);
    }

    @PatchMapping("/attendance/{attendanceId}")
    public TrainingAttendance patchAttended(@PathVariable Long attendanceId, @RequestBody Map<String, Boolean> body) {
        if (body == null || !body.containsKey("attended")) {
            throw new BusinessException("attended is required");
        }
        return trainingService.setAttended(attendanceId, Boolean.TRUE.equals(body.get("attended")));
    }
}
