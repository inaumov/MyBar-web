package mybar.service.users;

import mybar.api.users.IUser;
import mybar.api.users.RoleName;
import mybar.domain.users.Role;
import mybar.domain.users.User;
import mybar.dto.users.UserDto;
import mybar.exception.users.EmailDuplicatedException;
import mybar.exception.users.UnknownUserException;
import mybar.exception.users.UserExistsException;
import mybar.repository.users.RoleRepository;
import mybar.repository.users.UserRepository;
import mybar.utils.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public IUser createUser(IUser user) throws UserExistsException, EmailDuplicatedException {
        Preconditions.checkArgument(StringUtils.hasText(user.getUsername()), "Username is required.");
        Preconditions.checkArgument(StringUtils.hasText(user.getEmail()), "Email is required.");
        checkUsernameDuplicated(user.getUsername());
        checkEmailDuplicated(user.getEmail());

        User userEntity = toEntity(user);
        userEntity.setActive(true);
        if (CollectionUtils.isEmpty(user.getRoles())) {
            Role roleUser = roleRepository.getOne(RoleName.ROLE_USER.name());
            userEntity.addRole(roleUser);
        } else {
            List<Role> assignedRoles = roleRepository.findByRoleNameIn(user.getRoles());
            assignedRoles.forEach(userEntity::addRole);
        }
        return toUserDto(userRepository.save(userEntity));
    }

    public boolean isEmailDuplicated(String email) {
        User userEntity = userRepository.findByEmail(email);
        return userEntity != null;
    }

    private void checkEmailDuplicated(String email) {
        if (isEmailDuplicated(email)) {
            throw new EmailDuplicatedException(email);
        }
    }

    private void checkUsernameDuplicated(String username) {
        boolean exists = userRepository.existsById(username);
        if (exists) {
            throw new UserExistsException(username);
        }
    }

    // admin functions

    public IUser editUserInfo(IUser user) {
        Preconditions.checkArgument(StringUtils.hasText(user.getUsername()), "Username is required.");
        Preconditions.checkArgument(StringUtils.hasText(user.getEmail()), "Email is required.");
        User userEntityByEmail = userRepository.findByEmail(user.getEmail());
        if (Objects.equals(userEntityByEmail.getEmail(), user.getEmail())) {
            throw new UserExistsException("Email already belongs to another user");
        }
        User entity = new User();
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        if (CollectionUtils.isEmpty(user.getRoles())) {
            Role roleUser = roleRepository.getOne(RoleName.ROLE_USER.name());
            entity.addRole(roleUser);
        } else {
            List<Role> roles = roleRepository.findByRoleNameIn(user.getRoles());
            entity.setRoles(roles);
        }

        return toUserDto(userRepository.save(entity));
    }

    public IUser findByUsername(String username) throws UnknownUserException {
        Preconditions.checkArgument(StringUtils.hasText(username), "Username is required.");

        return userRepository.findById(username)
                .map(this::toUserDto)
                .orElseThrow(() -> new UnknownUserException(username));
    }

    private UserDto toUserDto(User userEntity) {
        UserDto dto = new UserDto();
        dto.setUsername(userEntity.getUsername());
        dto.setPassword(userEntity.getPassword());
        dto.setName(userEntity.getName());
        dto.setSurname(userEntity.getSurname());
        dto.setEmail(userEntity.getEmail());
        dto.setRoles(userEntity.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList())
        );
        dto.setActive(userEntity.isActive());
        return dto;
    }

    public void activateUser(String username) {
        Preconditions.checkArgument(StringUtils.hasText(username), "Username is required.");

        User userEntity = userRepository.getOne(username);
        if (userEntity != null) {
            userEntity.setActive(true);
            userRepository.save(userEntity);
        }
        throw new UnknownUserException(username);
    }

    public void deactivateUser(String username) throws UnknownUserException {
        Preconditions.checkArgument(StringUtils.hasText(username), "Username is required.");

        User userEntity = userRepository.getOne(username);
        if (userEntity != null) {
            userEntity.setActive(false);
            userRepository.save(userEntity);
        }
        throw new UnknownUserException(username);
    }

    public void assignRole(IUser user, RoleName roleName) {
        User userEntity = toEntity(user);
        Role role = roleRepository.getOne(roleName.name());
        userEntity.addRole(role);
        userRepository.save(userEntity);
    }

    public List<IUser> getAllRegisteredUsers() {
        Iterable<User> users = userRepository.findAll();
        return filterUsers(users)
                .stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    // util methods

    private Collection<User> filterUsers(Iterable<User> users) {
        Role role = roleRepository.getOne(RoleName.ROLE_USER.name());
        if (role != null) {
            Predicate<User> predicate = user -> user.getRoles().contains(role);
            return filter(users, predicate);
        }
        return Collections.emptyList();
    }

    public static Collection<User> filter(Iterable<User> users, Predicate<User> predicate) {
        Collection<User> result = new ArrayList<>();
        for (User element : users) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    private User toEntity(final IUser user) {
        User entity = new User();
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setEmail(user.getEmail());
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        List<Role> roles = roleRepository.findByRoleNameIn(user.getRoles());
        entity.setRoles(roles);
        entity.setActive(user.isActive());
        return entity;
    }

    public void changePassword(IUser user, String password) {
        Preconditions.checkArgument(StringUtils.hasText(user.getUsername()), "Username is required.");
        Preconditions.checkArgument(StringUtils.hasText(password), "Password must not be empty.");

        final User one = userRepository.getOne(user.getUsername());
        one.setPassword(password);
    }

}