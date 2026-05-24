package tn.iteam.backend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.repository.RoleRepository;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.email.EmailService;
import tn.iteam.backend.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CurrentUserProvider currentUserProvider;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final PayrollRepository payrollRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeProfileRepository employeeProfileRepository,
            TeamRepository teamRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            CurrentUserProvider currentUserProvider,
            LeaveRequestRepository leaveRequestRepository,
            NotificationRepository notificationRepository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            EmployeeDocumentRepository employeeDocumentRepository,
            EmployeeSkillRepository employeeSkillRepository,
            PayrollRepository payrollRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.currentUserProvider = currentUserProvider;
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.employeeDocumentRepository = employeeDocumentRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.payrollRepository = payrollRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException("User not found"));
    }

    @Override
    public User create(User user) {
        String rawPassword = user.getPassword();
        if (user.getId() != null) {
            user.setId(null);
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User creator = currentUserProvider.requireCurrentUser();
        boolean hrFlow = creator.getRole() != null && creator.getRole().getName() == RoleName.HR;

        if (user.getRole() == null || user.getRole().getName() == null) {
            Role role = roleRepository
                    .findByName(RoleName.EMPLOYEE)
                    .orElseThrow(() -> new BusinessException("Role EMPLOYEE not found"));
            user.setRole(role);
        } else {
            RoleName name = user.getRole().getName();
            Role role = roleRepository
                    .findByName(name)
                    .orElseThrow(() -> new BusinessException("Role not found: " + name));
            user.setRole(role);
        }

        Team requiredTeam = null;
        if (hrFlow && user.getRole().getName() != RoleName.ADMIN) {
            requiredTeam = resolveRequiredTeamForHr(user);
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);

        if (saved.getRole() != null && saved.getRole().getName() != RoleName.ADMIN) {
            EmployeeProfile profile = new EmployeeProfile();
            profile.setUser(saved);
            profile.setRemainingLeaveDays(30);
            copyProfileFieldsFromRequest(profile, user.getEmployeeProfile());
            if (hrFlow) {
                profile.setTeam(requiredTeam);
            } else if (user.getEmployeeProfile() != null && user.getEmployeeProfile().getTeam() != null) {
                Long tid = user.getEmployeeProfile().getTeam().getId();
                if (tid != null) {
                    Team t = teamRepository.findById(tid).orElseThrow(() -> new BusinessException("Team not found"));
                    profile.setTeam(t);
                }
            }
            employeeProfileRepository.save(profile);
        }

        try {
            if (creator.getRole() != null && creator.getRole().getName() == RoleName.HR) {
                String subject = "Welcome to the Company";
                String body = ""
                        + "Hello,\n\n"
                        + "Your account has been created successfully.\n\n"
                        + "Username: " + saved.getUsername() + "\n"
                        + "Password: " + rawPassword + "\n\n"
                        + "You can login here:\n"
                        + "http://localhost:4200/login\n\n"
                        + "Welcome to the team!\n";

                emailService.sendEmail(saved.getEmail(), subject, body);
            }
        } catch (Exception e) {
            log.error("Welcome email failed for createdUserId={} createdUserEmail={}", saved.getId(), saved.getEmail(), e);
        }

        return saved;
    }

    @Override
    public User update(Long id, User user) {
        User existing = findById(id);
        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setUsername(user.getUsername());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getRole() != null && user.getRole().getName() != null) {
            Role role = roleRepository
                    .findByName(user.getRole().getName())
                    .orElseThrow(() -> new BusinessException("Role not found: " + user.getRole().getName()));
            existing.setRole(role);
        }

        User saved = userRepository.save(existing);

        if (saved.getRole() != null && saved.getRole().getName() != RoleName.ADMIN) {
            EmployeeProfile profile = employeeProfileRepository
                    .findByUserId(saved.getId())
                    .orElseGet(() -> {
                        EmployeeProfile ep = new EmployeeProfile();
                        ep.setUser(saved);
                        ep.setRemainingLeaveDays(30);
                        return ep;
                    });
            copyProfileFieldsFromRequest(profile, user.getEmployeeProfile());
            if (user.getEmployeeProfile() != null && user.getEmployeeProfile().getTeam() != null) {
                Long tid = user.getEmployeeProfile().getTeam().getId();
                if (tid != null) {
                    Team t = teamRepository.findById(tid).orElseThrow(() -> new BusinessException("Team not found"));
                    profile.setTeam(t);
                }
            }
            employeeProfileRepository.save(profile);
        }

        return saved;
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        Long uid = user.getId();

        leaveRequestRepository.clearManagerForUser(uid);
        leaveRequestRepository.deleteByEmployee_Id(uid);
        notificationRepository.deleteByRecipient_Id(uid);
        taskRepository.clearAssignedToForUser(uid);
        taskRepository.clearCreatedByForUser(uid);

        for (Team t : teamRepository.findAllByTeamLeader_Id(uid)) {
            t.setTeamLeader(null);
            teamRepository.save(t);
        }
        for (Project p : projectRepository.findByManager_Id(uid)) {
            p.setManager(null);
            projectRepository.save(p);
        }

        removeStoredFilesForUser(uid);
        employeeDocumentRepository.deleteAll(employeeDocumentRepository.findByUser_IdOrderByUploadDateDesc(uid));
        employeeSkillRepository.deleteByUser_Id(uid);
        payrollRepository.deleteByUser_Id(uid);

        employeeProfileRepository.findByUserId(uid).ifPresent(employeeProfileRepository::delete);
        userRepository.delete(user);
    }

    private void removeStoredFilesForUser(Long userId) {
        List<EmployeeDocument> docs = employeeDocumentRepository.findByUser_IdOrderByUploadDateDesc(userId);
        for (EmployeeDocument d : docs) {
            if (d.getFilePath() == null) {
                continue;
            }
            try {
                Path p = Paths.get(d.getFilePath());
                Files.deleteIfExists(p);
            } catch (IOException e) {
                log.warn("Could not delete file for documentId={} path={}", d.getId(), d.getFilePath(), e);
            }
        }
    }

    private Team resolveRequiredTeamForHr(User user) {
        if (user.getEmployeeProfile() == null
                || user.getEmployeeProfile().getTeam() == null
                || user.getEmployeeProfile().getTeam().getId() == null) {
            throw new BusinessException("Team selection is required when HR creates a user");
        }
        Long teamId = user.getEmployeeProfile().getTeam().getId();
        return teamRepository.findById(teamId).orElseThrow(() -> new BusinessException("Team not found"));
    }

    private void copyProfileFieldsFromRequest(EmployeeProfile target, EmployeeProfile source) {
        if (source == null) {
            return;
        }
        if (source.getPhone() != null) {
            target.setPhone(source.getPhone());
        }
        if (source.getAddress() != null) {
            target.setAddress(source.getAddress());
        }
        if (source.getSpeciality() != null) {
            target.setSpeciality(source.getSpeciality());
        }
        if (source.getHireDate() != null) {
            target.setHireDate(source.getHireDate());
        }
        if (source.getExperienceLevel() != null) {
            target.setExperienceLevel(source.getExperienceLevel());
        }
        if (source.getJobTitle() != null) {
            target.setJobTitle(source.getJobTitle());
        }
        if (source.getRemainingLeaveDays() != null) {
            target.setRemainingLeaveDays(source.getRemainingLeaveDays());
        }
        if (source.getSalary() != null) {
            target.setSalary(source.getSalary());
        }
    }
}
