package com.saas.professor.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.entity.Teacher;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updateName(Long teacherId, String newName) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado"));
        teacher.setName(newName);
        teacherRepository.save(teacher);
    }

    @Transactional
    public void updatePassword(Long teacherId, String currentPassword, String newPassword) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado"));

        if (!passwordEncoder.matches(currentPassword, teacher.getPassword()))
            throw new BusinessException("Senha atual incorreta.");

        teacher.setPassword(passwordEncoder.encode(newPassword));
        teacherRepository.save(teacher);
    }
}