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
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.service.LeaveRequestService;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    public List<LeaveRequest> all() {
        return leaveRequestService.findAll();
    }

    @GetMapping("/mine")
    public List<LeaveRequest> mine() {
        return leaveRequestService.findMine();
    }

    @GetMapping("/user/{userId}")
    public List<LeaveRequest> forUser(@PathVariable Long userId) {
        return leaveRequestService.findForUser(userId);
    }

    @GetMapping("/{id}")
    public LeaveRequest one(@PathVariable Long id) {
        return leaveRequestService.findById(id);
    }

    @PostMapping
    public LeaveRequest request(@RequestBody LeaveRequest leaveRequest) {
        return leaveRequestService.request(leaveRequest);
    }

    @PostMapping("/{id}/approve")
    public LeaveRequest approve(@PathVariable Long id) {
        return leaveRequestService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public LeaveRequest reject(@PathVariable Long id) {
        return leaveRequestService.reject(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        leaveRequestService.delete(id);
        return ResponseEntity.ok().build();
    }
}

