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
import tn.iteam.backend.entity.Evaluation;
import tn.iteam.backend.service.EvaluationService;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public List<Evaluation> list() {
        return evaluationService.findVisible();
    }

    @GetMapping("/{id}")
    public Evaluation one(@PathVariable Long id) {
        return evaluationService.findById(id);
    }

    @PostMapping
    public Evaluation create(@RequestBody Evaluation evaluation) {
        return evaluationService.create(evaluation);
    }

    @PutMapping("/{id}")
    public Evaluation update(@PathVariable Long id, @RequestBody Evaluation evaluation) {
        return evaluationService.update(id, evaluation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        evaluationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
