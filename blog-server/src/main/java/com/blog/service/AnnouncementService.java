package com.blog.service;

import com.blog.domain.entity.Announcement;
import java.util.List;

public interface AnnouncementService {

    List<Announcement> getAnnouncementList();

    List<Announcement> getPublishedAnnouncements();

    Announcement getAnnouncementById(Long id);

    void saveOrUpdateAnnouncement(Announcement announcement);

    void deleteAnnouncement(Long id);

    void publishAnnouncement(Long id);
}
