package com.saas.professor.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saas.professor.entity.Teacher;
import com.saas.professor.repository.TeacherRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final TeacherRepository teacherRepository;
	
	public UserDetailsServiceImpl(TeacherRepository teacherRepository) {
		this.teacherRepository = teacherRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	    Teacher teacher = teacherRepository.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("Professor não encontrado: " + email));
	    return new TeacherUserDetails(teacher);
	}
}
