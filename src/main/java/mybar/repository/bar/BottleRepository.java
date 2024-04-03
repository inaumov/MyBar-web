package mybar.repository.bar;

import mybar.domain.bar.Bottle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BottleRepository extends JpaRepository<Bottle, String> {

}