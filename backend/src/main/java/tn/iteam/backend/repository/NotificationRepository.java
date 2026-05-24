package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    void deleteByRecipient_Id(Long recipientId);
}

