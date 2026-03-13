package com.saas.professor.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saas.professor.repository.AdminRepository;
import com.saas.professor.security.AdminUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {
	
	private final AdminRepository adminRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	    return adminRepository.findByEmail(email)
	        .map(AdminUserDetails::new)
	        .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado: " + email));
	}

}
