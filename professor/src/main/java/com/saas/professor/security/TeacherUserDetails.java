package com.saas.professor.security;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.saas.professor.entity.Teacher;

public class TeacherUserDetails implements UserDetails {
	
	private final Teacher teacher;

	public TeacherUserDetails(Teacher teacher) {
	    this.teacher = teacher;
	}

	public Teacher getTeacher() {
	    return teacher;
	}
	
	 public boolean canAccess() {
	        Teacher teacher = getTeacher();
	        if (teacher.hasActivePlan()) return true;
	        
	        // Permite trial ou plano não expirado
	        return teacher.getPlanExpiresAt() != null && 
	               LocalDateTime.now().isBefore(teacher.getPlanExpiresAt());
	    }
	
	@Override
	public String getUsername() { return teacher.getEmail(); }

	@Override
	public String getPassword() { return teacher.getPassword(); }

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }

	@Override
	public boolean isAccountNonExpired() { return true; }

	@Override
	public boolean isAccountNonLocked() { return true; }

	@Override
	public boolean isCredentialsNonExpired() { return true; }

	@Override
	public boolean isEnabled() { return teacher.getActive(); }

}
