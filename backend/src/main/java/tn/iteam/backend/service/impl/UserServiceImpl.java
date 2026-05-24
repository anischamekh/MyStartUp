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
import tn.iteam.backend.dto.CreateUserRequest;
import tn.iteam.backend.dto.EmployeeProfilePayload;
import tn.iteam.backend.dto.UpdateUserRequest;
import tn.iteam.backend.dto.UserResponse;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.TaskStatus;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.EmployeeSkillRepository;
import tn.iteam.backend.repository.EvaluationRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.repository.RoleRepository;
import tn.iteam.backend.repository.TaskRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.TrainingAttendanceRepository;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.UserService;
import tn.iteam.backend.service.email.EmailService;

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
    private final EvaluationRepository evaluationRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;

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
            PayrollRepository payrollRepository,
            EvaluationRepository evaluationRepository,
            TrainingAttendanceRepository trainingAttendanceRepository
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
        this.evaluationRepository = evaluationRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
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
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User creator = currentUserProvider.requireCurrentUser();
        boolean hrFlow = creator.getRole() != null && creator.getRole().getName() == RoleName.HR;

        RoleName roleName = request.getRole() != null ? request.getRole() : RoleName.EMPLOYEE;
        Role role = roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        Team team = null;
        if (roleName != RoleName.ADMIN) {
            Long teamId = request.resolveTeamId();
            if (hrFlow && teamId == null) {
                throw new BusinessException("Please select a team");
            }
            if (teamId != null) {
                team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException("Team not found"));
            }
        }

        String rawPassword = request.getPassword();
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        User saved = userRepository.save(user);

        EmployeeProfile profile = null;
        if (roleName != RoleName.ADMIN) {
            profile = new EmployeeProfile();
            profile.setUser(saved);
            profile.setRemainingLeaveDays(30);
            if (request.getEmployeeProfile() != null) {
                request.getEmployeeProfile().applyTo(profile);
            }
            profile.setTeam(team);
            profile = employeeProfileRepository.save(profile);
            saved.setEmployeeProfile(profile);
        }

        sendWelcomeEmailIfHr(creator, saved, rawPassword);

        return UserResponse.from(saved, profile);
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        User existing = findById(id);
        existing.setFullName(request.getFullName().trim());
        existing.setEmail(request.getEmail().trim());
        existing.setUsername(request.getUsername().trim());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        RoleName roleName = request.getRole();
        Role role = roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
        existing.setRole(role);

        User saved = userRepository.save(existing);

        EmployeeProfile profile = null;
        if (roleName != RoleName.ADMIN) {
            profile = employeeProfileRepository
                    .findByUserId(saved.getId())
                    .orElseGet(() -> {
                        EmployeeProfile ep = new EmployeeProfile();
                        ep.setUser(saved);
                        ep.setRemainingLeaveDays(30);
                        return ep;
                    });

            EmployeeProfilePayload payload = request.getEmployeeProfile();
            if (payload != null) {
                payload.applyTo(profile);
            }

            Long teamId = request.resolveTeamId();
            if (teamId != null) {
                Team team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException("Team not found"));
                profile.setTeam(team);
            }

            profile = employeeProfileRepository.save(profile);
            saved.setEmployeeProfile(profile);
        } else {
            employeeProfileRepository.findByUserId(saved.getId()).ifPresent(employeeProfileRepository::delete);
        }

        return UserResponse.from(saved, profile);
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        Long uid = user.getId();

        if (taskRepository.existsByAssignedTo_IdAndStatusIn(
                uid, List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS))) {
            throw new BusinessException("Cannot delete user because he is assigned to active tasks");
        }

        leaveRequestRepository.clearManagerForUser(uid);
        leaveRequestRepository.deleteByEmployee_Id(uid);
        notificationRepository.deleteByRecipientId(uid);
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

        evaluationRepository.deleteByEmployee_Id(uid);
        evaluationRepository.deleteByEvaluator_Id(uid);
        trainingAttendanceRepository.deleteByUser_Id(uid);

        removeStoredFilesForUser(uid);
        employeeDocumentRepository.deleteAll(employeeDocumentRepository.findByUser_IdOrderByUploadDateDesc(uid));
        employeeSkillRepository.deleteByUser_Id(uid);
        payrollRepository.deleteByUser_Id(uid);

        employeeProfileRepository.findByUserId(uid).ifPresent(employeeProfileRepository::delete);
        userRepository.delete(user);
    }

    private void sendWelcomeEmailIfHr(User creator, User saved, String rawPassword) {
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
}
