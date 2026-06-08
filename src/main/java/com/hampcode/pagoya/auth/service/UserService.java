package com.hampcode.pagoya.auth.service;

import com.hampcode.pagoya.auth.dto.RegisterResponse;
import com.hampcode.pagoya.auth.dto.RegisterUserRequest;
import com.hampcode.pagoya.auth.exception.EmailAlreadyExistsException;
import com.hampcode.pagoya.auth.mapper.UserMapper;
import com.hampcode.pagoya.auth.model.Role;
import com.hampcode.pagoya.auth.model.User;
import com.hampcode.pagoya.auth.repository.RoleRepository;
import com.hampcode.pagoya.auth.repository.UserRepository;
import com.hampcode.pagoya.customer.exception.DniAlreadyExistsException;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }
        if (customerRepository.existsByDni(request.dni())) {
            throw new DniAlreadyExistsException();
        }
        Role role = roleRepository.findByName("CUSTOMER")
            .orElseThrow(() -> new ResourceNotFoundException(
                "no se puede completar el registro en este momento"));

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .verified(false)
            .role(role)
            .build();
        user = userRepository.save(user);

        Customer customer = Customer.builder()
            .fullName(request.fullName())
            .dni(request.dni())
            .phone(request.phone())
            .user(user)
            .build();
        customer = customerRepository.save(customer);

        return userMapper.toRegisterResponse(user, customer);
    }
}
