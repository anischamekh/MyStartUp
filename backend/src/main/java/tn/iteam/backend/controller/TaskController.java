package tn.iteam.backend.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> all() {
        return taskService.findAll();
    }

    @GetMapping("/mine")
    public List<Task> mine() {
        return taskService.findMyTasks();
    }

    @GetMapping("/{id}")
    public Task one(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    public Task create(@RequestBody Task task) {
        return taskService.create(task);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @RequestBody Task task) {
        return taskService.update(id, task);
    }

    @PutMapping("/{id}/progress")
    public Task updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int progress = body.get("progress") == null ? 0 : Integer.parseInt(body.get("progress").toString());
        return taskService.updateProgress(id, progress);
    }

    @PutMapping("/{id}/validate")
    public Task validate(@PathVariable Long id) {
        return taskService.validate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.ok().build();
    }
}

