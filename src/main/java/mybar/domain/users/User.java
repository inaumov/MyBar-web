package mybar.domain.users;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @Column(name = "USERNAME")
    private String username;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USERS_HAS_ROLES",
            joinColumns = {@JoinColumn(name = "USERNAME")},
            inverseJoinColumns = {@JoinColumn(name = "ROLE_NAME")}
    )
    private Collection<Role> roles;

    private String name;
    private String surname;

    @Basic
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ACTIVE")
    private boolean active;

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private Collection<Rate> rates;

    public void addRole(Role role) {
        getRoles().add(role);
    }

}