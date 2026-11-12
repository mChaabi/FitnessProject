package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByActiveTrueOrderByCreatedAtDesc();
}