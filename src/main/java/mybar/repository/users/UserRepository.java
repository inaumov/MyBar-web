package mybar.repository.users;

import mybar.domain.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT user FROM User user WHERE LOWER(user.email) = LOWER(:email)")
    User findByEmail(@Param("email") String email);
}