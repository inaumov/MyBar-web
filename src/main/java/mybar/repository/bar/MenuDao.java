package mybar.repository.bar;

import mybar.domain.bar.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuDao extends JpaRepository<Menu, Integer> {

}