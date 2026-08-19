package com.support.backend.repository;

import com.support.backend.entity.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByMessageIdIn(List<Long> messageIds);
}
