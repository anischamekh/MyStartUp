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
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.service.TeamService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<Team> all() {
        return teamService.findAll();
    }

    @GetMapping("/{id}")
    public Team one(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @PostMapping
    public Team create(@RequestBody Team team) {
        return teamService.create(team);
    }

    @PutMapping("/{id}")
    public Team update(@PathVariable Long id, @RequestBody Team team) {
        return teamService.update(id, team);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.ok().build();
    }
}

