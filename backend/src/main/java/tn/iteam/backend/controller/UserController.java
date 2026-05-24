package tn.iteam.backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.SecurityUtil;
import tn.iteam.backend.service.UserService;
import tn.iteam.backend.service.impl.CurrentUserProvider;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TeamRepository teamRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public UserController(
            UserService userService,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            TeamRepository teamRepository,
            EmployeeProfileRepository employeeProfileRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.teamRepository = teamRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    @GetMapping
    public List<User> all() {
        return userService.findAll();
    }

    /** Team leaders: members of the team they lead (for task assignment UI). */
    @GetMapping("/team-members")
    public List<User> teamMembers() {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.TEAM_LEADER) {
            throw new BusinessException("Only TEAM_LEADER can load team members");
        }
        Team team = teamRepository
                .findByTeamLeader_Id(me.getId())
                .orElseThrow(() -> new BusinessException("You are not assigned as leader of any team"));
        return employeeProfileRepository.findByTeam_Id(team.getId()).stream()
                .map(EmployeeProfile::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    public User me() {
        String username = SecurityUtil.currentUsername();
        return userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("Current user not found"));
    }

    @GetMapping("/{id}")
    public User one(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}

